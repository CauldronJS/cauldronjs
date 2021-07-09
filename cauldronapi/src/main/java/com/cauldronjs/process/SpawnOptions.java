package com.cauldronjs.process;

import com.cauldronjs.serializer.Serializable;
import org.graalvm.polyglot.Value;

import java.io.File;
import java.util.HashMap;

public class SpawnOptions extends Serializable {
  File cwd;
  HashMap<String, Object> env;
  String argv0;
  ProcessBuilder.Redirect inputRedirect;
  ProcessBuilder.Redirect outputRedirect;
  ProcessBuilder.Redirect errorRedirect;
  int uid;
  int gid;
  String serialization;

  public SpawnOptions() {
    this.env = new HashMap<>();
    this.cwd = new File(".");
    this.argv0 = null;
    this.inputRedirect = ProcessBuilder.Redirect.INHERIT;
    this.outputRedirect = ProcessBuilder.Redirect.INHERIT;
    this.errorRedirect = ProcessBuilder.Redirect.INHERIT;
  }

  public SpawnOptions(
    File cwd,
    HashMap<String, Object> env,
    String argv0,
    ProcessBuilder.Redirect inputRedirect,
    ProcessBuilder.Redirect outputRedirect,
    ProcessBuilder.Redirect errorRedirect,
    int uid,
    int gid,
    String serialization) {
    this.cwd = cwd;
    this.env = env;
    this.argv0 = argv0;
    this.inputRedirect = inputRedirect;
    this.outputRedirect = outputRedirect;
    this.errorRedirect = errorRedirect;
    this.uid = uid;
    this.gid = gid;
    this.serialization = serialization;
  }

  public File getCwd() {
    return this.cwd;
  }

  public HashMap<String, Object> getEnv() {
    return this.env;
  }

  public String getArgv0() {
    return this.argv0;
  }

  public ProcessBuilder.Redirect getInputRedirect() {
    return this.inputRedirect;
  }

  public ProcessBuilder.Redirect getOutputRedirect() {
    return this.outputRedirect;
  }

  public ProcessBuilder.Redirect getErrorRedirect() {
    return this.errorRedirect;
  }

  public int getUid() {
    return this.uid;
  }

  public void setUid(int value) {
    this.uid = value;
  }

  public int getGid() {
    return this.gid;
  }

  public String getSerialization() {
    return this.serialization;
  }

  private static String getRedirectAsString(ProcessBuilder.Redirect redirect) {
    if (redirect == ProcessBuilder.Redirect.PIPE) {
      return "pipe";
    } else if (redirect == ProcessBuilder.Redirect.INHERIT) {
      return "inherit";
    } else {
      return "ignore";
    }
  }

  private static ProcessBuilder.Redirect getRedirectFromString(String redirect) {
    return switch (redirect.toLowerCase()) {
      case "pipe" -> ProcessBuilder.Redirect.PIPE;
      case "inherit" -> ProcessBuilder.Redirect.INHERIT;
      default -> null;
    };
  }

  @Override
  public Value serialize() {
    var result = Value.asValue(new Object());
    result.putMember("cwd", this.cwd.toString());
    result.putMember("env", this.env);
    result.putMember("argv0", this.argv0);
    var stdio = new String[] {
      getRedirectAsString(this.inputRedirect),
      getRedirectAsString(this.outputRedirect),
      getRedirectAsString(this.errorRedirect)
    };
    result.putMember("stdio", stdio);
    result.putMember("detached", false);
    result.putMember("uid", this.uid);
    result.putMember("gid", this.gid);
    result.putMember("serialization", this.serialization);
    result.putMember("shell", false);
    result.putMember("windowsVerbatimArguments", false);
    result.putMember("windowsHide", false);
    return result;
  }

  @Override
  public void deserialize(Value value) {
    this.cwd = new File(value.getMember("cwd").asString());
    var envKeys = value.getMember("env").getMemberKeys();
    envKeys.forEach(key -> {
      this.env.put(key, value.getMember("env").getMember(key));
    });
    this.argv0 = value.getMember("argv0").asString();
    var stdio = value.getMember("stdio");
    this.inputRedirect = getRedirectFromString(stdio.getArrayElement(0).toString());
    this.outputRedirect = getRedirectFromString(stdio.getArrayElement(1).toString());
    this.errorRedirect = getRedirectFromString(stdio.getArrayElement(2).toString());
    this.uid = value.getMember("uid").asInt();
    this.gid = value.getMember("gid").asInt();
    this.serialization = value.getMember("serialization").asString();
  }
}
