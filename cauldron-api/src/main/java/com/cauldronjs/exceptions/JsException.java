package com.cauldronjs.exceptions;

import java.util.stream.Stream;

import com.cauldronjs.Cauldron;

public class JsException extends Exception {
  private static final long serialVersionUID = 1599937854838882128L;

  public JsException(Cauldron cauldron, Throwable throwable) {
    super("[" + throwable.getClass().getName() + "]: " + throwable.getMessage());
    var isDebugging = cauldron.isDebugging();
    var stackTrace = Stream.of(throwable.getStackTrace());
    var cleanedTrace = stackTrace.filter(
        stackTraceElement -> isDebugging || !stackTraceElement.toString().contains("lib/internal/modules/loader.js"))
        .toArray(StackTraceElement[]::new);
    this.setStackTrace(cleanedTrace);
  }
}
