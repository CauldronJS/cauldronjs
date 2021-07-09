package com.cauldronjs.bindings;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.logging.Level;

import com.cauldronjs.Reflections;
import com.cauldronjs.isolate.Isolate;

import org.graalvm.polyglot.Value;

public class IsolateBindingProvider implements BindingProvider {
  public static String BOUND_VALUE_FIELD = "$$isBound";

  final Isolate isolate;
  final HashMap<String, Object> bindings = new HashMap<>();

  public IsolateBindingProvider(Isolate isolate) {
    this.isolate = isolate;
    this.isolate.getContext().getBindings("js").putMember(
      "internalBinding",
      (Function<String, Object>) this::getValue
    );
  }

  @Override
  public void initialize() {
    var internalTypes = Reflections.CAULDRON.getTypesAnnotatedWith(InternalBinding.class);
    var internalFields = Reflections.CAULDRON.getFieldsAnnotatedWith(InternalBinding.class);
    var globalTypes = Reflections.CAULDRON.getTypesAnnotatedWith(GlobalBinding.class);
    var globalFields = Reflections.CAULDRON.getFieldsAnnotatedWith(GlobalBinding.class);

    internalTypes.forEach(this::registerInternalType);
    internalFields.forEach(this::registerInternalField);
    globalTypes.forEach(this::registerGlobalType);
    globalFields.forEach(this::registerGlobalField);
  }

  @Override
  public BindingProvider register(Object object) {
    return this.register(object.getClass().getSimpleName(), object);
  }

  @Override
  public BindingProvider register(String name, Object object) {
    this.bindings.put(name, object);
    this.isolate.getContext().getPolyglotBindings().putMember(name, object);
    return this;
  }

  @Override
  public BindingProvider registerGlobal(String name, Object object) {
    this.isolate.getContext().getBindings("js").putMember(name, object);
    return this;
  }

  @Override
  public Object getValue(String name) {
    return this.bindings.getOrDefault(name, null);
  }

  private void registerInternalType(Class<?> type) {
    var namespace = type.getAnnotation(InternalBinding.class).value();
    var binding = this.getBindingAsValue(type);
    this.register(namespace, binding);
  }

  private void registerInternalField(Field field) {
    var name = String.format(
      "{0}_{1}",
      field.getDeclaringClass().getAnnotation(InternalBinding.class).value(),
      field.getAnnotation(InternalBinding.class).value()
    );
    field.setAccessible(true);
    try {
      var binding = this.isolate.getContext().asValue(field.get(null));
      this.register(name, binding);
    } catch (IllegalAccessException ex) {
      // ignore
    }
  }

  private void registerGlobalType(Class<?> type) {
    var name = type.getAnnotation(GlobalBinding.class).value();
    var binding = this.getBindingAsValue(type);
    this.registerGlobal(name, binding);
  }

  private void registerGlobalField(Field field) {
    var name = String.format(
      "{0}_{1}",
      field.getDeclaringClass().getAnnotation(InternalBinding.class).value(),
      field.getAnnotation(InternalBinding.class).value()
    );
    field.setAccessible(true);
    try {
      var binding = this.isolate.getContext().asValue(field.get(null));
      this.registerGlobal(name, binding);
    } catch (IllegalAccessException ex) {
      // ignore
    }
  }
  
  private Value getBindingAsValue(Class<?> type) {
    Constructor<?> chosenCtr = null;
    Object instance = null;
    var argsList = new ArrayList<>();
    var ctrs = type.getConstructors();

    for (var ctr: ctrs) {
      if (ctr.getParameterCount() == 1 && ctr.getParameterTypes()[0] == Isolate.class) {
        chosenCtr = ctr;
        argsList.add(this.isolate);
        break;
      } else if (ctr.getParameterCount() == 0) {
        chosenCtr = ctr;
      }
    }

    try {
      if (chosenCtr != null) {
        instance = chosenCtr.newInstance(argsList.toArray());
      } else {
        this.isolate.getCauldron().log(
          Level.SEVERE,
          "Cannot get binding for {0}, no default constructor found",
          type.getSimpleName()
        );
      }
    } catch (InvocationTargetException | IllegalAccessException | InstantiationException ex) {
      // log
      this.isolate.getCauldron().log(
        Level.SEVERE,
        "Failed to get binding for {0}: {1}",
        type.getSimpleName(),
        ex.toString()
      );
    }

    var value = this.isolate.getContext().asValue(instance);
    value.putMember(BOUND_VALUE_FIELD, true);
    return value;
  }
}
