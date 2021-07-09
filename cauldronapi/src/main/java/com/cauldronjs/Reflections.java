package com.cauldronjs;

import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

public class Reflections {
  public static final org.reflections.Reflections CAULDRON = new org.reflections.Reflections(
    "com.cauldronjs",
    new SubTypesScanner(),
    new TypeAnnotationsScanner(),
    new FieldAnnotationsScanner()
  );
}
