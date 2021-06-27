package com.cauldronjs.scriptrunner;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.cauldronjs.exceptions.JsException;
import com.cauldronjs.isolate.Isolate;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.management.ExecutionEvent;
import org.graalvm.polyglot.management.ExecutionListener;

public class IsolateScriptRunner implements ScriptRunner {

  static final String CORE_ENTRY = "lib/internal/bootstrap/loader.js";
  static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

  final Isolate isolate;
  final Context context;
  final ScriptEngine scriptEngine;
  final ExecutionListener executionListener;
  Value modulePackage = null;

  public IsolateScriptRunner(Isolate isolate) {
    this.isolate = isolate;
    this.context = isolate.getContext();
    this.scriptEngine = scriptEngineManager.getEngineByName("graal-js");
    this.executionListener = ExecutionListener.newBuilder()
      .onEnter(this::handleExecutionEnter)
      .statements(true)
      .roots(true)
      .attach(this.context.getEngine()
    );
  }

  @Override
  @InternalAccessOnly
  public void initializeCoreLibrary() throws JsException {
    try {
      this.evalFile(CORE_ENTRY);
    } catch (Exception ex) {
      throw prunedJsException(ex);
    }
  }

  @Override
  @InternalAccessOnly
  public void initializeEntryPoint() throws JsException {
    try {
      this.modulePackage = this.context.getBindings("js").getMember("NativeModule").invokeMember("$$bootstrap");
    } catch (Exception ex) {
      throw prunedJsException(ex);
    }
  }

  @Override
  @InternalAccessOnly
  public Value eval(CharSequence content, String filename) throws JsException {
    try {
      var source = Source.newBuilder("js", content, filename).build();
      return this.context.eval(source);
    } catch (Exception ex) {
      throw prunedJsException(ex);
    }
  }

  @Override
  @InternalAccessOnly
  public Value eval(CharSequence content) throws JsException {
    try {
      return this.modulePackage.getMember("evalInContext").execute(content);
    } catch (Exception ex) {
      throw prunedJsException(ex);
    }
  }

  @Override
  @InternalAccessOnly
  public Value evalFile(String filename) throws JsException {
    try {
      return this.eval(this.isolate.getFileSystem().readString(filename), filename);
    } catch (Exception ex) {
      throw prunedJsException(ex);
    }
  }

  @Override
  @InternalAccessOnly
  public Value requireModule(String id) throws JsException {
    try {
      return this.modulePackage.getMember("_load").execute(id, null, false);
    } catch (Exception ex) {
      throw prunedJsException(ex);
    }
  }

  @Override
  @InternalAccessOnly
  public void validate(CharSequence content) throws JsException {
    try {
      ((Compilable) this.scriptEngine).compile(content.toString());
    } catch (Exception ex) {
      throw prunedJsException(ex);
    }
  }
  
  private JsException prunedJsException(Exception ex) {
    var pruned = new JsException(this.isolate.getCauldron(), ex);
    return pruned;
  }

  private void handleExecutionEnter(ExecutionEvent executionEvent) {
    // TODO: verify the safety of the calls being made
    
  }
}
