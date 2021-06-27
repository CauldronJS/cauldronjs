package com.cauldronjs.filesystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.cauldronjs.bindings.InternalBinding;
import com.cauldronjs.isolate.Isolate;

@InternalBinding("FileSystem")
public class IsolateFileSystem implements FileSystem {

  final Isolate isolate;
  final String baseDirectory;

  public IsolateFileSystem(Isolate isolate) {
    this.isolate = isolate;
    this.baseDirectory = isolate.getCurrentDirectory().getAbsolutePath();
  }

  @Override
  public Path getPath(String path, String... parts) throws AccessDeniedException {
    var pathArgs = Paths.get(path, parts);
    var fullPath = Paths.get(this.baseDirectory, pathArgs.toString());
    this.assertAccess(fullPath);
    return fullPath.toAbsolutePath();
  }

  @Override
  public File getFile(String filename) throws AccessDeniedException {
    var fullPath = Paths.get(this.baseDirectory, filename).toFile();
    this.assertAccess(fullPath);
    return fullPath;
  }

  @Override
  public String[] readLines(String filename) throws IOException, AccessDeniedException, FileNotFoundException {
    var reader = this.getReaderForFile(filename);
    var result = new ArrayList<>();
    String line;
    while ((line = reader.readLine()) != null) {
      result.add(line);
    }
    reader.close();
    return result.toArray(new String[0]);
  }

  @Override
  public String readString(String filename) throws IOException, AccessDeniedException, FileNotFoundException {
    var reader = this.getReaderForFile(filename);
    var result = "";
    String line;
    while ((line = reader.readLine()) != null) {
      result += String.format("{0}{1}", line, System.lineSeparator());
    }
    reader.close();
    return result;
  }

  @Override
  public byte[] read(String filename) throws IOException, AccessDeniedException, FileNotFoundException {
    var file = this.getPath(filename);
    return Files.readAllBytes(file);
  }

  @Override
  public void write(String filename, String content) throws AccessDeniedException, IOException {
    var localizedFile = this.getFile(filename);
    this.assertAccess(localizedFile);
    localizedFile.createNewFile();
    var writer = new FileWriter(localizedFile);
    var stream = new BufferedWriter(writer);
    stream.write(content);
    stream.close();
  }

  @Override
  public void write(String filename, String content, int position) throws AccessDeniedException, IOException {
    var localizedFile = this.getFile(filename);
    this.assertAccess(localizedFile);
    localizedFile.createNewFile();
    var writer = new FileWriter(localizedFile);
    var stream = new BufferedWriter(writer);
    stream.write(content, position, content.length());
    stream.close();
  }

  @Override
  public void write(String filename, byte[] content) throws AccessDeniedException, IOException {
    var localizedFile = this.getFile(filename);
    this.assertAccess(localizedFile);
    localizedFile.createNewFile();
    var writer = new FileOutputStream(localizedFile);
    writer.write(content);
    writer.close();
  }

  @Override
  public void append(String filename, String content) throws AccessDeniedException, IOException {
    var localizedFile = this.getFile(filename);
    localizedFile.createNewFile();
    this.assertAccess(localizedFile);
    var stream = new FileWriter(localizedFile, true);
    var writer = new BufferedWriter(stream);
    writer.write(content);
    writer.newLine();
    writer.close();
  }

  private void assertAccess(Path path) throws AccessDeniedException {
    if (!path.toAbsolutePath().startsWith(this.baseDirectory)) {
      throw new AccessDeniedException(path.toString());
    }
  }

  private void assertAccess(File file) throws AccessDeniedException {
    if (!file.getAbsolutePath().startsWith(this.baseDirectory)) {
      throw new AccessDeniedException(file.getAbsolutePath());
    }
  }

  private BufferedReader getReaderForFile(String filename) throws AccessDeniedException, FileNotFoundException {
    var localizedFilename = Paths.get(this.baseDirectory, filename).toString();
    var localFile = new File(localizedFilename);
    var resourceFile = this.isolate.getCauldron().getResourceFile(filename);
    if (localFile.exists()) {
      this.assertAccess(localFile);
      return new BufferedReader(new InputStreamReader(new FileInputStream(localFile)));
    } else if (resourceFile != null) {
      return new BufferedReader(new InputStreamReader(resourceFile));
    } else {
      throw new FileNotFoundException(filename);
    }
  }
}
