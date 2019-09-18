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

import com.google.dicomwebfuse.auth.AuthAdc;
import com.google.dicomwebfuse.dao.FuseDao;
import com.google.dicomwebfuse.dao.FuseDaoImpl;
import com.google.dicomwebfuse.dao.http.HttpClientFactory;
import com.google.dicomwebfuse.dao.http.HttpClientFactoryImpl;
import com.google.dicomwebfuse.exception.DicomFuseException;
import com.google.dicomwebfuse.fuse.DicomFuse;
import com.google.dicomwebfuse.fuse.Parameters;
import com.google.dicomwebfuse.fuse.AccessChecker;
import com.google.dicomwebfuse.parser.Arguments;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import jnr.ffi.Platform;
import jnr.ffi.Platform.OS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class AppMountProcess {

  private static final Logger LOGGER = LogManager.getLogger();
  private static final Path USER_HOME = Paths.get(System.getProperty("user.home"));

  void startMountProcess(Arguments arguments) {
    AuthAdc authADC = new AuthAdc();
    try {
      Path keyPath = arguments.keyPath;
      if (keyPath == null) {
        authADC.createCredentials();
      } else {
        authADC.createCredentials(keyPath);
      }
    } catch (IOException e) {
      LOGGER.error("AuthAdc error! ", e);
      return;
    }

    HttpClientFactory httpClientFactory = new HttpClientFactoryImpl();
    FuseDao fuseDAO = new FuseDaoImpl(authADC, httpClientFactory);
    OS os = Platform.getNativePlatform().getOS();
    Parameters parameters = new Parameters(fuseDAO, arguments, os);
    DicomFuse dicomFuse = new DicomFuse(parameters);

    AccessChecker accessChecker = new AccessChecker(parameters);
    try {
      accessChecker.check();
    } catch (DicomFuseException e) {
      if (e.getStatusCode() == 403) {
        LOGGER.error("Please check your Project name, Location, Dataset name in "
            + "--datasetAddr argument. Check that Project and Dataset exist. Also, check the role "
            + "for current account service key. The role should be Healthcare DICOM Editor.", e);
      } else {
        LOGGER.error("AccessChecker error!", e);
      }
      return;
    }

    try {
      ArrayList<String> mountOptions = new ArrayList<>();
      if (os == OS.DARWIN || os == OS.LINUX) {
        try {
          mountOptions.add("-ouid=" + Files.getAttribute(USER_HOME, "unix:uid"));
          mountOptions.add("-ogid=" + Files.getAttribute(USER_HOME, "unix:gid"));
        } catch (IOException e) {
          LOGGER.error("Set uid, gid error! ", e);
          return;
        }
      }
      if (os == OS.DARWIN) {
        // set OSXFuse mount options see: https://github.com/osxfuse/osxfuse/wiki/Mount-options
        // set the timeout in seconds for which a negative lookup will be cached
        mountOptions.add("-onegative_timeout=4"); // in seconds
        // because DICOMFuse has the internal cache, is disabled the external cache
        mountOptions.add("-onolocalcaches");
        // .DS_Store and ._ files are not used
        mountOptions.add("-onoappledouble");
        // set defer_permissions
        mountOptions.add("-odefer_permissions");
        // set name
        mountOptions.add("-ovolname=DICOMFuse");
      }
      if (os == OS.WINDOWS || os == OS.LINUX) {
        // set libfuse and WinFps mount options
        // set filesystem name
        mountOptions.add("-ofsname=DicomFuse");
        //set the timeout in seconds for which a negative lookup will be cached
        mountOptions.add("-onegative_timeout=4");
        // because DICOMFuse has the internal cache, is disabled the external cache
        mountOptions.add("-oattr_timeout=0");
        mountOptions.add("-oac_attr_timeout=0");
        mountOptions.add("-oentry_timeout=0");
      }
      String[] opt = mountOptions.toArray(new String[mountOptions.size()]);
      // mount DICOMFuse with options
      dicomFuse.mount(arguments.mountPath, true, false, opt);
    } finally {
      dicomFuse.umount();
    }
  }
}
