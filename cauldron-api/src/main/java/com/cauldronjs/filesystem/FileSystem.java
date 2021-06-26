package com.cauldronjs.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;

public interface FileSystem {
  Path getPath(String path, String... parts) throws AccessDeniedException;
  File getFile(String filename) throws AccessDeniedException;
  String[] readLines(String filename) throws IOException, AccessDeniedException, FileNotFoundException;
  String readString(String filename) throws IOException, AccessDeniedException, FileNotFoundException;
  byte[] read(String filename) throws IOException, AccessDeniedException, FileNotFoundException;
  void write(String filename, String content) throws AccessDeniedException, IOException;
  void write(String filename, String content, int position) throws AccessDeniedException, IOException;
  void write(String filename, byte[] content) throws AccessDeniedException, IOException;
  void append(String filename, String content) throws AccessDeniedException, IOException;
}
