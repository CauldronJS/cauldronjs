package com.cauldronjs.promise;

public enum PromiseState {
  /**
   * The promise has been queued but not called
   */
  INIT,
  /**
   * The promise is getting ready to be called and popped
   * from the manager already
   */
  BEFORE,
  /**
   * The promise has completed and the manager will begin
   * releasing resources shortly
   */
  AFTER,
  /**
   * The promise body threw an error, resulting in the rest
   * of the promise and its children being cancelled
   */
  DESTROY,
  /**
   * The promise body successfully completed, resulting in
   * continuation of the promise
   */
  RESOLVE,
}
