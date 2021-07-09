package com.cauldronjs.isolate;

import java.util.HashMap;
import java.util.logging.Level;

import com.cauldronjs.Cauldron;

import org.graalvm.polyglot.Engine;

public class IsolateManager {
  final Cauldron cauldron;
  final Engine engine;
  final HashMap<Long, Isolate> isolates = new HashMap<>();
  Long mainIsolateThreadId;

  public IsolateManager(Cauldron cauldron) {
    this.cauldron = cauldron;
    this.engine = Engine.create();
  }

  public Isolate getCurrentThreadIsolate() {
    var threadId = Thread.currentThread().getId();
    synchronized (isolates) {
      if (this.isolates.containsKey(threadId)) {
        return this.isolates.get(threadId);
      } else {
        return null;
      }
    }
  }

  public Isolate createIsolate() {
    return this.createIsolate(this.cauldron.getWorkingDirectory().getAbsolutePath());
  }

  public Isolate createIsolate(String directory) {
    var threadId = Thread.currentThread().getId();
    var isolate = new CauldronIsolate(this.cauldron, this.engine, directory);
    try {
      isolate.initialize();
    } catch (Exception ex) {
      this.cauldron.log(Level.SEVERE, "Failed to initialize new isolate, {0}", ex);
      return null;
    }

    if (this.mainIsolateThreadId == null) {
      this.mainIsolateThreadId = threadId;
    }
    this.isolates.put(threadId, isolate);
    return isolate;
  }

  public Isolate getMainThreadIsolate() {
    return this.isolates.getOrDefault(this.mainIsolateThreadId, null);
  }
}
