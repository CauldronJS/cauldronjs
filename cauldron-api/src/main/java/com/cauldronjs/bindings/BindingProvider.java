package com.cauldronjs.bindings;

public interface BindingProvider {
  void initialize();
  BindingProvider register(Object object);
  BindingProvider register(String name, Object object);
  BindingProvider registerGlobal(String name, Object object);
  Object getValue(String name);
}
