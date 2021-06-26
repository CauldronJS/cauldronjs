package com.cauldronjs.filesystem;

import java.nio.charset.StandardCharsets;

import com.cauldronjs.bindings.InternalBinding;

public interface Encoding {
  byte[] getBytes(String input);

  String getString(byte[] data);

  @InternalBinding("Encoding_UTF8")
  public static class UTF8Encoding implements Encoding {
    @Override
    public byte[] getBytes(String input) {
      return input.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getString(byte[] data) {
      return new String(data, StandardCharsets.UTF_8);
    }
  }

  @InternalBinding("Encoding_UTF16")
  public static class UTF16Encoding implements Encoding {
    @Override
    public byte[] getBytes(String input) {
      return input.getBytes(StandardCharsets.UTF_16LE);
    }

    @Override
    public String getString(byte[] data) {
      return new String(data, StandardCharsets.UTF_16LE);
    }
  }

  @InternalBinding("Encoding_ASCII")
  public static class ASCIIEncoding implements Encoding {
    @Override
    public byte[] getBytes(String input) {
      return input.getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public String getString(byte[] data) {
      return new String(data, StandardCharsets.US_ASCII);
    }
  }
}
