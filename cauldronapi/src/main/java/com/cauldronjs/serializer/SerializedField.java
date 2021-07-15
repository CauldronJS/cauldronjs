package com.cauldronjs.serializer;

public @interface SerializedField {
  String value() default "";
}
