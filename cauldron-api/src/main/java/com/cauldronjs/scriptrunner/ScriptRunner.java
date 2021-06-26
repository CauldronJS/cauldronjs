package com.cauldronjs.scriptrunner;

import com.cauldronjs.exceptions.JsException;

import org.graalvm.polyglot.Value;

public interface ScriptRunner {
  void initializeCoreLibrary() throws JsException;
  void initializeEntryPoint() throws JsException;

  Value eval(CharSequence content, String filename) throws JsException;
  Value eval(CharSequence content) throws JsException;
  Value evalFile(String filename) throws JsException;
  Value requireModule(String id) throws JsException;
  void validate(CharSequence content) throws JsException;
}
