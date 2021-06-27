package com.cauldronjs.isolate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

public class IsolateEnvironment implements Environment {
  final Isolate isolate;
  final Map<String, String> variables;

  public IsolateEnvironment(Isolate isolate) {
    this.isolate = isolate;
    this.variables = new HashMap<>(System.getenv());
  }

  @Override
  public void initialize() {
    var fs = this.isolate.getFileSystem();
    try {
      var content = fs.readLines(".env");
      for (var line: content) {
        var kvp = line.split("=", 1);
        if (kvp.length != 2) {
          kvp[1] = "";
        }
        this.variables.put(kvp[0], kvp[1]);
      }
    } catch (AccessDeniedException ex) {
      // shouldn't happen, ignore
    } catch (FileNotFoundException ex) {
      // ignore
    } catch (IOException ex) {
      this.isolate.getCauldron().log(Level.ERROR, "Failed to read from .env, {0}", ex);
    }
  }

  @Override
  public Map<String, String> getVariables() {
    return this.variables;
  }
}
