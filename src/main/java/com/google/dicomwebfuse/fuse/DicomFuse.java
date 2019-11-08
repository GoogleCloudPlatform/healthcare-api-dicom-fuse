// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.dicomwebfuse.fuse;

import static jnr.ffi.Platform.OS.DARWIN;
import static jnr.ffi.Platform.OS.LINUX;
import static jnr.ffi.Platform.OS.WINDOWS;

import com.google.dicomwebfuse.entities.DicomPath;
import com.google.dicomwebfuse.entities.DicomPathLevel;
import com.google.dicomwebfuse.entities.cache.Cache;
import com.google.dicomwebfuse.exception.DicomFuseException;
import com.google.dicomwebfuse.fuse.cacher.DicomPathCacher;
import jnr.ffi.Platform.OS;
import jnr.ffi.Pointer;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;
import ru.serce.jnrfuse.struct.Statvfs;

public class DicomFuse extends FuseStubFS {

  private static final Logger LOGGER = LogManager.getLogger();
  private final DicomFuseHelper dicomFuseHelper;
  private final Parameters parameters;
  private final DicomPathParser dicomPathParser;
  private final OS os;

  public DicomFuse(Parameters parameters) {
    this.parameters = parameters;
    DicomPathCacher dicomPathCacher = new DicomPathCacher();
    Cache cache = new Cache();
    dicomFuseHelper = new DicomFuseHelper(parameters, dicomPathCacher, cache);
    dicomPathParser = new DicomPathParser(dicomPathCacher);
    os = parameters.getOs();
  }

  @Override
  public int getattr(String path, FileStat fileStat) {
    LOGGER.debug("getattr " + path);
    DicomPath dicomPath;
    try {
      dicomFuseHelper.checkPath(path);
      dicomPath = dicomPathParser.parsePath(path);
    } catch (DicomFuseException e) {
      return -ErrorCodes.ENOENT();
    }
    try {
      dicomFuseHelper.checkExistingObject(dicomPath);
      dicomFuseHelper.setAttr(dicomPath, this, fileStat);
    } catch (DicomFuseException e) {
      LOGGER.debug("getattr error", e);
      return -ErrorCodes.ENOENT();
    }
    return 0;
  }

  @Override
  public int readdir(String path, Pointer buf, FuseFillDir filler, @off_t long offset,
      FuseFileInfo fi) {
    LOGGER.debug("readdir " + path);
    filler.apply(buf, ".", null, 0); // add default folder
    filler.apply(buf, "..", null, 0); // add default folder
    try {
      DicomPath dicomPath = dicomPathParser.parsePath(path);
      dicomFuseHelper.fillFolder(dicomPath, buf, filler);
    } catch (DicomFuseException e) {
      LOGGER.error("readdir error", e);
      return -ErrorCodes.ENOENT();
    }
    return 0;
  }

  @Override
  public int opendir(String path, FuseFileInfo fi) {
    LOGGER.debug("opendir " + path);
    try {
      DicomPath dicomPath = dicomPathParser.parsePath(path);
      dicomFuseHelper.updateDir(dicomPath);
    } catch (DicomFuseException e) {
      LOGGER.error("opendir error", e);
      return -ErrorCodes.ENOENT();
    }
    return 0;
  }

  @Override
  public int read(String path, Pointer buf, @size_t long size, @off_t long offset,
      FuseFileInfo fi) {
    try {
      DicomPath dicomPath = dicomPathParser.parsePath(path);
      return dicomFuseHelper.readInstance(dicomPath, buf, (int) size, offset);
    } catch (DicomFuseException e) {
      LOGGER.error("read error", e);
      return -ErrorCodes.EIO();
    }
  }

  @Override
  public int write(String path, Pointer buf, long size, long offset, FuseFileInfo fi) {
    try {
      DicomPath dicomPath = dicomPathParser.parsePath(path);
      return dicomFuseHelper.writeInstance(dicomPath, buf, (int) size, offset);
    } catch (DicomFuseException e) {
      LOGGER.error("write error", e);
      return -ErrorCodes.EIO();
    }
  }

  @Override
  public int open(String path, FuseFileInfo fi) {
    LOGGER.debug("open " + path);
    try {
      DicomPath dicomPath = dicomPathParser.parsePath(path);
      dicomFuseHelper.cacheInstanceData(dicomPath);
    } catch (DicomFuseException e) {
      LOGGER.error("open error", e);
      return -ErrorCodes.EIO();
    }
    return 0;
  }

