package com.cauldronjs.exceptions;

import com.cauldronjs.isolate.Isolate;

public class IsolateException extends Exception {

  /**
   *
   */
  static final long serialVersionUID = 1599937854838882129L;

  final Isolate isolate;

  public IsolateException(Isolate isolate, String message) {
    super(message);
    this.isolate = isolate;
  }

  public Isolate getIsolate() {
    return this.isolate;
  }
}