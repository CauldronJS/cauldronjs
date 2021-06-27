package com.cauldronjs.isolate;

import java.io.File;
import java.util.UUID;

import com.cauldronjs.Cauldron;
import com.cauldronjs.bindings.BindingProvider;
import com.cauldronjs.exceptions.JsException;
import com.cauldronjs.filesystem.FileSystem;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public interface Isolate {
  Cauldron getCauldron();
  UUID getUUID();
  File getCurrentDirectory();
  Context getContext();
  BindingProvider getBindingProvider();
  FileSystem getFileSystem();
  Environment getEnvironment();

  void initialize();
  void dispose();

  void runEntryPoint() throws JsException;
  Value eval(CharSequence content) throws JsException;
}
