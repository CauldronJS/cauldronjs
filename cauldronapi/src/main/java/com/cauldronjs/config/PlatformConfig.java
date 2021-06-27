package com.cauldronjs.config;

public class PlatformConfig {
  final String platform;
  final String version;

  public PlatformConfig(String platform, String version) {
    this.platform = platform;
    this.version = version;
  }

  public String getPlatform() {
    return this.platform;
  }

  public String getVersion() {
    return this.version;
  }
}
