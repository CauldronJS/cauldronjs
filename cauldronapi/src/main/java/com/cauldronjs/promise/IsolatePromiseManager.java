package com.cauldronjs.promise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.cauldronjs.async.Thenable;
import com.cauldronjs.isolate.Isolate;

public class IsolatePromiseManager implements PromiseManager {
  final Isolate isolate;
  final Map<Integer, PromiseEntry> backingStore;
  final Map<Integer, List<Integer>> children;
  final Queue<Integer> queue;

  int currentPromiseId = 0;

  public IsolatePromiseManager(Isolate isolate) {
    this.isolate = isolate;
    this.backingStore = new HashMap<>();
    this.children = new HashMap<>();
    this.queue = new LinkedBlockingQueue<>();
  }

  @Override
  /**
   * @param thenable The promise body
   * @param parentId The overarching parent promise ID. If none exists, use -1.
   */
  public PromiseEntry push(Thenable thenable, int parentId) {
    var entry = new PromiseEntry(thenable, currentPromiseId++, parentId);
    this.backingStore.put(entry.getId(), entry);
    this.queue.add(entry.getId());
    return entry;
  }

  @Override
  public PromiseEntry pop() {
    var nextId = this.queue.poll();
    return this.backingStore.get(nextId);
  }

  @Override
  public PromiseEntry get(int id) {
    return this.backingStore.get(id);
  }

  @Override
  public void remove(int id) {
    this.backingStore.get(id).setState(PromiseState.DESTROY);
  }

  @Override
  public void finalizeState(int id) {
    var entry = this.backingStore.get(id);
    switch (entry.getState()) {
      case INIT:
        entry.setState(PromiseState.BEFORE);
        break;
      case BEFORE:
        entry.setState(PromiseState.AFTER);
        break;
      case AFTER:
        if (entry.wasSuccessful) {
          entry.setState(PromiseState.RESOLVE);
        } else {
          entry.setState(PromiseState.DESTROY);
        }
        break;
      case DESTROY:
        this.delete(id, true);
        break;
      case RESOLVE:
        this.delete(id, false);
        break;
    }
  }

  private void delete(int id, boolean deleteChildren) {
    if (id == -1) return;
    synchronized(this.backingStore) {
      if (deleteChildren) {
        var children = this.children.get(id);
        for (var childId: children) {
          this.backingStore.remove(childId);
          this.queue.remove(childId);
          this.delete(childId, deleteChildren);
        }
      }
      this.backingStore.remove(id);
      this.queue.remove(id);
      this.children.remove(id);
    }
  }
}
