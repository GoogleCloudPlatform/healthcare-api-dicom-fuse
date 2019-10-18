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

package com.google.dicomwebfuse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import jnr.ffi.Platform.OS;

class MountOptions {

  private static final Path USER_HOME = Paths.get(System.getProperty("user.home"));
  private final OS os;
  private final List<String> extraMountOptions;

  MountOptions(OS os, List<String> extraMountOptions) {
    this.os = os;
    this.extraMountOptions = extraMountOptions;
  }

  List<String> setMountOptions() throws IOException {
    // Adding all extra mount options if present
    ArrayList<String> mountOptions = new ArrayList<>(prepareOptions(extraMountOptions));
    if (os == OS.DARWIN || os == OS.LINUX) {
        mountOptions.add("-ouid=" + Files.getAttribute(USER_HOME, "unix:uid"));
        mountOptions.add("-ogid=" + Files.getAttribute(USER_HOME, "unix:gid"));
    }
    if (os == OS.LINUX) {
      // libfuse mount options. See: http://man7.org/linux/man-pages/man8/mount.fuse.8.html
      // Setting libfuse mount options
      // Setting filesystem name
      mountOptions.add("-ofsname=DICOMFuse");
      // Setting the timeout in seconds for which a negative lookup will be cached
      mountOptions.add("-onegative_timeout=4");
      // DICOMFuse has the internal cache, is disabled the external cache
      mountOptions.add("-oattr_timeout=0");
      mountOptions.add("-oac_attr_timeout=0");
      mountOptions.add("-oentry_timeout=0");
    }
    if (os == OS.WINDOWS) {
      // When starting DICOMFuse on Windows, to get mount options for WinFsp to add the
      // following options: --extraMountOptions --help
      // Also see: https://github.com/billziss-gh/winfsp/blob/master/src/dll/fuse/fuse.c
      // Setting WinFps mount options
      // Setting filesystem name
      mountOptions.add("-ofsname=DICOMFuse");
      // Setting the timeout in seconds for which a negative lookup will be cached
      mountOptions.add("-onegative_timeout=4");
      // DICOMFuse has the internal cache, is disabled the external cache
      mountOptions.add("-oattr_timeout=0");
      mountOptions.add("-oac_attr_timeout=0");
      mountOptions.add("-oentry_timeout=0");
      // Setting maximum thread count for Windows
      // See: https://github.com/billziss-gh/winfsp/commit/3902874ac93fe40685d9761f46a96358ba24f24c
      mountOptions.add("-oThreadCount=16");
    }
    if (os == OS.DARWIN) {
      // OSXFuse mount options. See: https://github.com/osxfuse/osxfuse/wiki/Mount-options
      // Setting timeout in seconds for which a negative lookup will be cached
      mountOptions.add("-onegative_timeout=4"); // in seconds
      // DICOMFuse has the internal cache, is disabled the external cache
      mountOptions.add("-onolocalcaches");
      // .DS_Store and ._ files are not used
      mountOptions.add("-onoappledouble");
      // Setting defer_permissions
      mountOptions.add("-odefer_permissions");
      // Setting volume name
      mountOptions.add("-ovolname=DICOMFuse");
    }
    return mountOptions;
  }

  private List<String> prepareOptions(List<String> options) {
    List<String> preparedOptions = new ArrayList<>();
    for (String option : options) {
      preparedOptions.add("-o" + option);
    }
    return preparedOptions;
  }
}
