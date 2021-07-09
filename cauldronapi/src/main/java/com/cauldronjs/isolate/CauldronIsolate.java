package com.cauldronjs.isolate;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import com.cauldronjs.Cauldron;
import com.cauldronjs.bindings.BindingProvider;
import com.cauldronjs.bindings.IsolateBindingProvider;
import com.cauldronjs.events.EventEmitter;
import com.cauldronjs.exceptions.JsException;
import com.cauldronjs.filesystem.FileSystem;
import com.cauldronjs.filesystem.IsolateFileSystem;
import com.cauldronjs.scriptrunner.IsolateScriptRunner;
import com.cauldronjs.scriptrunner.ScriptRunner;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

public class CauldronIsolate extends EventEmitter implements Isolate {
  final Cauldron cauldron;
  final Engine engine;
  final Environment environment;
  final UUID uuid;
  final File currentDirectory;
  final BindingProvider bindingProvider;
  final Context context;
  final FileSystem fileSystem;
  final ScriptRunner scriptRunner;

  boolean isInitialized = false;

  public CauldronIsolate(Cauldron cauldron, Engine engine) {
    this.cauldron = cauldron;
    this.engine = engine;
    this.environment = new IsolateEnvironment(this);
    this.uuid = UUID.randomUUID();
    this.fileSystem = new IsolateFileSystem(this);
    var currentDirectory = Optional
      .ofNullable(System.getenv("CAULDRON_CWD"))
      .orElse(this.cauldron.getWorkingDirectory().getAbsolutePath());
    this.currentDirectory = Paths.get(currentDirectory).toFile();
    this.context = this.initializeContext();
    this.bindingProvider = new IsolateBindingProvider(this);
    this.scriptRunner = new IsolateScriptRunner(this);
  }

  public CauldronIsolate(Cauldron cauldron, Engine engine, String directory) {
    this.cauldron = cauldron;
    this.engine = engine;
    this.environment = new IsolateEnvironment(this);
    this.uuid = UUID.randomUUID();
    this.fileSystem = new IsolateFileSystem(this);
    this.currentDirectory = Paths.get(directory).toFile();
    this.context = this.initializeContext();
    this.bindingProvider = new IsolateBindingProvider(this);
    this.scriptRunner = new IsolateScriptRunner(this);
  }

  @Override
  public Cauldron getCauldron() {
    return this.cauldron;
  }

  @Override
  public UUID getUUID() {
    return this.uuid;
  }

  @Override
  public File getCurrentDirectory() {
    return this.currentDirectory;
  }

  @Override
  public Context getContext() {
    return this.context;
  }

  @Override
  public BindingProvider getBindingProvider() {
    return this.bindingProvider;
  }

  @Override
  public FileSystem getFileSystem() {
    return this.fileSystem;
  }

  @Override
  public Environment getEnvironment() {
    return this.environment;
  }

  @Override
  public void initialize() {
    this.context.enter();
    if (!this.isInitialized) {
      this.environment.initialize();
      this.bindingProvider.initialize();
      this.registerBindings();
      try {
        this.scriptRunner.initializeCoreLibrary();
        this.isInitialized = true;
        this.emit("activate");
      } catch (JsException ex) {
        this.cauldron.log(Level.SEVERE, "An error occurred while loading the core library: {0}", ex);
      }
    }
    this.cauldron.scheduleRepeatingTask(() -> this.emit("tick"), 50, 50);
  }

  @Override
  public void dispose() {
    this.context.leave();
    this.emit("shutdown");
    this.context.close();
  }

  @Override
  public void runEntryPoint() throws JsException {
    this.scriptRunner.initializeEntryPoint();
  }

  @Override
  public Value eval(CharSequence content) throws JsException {
    return this.scriptRunner.eval(content);
  }
  
  private Context initializeContext() {
    var entryClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(Cauldron.class.getClassLoader());
    var context = Context.newBuilder("js")
      .option("js.ecmascript-version", "11")
      .allowHostAccess(HostAccess.ALL)
      //.allowAllAccess(true)
      //.allowHostClassLoading(true)
      .engine(this.engine)
      .build();
    Thread.currentThread().setContextClassLoader(entryClassLoader);
    return context;
  }

  private void registerBindings() {
    this.bindingProvider
      .register(this.environment)
      .registerGlobal("process", false);
  }
}
