package com.cauldronjs.promise;

import com.cauldronjs.async.Thenable;

public interface PromiseManager {
  PromiseEntry push(Thenable thenable, int parentId);
  PromiseEntry pop();
  PromiseEntry get(int id);
  void remove(int id);
  void finalizeState(int id);
}
