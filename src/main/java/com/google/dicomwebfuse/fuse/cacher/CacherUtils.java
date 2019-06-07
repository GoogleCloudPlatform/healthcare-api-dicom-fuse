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

package com.google.dicomwebfuse.fuse.cacher;

import com.google.dicomwebfuse.exception.DicomFuseException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class CacherUtils {

  static Path createTempPath() throws DicomFuseException {
    try {
      Path instanceDataPath = Files.createTempFile("temp-", ".dcm");
      instanceDataPath.toFile().deleteOnExit();
      return instanceDataPath;
    } catch (IOException e) {
      throw new DicomFuseException(e);
    }
  }

  static void deleteFile(Path path) throws DicomFuseException {
    try {
      Files.delete(path);
    } catch (IOException e) {
      throw new DicomFuseException("Error deleting file!");
    }
  }

  private CacherUtils() {
  }
}
