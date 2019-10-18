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

import com.beust.jcommander.JCommander;
import com.google.dicomwebfuse.exception.DicomFuseException;
import com.google.dicomwebfuse.parser.Arguments;
import java.io.IOException;
import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {

  private static final Logger LOGGER = LogManager.getLogger();

  public static void main(String[] args) {

    Arguments arguments = new Arguments();
    JCommander jCommander = JCommander.newBuilder()
        .addObject(arguments)
        .resourceBundle(ResourceBundle.getBundle("cli-messages"))
        .programName("DICOMFuse")
        .build();
    jCommander.parse(args);

    if (arguments.help) {
      jCommander.usage();
      return;
    }

    try {
      AppMountProcess appMountProcess = new AppMountProcess(arguments);
      appMountProcess.startMountProcess();
    } catch (IOException | DicomFuseException e) {
      LOGGER.error("DICOMFuse error", e);
    }
  }
}
