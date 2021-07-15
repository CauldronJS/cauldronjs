const promiseManager = internalBinding('PromiseManager');

class Promise {
  /**
   *
   * @param {(resolve: (value: any) => void, reject: (reason?: any) => void)} executor
   * @param {number} parentId
   */
  constructor(executor, parentId = -1) {
    this.$$executor = executor;
    this.$$parentId = parentId;
    // informs the promise thread of this being created
    this.$$asyncEntry = promiseManager.push(this, parentId);
    this.$$returnValue = undefined;
  }

  then(fn) {
    // we'll be created a brand new promise, but this time with
    // a parent entry
    return new Promise((resolve, reject) => {
      var result = fn();
    });
  }

  catch(fn) {}

  finally(fn) {}
}
