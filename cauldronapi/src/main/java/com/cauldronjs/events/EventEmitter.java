package com.cauldronjs.events;

import java.util.ArrayList;
import java.util.HashMap;

import com.cauldronjs.async.RunnableWithArgs;

import org.graalvm.polyglot.Value;

public class EventEmitter {
  private static class HandlerFrame {
      final RunnableWithArgs runnable;
      final boolean shouldDeleteAfterRun;

      public HandlerFrame(RunnableWithArgs runnable, boolean shouldDeleteAfterRun) {
          this.runnable = runnable;
          this.shouldDeleteAfterRun = shouldDeleteAfterRun;
      }

      public RunnableWithArgs getRunnable() {
          return runnable;
      }

      public boolean shouldDeleteAfterRun() {
          return shouldDeleteAfterRun;
      }
  }

  volatile HashMap<String, ArrayList<HandlerFrame>> handlers;
  int maxListeners = 256;

  public EventEmitter() {
      this.handlers = new HashMap<>();
  }

  public void on(String event, Value handler) {
      this.on(event, handler::executeVoid);
  }

  public void on(String event, RunnableWithArgs handler) {
      var frame = new HandlerFrame(handler, false);
      if (!this.handlers.containsKey(event)) {
          this.handlers.put(event, new ArrayList<>());
      }
      this.handlers.get(event).add(frame);
  }

  public void prependListener(String event, Value handler) {
      this.prependListener(event, handler::executeVoid);
  }

  public void prependListener(String event, RunnableWithArgs handler) {
      var frame = new HandlerFrame(handler, false);
      if (!this.handlers.containsKey(event)) {
          this.handlers.put(event, new ArrayList<>());
      }
      this.handlers.get(event).add(0, frame);
  }

  public void addListener(String event, RunnableWithArgs handler) {
      this.on(event, handler);
  }

  public void addListener(String event, Value handler) {
      this.on(event, handler);
  }

  public void once(String event, Value handler) {
      this.once(event, handler::executeVoid);
  }

  public void once(String event, RunnableWithArgs handler) {
      var frame = new HandlerFrame(handler, true);
      if (!this.handlers.containsKey(event)) {
          this.handlers.put(event, new ArrayList<>());
      }
      this.handlers.get(event).add(frame);
  }

  public void emit(String event, Object... args) {
      ArrayList<HandlerFrame> frames = this.handlers.getOrDefault(event, null);
      if (frames != null) {
          for (var frame: frames) {
              try {
                  frame.getRunnable().run(args);
                  if (frame.shouldDeleteAfterRun()) {
                      frames.remove(frame);
                  }
              } catch (Exception ex) {
                  // log somewhere?
              }
          }
      }
  }

  public void removeAllListeners() {
      this.handlers.clear();
  }

  public void removeAllListeners(String event) {
      this.handlers.remove(event);
  }

  public String[] eventNames() {
      return this.handlers.keySet().toArray(new String[0]);
  }

  public int getMaxListeners() {
      return this.maxListeners;
  }

  public void setMaxListeners(int maxListeners) {
      this.maxListeners = maxListeners;
  }
}