  @Override
  public int flush(String path, FuseFileInfo fi) {
    LOGGER.debug("flush " + path);
    try {
      dicomFuseHelper.checkPath(path);
    } catch (DicomFuseException e) {
      LOGGER.debug(e);
      return -ErrorCodes.ENOENT();
    }
    try {
      DicomPath dicomPath = dicomPathParser.parsePath(path);
      dicomFuseHelper.flushInstance(dicomPath);
    } catch (DicomFuseException e) {
      LOGGER.error("flush error", e);
      if (os == LINUX) {
        // "Remote I/O error" in Linux but in macOS "Unknown error: 121"
        return -ErrorCodes.EREMOTEIO();
      } else {
        // "Input/output error" in macOS
        return -ErrorCodes.EIO();
      }
    }
    return 0;
  }

  @Override
  public int create(String path, long mode, FuseFileInfo fi) {
    LOGGER.debug("create " + path);
    try {
      dicomFuseHelper.checkPath(path);
    } catch (DicomFuseException e) {
      LOGGER.debug(e);
      return -ErrorCodes.ENOENT();
    }
    try {
      DicomPath dicomPath = dicomPathParser.parsePath(path, Command.CREATE);
      dicomFuseHelper.createTemporaryInstance(dicomPath);
    } catch (DicomFuseException e) {
      LOGGER.error("create error", e);
      return -ErrorCodes.EIO();
    }
    return 0;
  }

  @Override
  public int unlink(String path) {
    LOGGER.debug("unlink " + path);
    if (parameters.isEnableDeletion()) {
      try {
        dicomFuseHelper.checkPath(path);
      } catch (DicomFuseException e) {
        LOGGER.debug(e);
        return -ErrorCodes.ENOENT();
      }
      try {
        DicomPath dicomPath = dicomPathParser.parsePath(path);
        // Before creating a file in macOS, unlink method will be called for checking the existence
        // of the file
        if (dicomPath.getDicomPathLevel() != DicomPathLevel.INSTANCE) {
          return -ErrorCodes.ENOENT();
        }
        dicomFuseHelper.unlinkInstance(dicomPath);
      } catch (DicomFuseException e) {
        LOGGER.error("unlink error", e);
        return -ErrorCodes.EIO();
      }
    }
    return 0;
  }

  @Override
  public int mkdir(String path, long mode) {
    LOGGER.debug("mkdir " + path);
    try {
      DicomPath dicomPath = dicomPathParser.parsePath(path);
      dicomFuseHelper.createDicomStoreInDataset(dicomPath);
    } catch (DicomFuseException e) {
      LOGGER.error("mkdir error", e);
      return -ErrorCodes.EPERM();
    }
    return 0;
  }

  @Override
  public int statfs(String path, Statvfs stbuf) {
    if (os == WINDOWS || os == DARWIN) {
      if ("/".equals(path)) {
        stbuf.f_bsize.set(4096); // file system block size
        stbuf.f_frsize.set(1024 * 1024); // fragment size
        stbuf.f_blocks.set(1024 * 1024); // total data blocks in file system
        stbuf.f_bfree.set(1024 * 1024); // free blocks in fs
        stbuf.f_bavail.set(1024 * 1024); // free blocks for non-root
      }
    }
    return super.statfs(path, stbuf);
  }

  // When auto_xattr option used may cause the error in the terminal - "Could not copy extended
  // attributes. Operation not permitted". Instead auto_xattr option, these methods were implemented
  // but do nothing.
  // See: https://github.com/osxfuse/osxfuse/issues/363
  @Override
  public int setxattr(String path, String name, Pointer value, long size, int flags) {
    return super.setxattr(path, name, value, size, flags);
  }

  @Override
  public int getxattr(String path, String name, Pointer value, long size) {
    return super.getxattr(path, name, value, size);
  }

  @Override
  public int listxattr(String path, Pointer list, long size) {
    return super.listxattr(path, list, size);
  }

  @Override
  public int removexattr(String path, String name) {
    return super.removexattr(path, name);
  }

  // methods do nothing, but needs to be implemented for correct work some programs
  @Override
  public int truncate(String path, long size) {
    return super.truncate(path, size);
  }

  @Override
  public int rename(String oldpath, String newpath) {
    return super.rename(oldpath, newpath);
  }

  @Override
  public int chown(String path, long uid, long gid) {
    return super.chown(path, uid, gid);
  }

  @Override
  public int chmod(String path, long mode) {
    return super.chmod(path, mode);
  }
}
