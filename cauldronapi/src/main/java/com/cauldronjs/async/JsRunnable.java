package com.cauldronjs.async;

import com.cauldronjs.bindings.InternalBinding;

import org.graalvm.polyglot.Value;

public class JsRunnable implements Runnable {
  final Value fn;

  @InternalBinding("Runnable")
  public JsRunnable(Value fn) {
    this.fn = fn;
  }

  @Override
  public void run() {
    this.fn.executeVoid();
  }
  
  public JsRunnable create(Value fn) {
    return new JsRunnable(fn);
  }
}
