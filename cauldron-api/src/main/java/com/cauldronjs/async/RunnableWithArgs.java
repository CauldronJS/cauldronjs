package com.cauldronjs.async;

@FunctionalInterface
public interface RunnableWithArgs {
  void run(Object... args);
}
