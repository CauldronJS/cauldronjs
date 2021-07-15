package com.cauldronjs.promise;

import com.cauldronjs.async.Thenable;

public class PromiseEntry {
  final Thenable body;
  final int id;
  final int parentId;
  PromiseState state = PromiseState.INIT;
  boolean wasSuccessful = false;

  public PromiseEntry(Thenable body, int id, int parentId) {
    this.body = body;
    this.id = id;
    this.parentId = parentId;
  }

  public Thenable getBody() {
    return this.body;
  }

  public int getId() {
    return this.id;
  }

  public int getParentId() {
    return this.parentId;
  }

  public PromiseState getState() {
    return this.state;
  }

  public void setState(PromiseState state) {
    this.state = state;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof PromiseEntry) {
      var otherAsPromiseEntry = (PromiseEntry) other;
      return otherAsPromiseEntry.id == this.id && otherAsPromiseEntry.parentId == this.parentId;
    }
    return false;
  }
}
