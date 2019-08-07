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

import java.util.Arrays;
import java.util.List;

class FuseConstants {

  static final String DCM_EXTENSION = ".dcm"; // Default extension
  static final List<String> LINUX_FORBIDDEN_PATHS = Arrays.asList(
      "Trash", // Ubuntu
      "hidden", // Ubuntu
      "autorun.inf", // Ubuntu
      ".xdg-volume-info" // Ubuntu
  );
  static final List<String> WINDOWS_FORBIDDEN_PATHS = Arrays.asList(
      "autorun.inf",
      "desktop.ini",
      "AutoRun.inf",
      ".jpg", // Windows 7
      ".gif" // Windows 7
  );
  static final List<String> MAC_OS_FORBIDDEN_PATHS = Arrays.asList(
      ".localized",
      "hidden",
      "icloud",
      "Contents",
      ".metadata_never_index",
      ".Spotlight-V100",
      ".ql_disablethumbnails",
      ".ql_disablecache"
  );
}
