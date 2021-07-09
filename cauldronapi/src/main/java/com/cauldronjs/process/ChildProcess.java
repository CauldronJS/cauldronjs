package com.cauldronjs.process;

import com.cauldronjs.events.EventEmitter;
import com.cauldronjs.serializer.SerializedField;
import com.cauldronjs.serializer.SerializedObject;

import java.io.*;
import java.lang.reflect.Array;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class ChildProcess extends EventEmitter {
  @SerializedObject
  public static class ChildProcessResult {
    @SerializedField("pid")
    final long pid;
    @SerializedField("output")
    final String[] output;
    @SerializedField("stdout")
    final String stdout;
    @SerializedField("stderr")
    final String stderr;
    @SerializedField("status")
    int status;
    @SerializedField("signal")
    final String signal;
    @SerializedField("error")
    final Exception error;

    private ChildProcessResult(long pid, String stdout, String stderr, int status, String signal, Exception error) {
      this.pid = pid;
      this.output = new String[] {
        "",
        stdout,
        stderr
      };
      this.stdout = stdout;
      this.stderr = stderr;
      this.status = status;
      this.signal = signal;
      this.error = error;
    }
  }

  final Process process;
  final int uid;
  final CountDownLatch latch;

  boolean isAsync = true;
  long pid;
  Thread runner;
  String killSignal;
  ChildProcessResult result;

  protected ChildProcess(Process process, int uid) {
    this.process = process;
    this.uid = uid;
    this.latch = new CountDownLatch(1);
  }

  public static ChildProcess spawn(String command, String[] args, SpawnOptions options) throws IOException {
    var commandWithArgs = new String[1 + args.length];
    commandWithArgs[0] = command;
    System.arraycopy(args, 0, commandWithArgs, 1, args.length);
    var builder = new ProcessBuilder(commandWithArgs)
      .directory(options.getCwd())
      .redirectOutput(options.getOutputRedirect())
      .redirectInput(options.getInputRedirect())
      .redirectError(options.getErrorRedirect());
    var process = new ChildProcess(builder.start(), options.getUid());
    process.pipeStreams();
    process.runner.start();
    process.on("finish", (eventArgs) -> process.result.status = (int)eventArgs[0]);
    return process;
  }

  public static ChildProcess spawnSync(String command, String[] args, SpawnOptions options) throws IOException, InterruptedException {
    var commandWithArgs = new String[1 + args.length];
    commandWithArgs[0] = command;
    System.arraycopy(args, 0, commandWithArgs, 1, args.length);
    var builder = new ProcessBuilder(commandWithArgs)
      .directory(options.getCwd())
      .redirectOutput(options.getOutputRedirect())
      .redirectInput(options.getInputRedirect())
      .redirectError(options.getErrorRedirect());
    var process = new ChildProcess(builder.start(), options.getUid());
    process.pipeStreams();
    process.isAsync = false;
    var stdin = new AtomicReference<>("");
    var stderr = new AtomicReference<>("");

    process.on("stdin", (eventArgs) -> stdin.set(stdin.get() + "\n" + eventArgs[0]));
    process.on("stderr", (eventArgs) -> stderr.set(stderr.get() + "\n" + eventArgs[0]));

    process.runner.start();
    var pid = process.pid;
    process.runner.join();
    var status = process.process.exitValue();
    process.result = new ChildProcessResult(pid, stdin.get(), stderr.get(), status, process.killSignal, null);
    return process;
  }

  public void kill() throws InterruptedException {
    this.process.destroyForcibly().waitFor();
  }

  public void kill(String signal) throws InterruptedException {
    this.killSignal = signal;
    this.process.destroyForcibly().waitFor();
  }

  public void waitFor() throws InterruptedException {
    this.process.waitFor();
  }

  public int getUid() {
    return this.uid;
  }

  public OutputStream getOutputStream() {
    return this.process.getOutputStream();
  }

  public InputStream getInputStream() {
    return this.process.getInputStream();
  }

  public InputStream getErrorStream() {
    return this.process.getErrorStream();
  }

  private static boolean isEof(String input) {
    return input != null && input.trim().equals("--EOF--");
  }

  private void pipeStreams() {
    this.pid = this.process.pid();
    var outputStream = this.getOutputStream();
    var inputStream = this.getInputStream();
    var errorStream = this.getErrorStream();
    this.runner = new Thread(() -> {
      var inputReader = new BufferedReader(new InputStreamReader(inputStream));
      var errorReader = new BufferedReader(new InputStreamReader(errorStream));
      var outputWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

      try {
        String input, error = null;
        while (ChildProcess.this.process.isAlive() &&
          ((input = inputReader.readLine()) != null || (error = errorReader.readLine()) != null) &&
          (!isEof(input) || !isEof(error))) {
          if (input != null) {
            ChildProcess.this.emit("stdin", input);
          }
          if (error != null) {
            ChildProcess.this.emit("stderr", error);
          }
          if (ChildProcess.this.isAsync) {
            Thread.sleep(10);
          }
        }
        inputReader.close();
        errorReader.close();
        outputWriter.close();
      } catch (IOException | InterruptedException | IllegalThreadStateException ex) {
        System.err.println(ex);
      }
      ChildProcess.this.latch.countDown();
    });

    this.runner.setName("CauldronProcessWatcher-" + this.runner.getId());
  }
}
