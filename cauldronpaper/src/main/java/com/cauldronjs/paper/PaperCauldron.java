package com.cauldronjs.paper;

import com.cauldronjs.Cauldron;
import com.cauldronjs.config.PlatformConfig;
import com.cauldronjs.exceptions.JsException;
import com.cauldronjs.isolate.IsolateManager;
import com.cauldronjs.paper.commands.SpigotCommandProvider;
import com.cauldronjs.paper.events.SpigotEventProvider;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.graalvm.polyglot.Value;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

public class PaperCauldron extends JavaPlugin implements Cauldron {
  boolean isRunning = false;

  final PlatformConfig platformConfig;
  final IsolateManager isolateManager;
  final SpigotEventProvider eventProvider;
  final SpigotCommandProvider commandProvider;

  public PaperCauldron() {
    this.platformConfig = new PlatformConfig("bukkit", "none");
    this.isolateManager = new IsolateManager(this);
    this.eventProvider = new SpigotEventProvider(this);
    this.commandProvider = new SpigotCommandProvider();
  }

   @Override
   public void onEnable() {
    var mainIsolate = this.isolateManager.getCurrentThreadIsolate();
    mainIsolate.initialize();
    try {
      mainIsolate.getBindingProvider().register(this.eventProvider);
      mainIsolate.getBindingProvider().register(this.commandProvider);
      mainIsolate.runEntryPoint();
      this.log(Level.INFO, "Finished initializing Cauldron");
    } catch (JsException ex) {
      this.log(Level.SEVERE, "Failed to start Cauldron: {0}", ex);
    }
   }

  @Override
  public boolean isRunning() {
    return this.isRunning;
  }

  @Override
  public boolean isDebugging() {
    return false;
  }

  @Override
  public void log(Level level, String content, Object... args) {
    this.getLogger().log(level, content, args);
  }

  @Override
  public PlatformConfig getPlatformConfig() {
    return null;
  }

  @Override
  public IsolateManager getIsolateManager() {
    return null;
  }

  @Override
  public File getWorkingDirectory() {
    return null;
  }

  @Override
  public InputStream getResourceFile(String filename) {
    return null;
  }

  @Override
  public int scheduleRepeatingTask(Value fn, int interval, int timeout) {
    return 0;
  }

  @Override
  public int scheduleTask(Value fn, int timeout) {
    return 0;
  }

  @Override
  public int scheduleRepeatingTask(Runnable runnable, int interval, int timeout) {
    return 0;
  }

  @Override
  public int scheduleTask(Runnable runnable, int timeout) {
    return 0;
  }

  @Override
  public boolean cancelTask(int id) {
    return false;
  }
}
