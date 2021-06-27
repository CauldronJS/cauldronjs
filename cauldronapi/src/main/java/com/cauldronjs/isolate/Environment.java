package com.cauldronjs.isolate;

import java.util.Map;

public interface Environment {
  void initialize();
  Map<String, String> getVariables();
}
