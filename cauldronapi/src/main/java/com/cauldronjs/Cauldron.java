package com.cauldronjs;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

import com.cauldronjs.config.PlatformConfig;

import com.cauldronjs.isolate.IsolateManager;
import org.graalvm.polyglot.Value;

public interface Cauldron {
  boolean isRunning();
  boolean isDebugging();

  void log(Level level, String content, Object... args);

  PlatformConfig getPlatformConfig();
  IsolateManager getIsolateManager();
  File getWorkingDirectory();
  InputStream getResourceFile(String filename);

  int scheduleRepeatingTask(Value fn, int interval, int timeout);
  int scheduleTask(Value fn, int timeout);
  int scheduleRepeatingTask(Runnable runnable, int interval, int timeout);
  int scheduleTask(Runnable runnable, int timeout);

  boolean cancelTask(int id);
}