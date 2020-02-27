/*
 * MIT License
 *
 * Copyright (c) 2019 1619kHz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.aquiver.server.watcher;

import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

public class GlobalEnvObserver implements Runnable {

  private Path            watchDirectory;
  private FilenameFilter  filenameFilter;
  private WatcherListener watcherListener;

  public static GlobalEnvObserver config() {
    return new GlobalEnvObserver();
  }

  public GlobalEnvObserver watchPath(Path watchDirectory) {
    this.watchDirectory = watchDirectory;
    return this;
  }

  public GlobalEnvObserver filter(FilenameFilter filenameFilter) {
    this.filenameFilter = filenameFilter;
    return this;
  }

  public GlobalEnvObserver listener(WatcherListener watcherListener) {
    this.watcherListener = watcherListener;
    return this;
  }

  public void start() {
    Thread thread = new Thread(this);
    thread.setName("watcher@thrad");
    thread.start();
  }

  private void executeHistoryFiles() {
    try {
      Stream<Path> stream = Files.list(this.watchDirectory);
      if (this.filenameFilter != null) {
        stream = stream.filter(path -> {
          String fileName = path.getFileName().toString();
          return filenameFilter.accept(this.watchDirectory.toFile(), fileName);
        });
      }
      stream.forEach(path -> this.watcherListener.onCreate(path));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    this.executeHistoryFiles();
    try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
      this.watchDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
              StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
      while (true) {
        WatchKey watchKey = watchService.take();
        for (WatchEvent event : watchKey.pollEvents()) {
          WatchEvent.Kind eventKind = event.kind();
          if (eventKind == StandardWatchEventKinds.OVERFLOW) {
            continue;
          }
          String fileName = event.context().toString();
          if (this.filenameFilter != null && !this.filenameFilter.accept(this.watchDirectory.toFile(), fileName)) {
            continue;
          }
          Path file = Paths.get(this.watchDirectory.toString(), fileName);
          if (eventKind == StandardWatchEventKinds.ENTRY_CREATE) {
            this.watcherListener.onCreate(file);
          } else if (eventKind == StandardWatchEventKinds.ENTRY_MODIFY) {
            this.watcherListener.onModify(file);
          } else if (eventKind == StandardWatchEventKinds.ENTRY_DELETE) {
            this.watcherListener.onDelete(file);
          }
        }
        boolean isKeyValid = watchKey.reset();
        if (!isKeyValid) {
          break;
        }
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}