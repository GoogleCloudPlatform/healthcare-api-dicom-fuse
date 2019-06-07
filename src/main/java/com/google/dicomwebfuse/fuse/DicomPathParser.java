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

import static com.google.dicomwebfuse.fuse.FuseConstants.DCM_EXTENSION;

import com.google.dicomwebfuse.fuse.cacher.DicomPathCacher;
import com.google.dicomwebfuse.entities.DicomPath;
import com.google.dicomwebfuse.entities.DicomPathLevel;
import com.google.dicomwebfuse.exception.DicomFuseException;

class DicomPathParser {

  private final DicomPathCacher dicomPathCacher;

  DicomPathParser(DicomPathCacher dicomPathCacher) {
    this.dicomPathCacher = dicomPathCacher;
  }

  DicomPath parsePath(String path) throws DicomFuseException {
    return parsePath(path, null);
  }

  DicomPath parsePath(String path, Command command) throws DicomFuseException {
    String partOfPath = path.substring(1);
    if (partOfPath.length() == 0) {
      return new DicomPath.Builder(DicomPathLevel.DATASET).build();
    }
    String[] pathREST = partOfPath.split("/");
    int pathRESTLength = pathREST.length;
    DicomPath dicomPath;
    DicomPath tempDicomPath;
    switch (pathRESTLength) {
      case 1:
        dicomPath = new DicomPath.Builder(DicomPathLevel.DICOM_STORE)
            .dicomStoreId(pathREST[0])
            .build();
        break;
      case 2:
        if (command == Command.CREATE) {
          String fileName = pathREST[1];
          dicomPath = new DicomPath.Builder(DicomPathLevel.TEMP_FILE_IN_DICOM_STORE)
              .dicomStoreId(pathREST[0])
              .fileName(fileName)
              .build();
          dicomPathCacher.putDicomPath(path, dicomPath);
          return dicomPath;
        }
        tempDicomPath = dicomPathCacher.getDicomPath(path);
        if (tempDicomPath != null) {
          dicomPath = tempDicomPath;
        } else {
          dicomPath = new DicomPath.Builder(DicomPathLevel.STUDY)
              .dicomStoreId(pathREST[0])
              .studyInstanceUID(pathREST[1])
              .build();
        }
        break;
      case 3:
        dicomPath = new DicomPath.Builder(DicomPathLevel.SERIES)
            .dicomStoreId(pathREST[0])
            .studyInstanceUID(pathREST[1])
            .seriesInstanceUID(pathREST[2])
            .build();
        break;
      case 4:
        String fileName = pathREST[3];
        if (command == Command.CREATE) {
          dicomPath = new DicomPath.Builder(DicomPathLevel.TEMP_FILE_IN_SERIES)
              .dicomStoreId(pathREST[0])
              .studyInstanceUID(pathREST[1])
              .seriesInstanceUID(pathREST[2])
              .fileName(fileName)
              .build();
          dicomPathCacher.putDicomPath(path, dicomPath);
          return dicomPath;
        }
        tempDicomPath = dicomPathCacher.getDicomPath(path);
        if (tempDicomPath != null) {
          dicomPath = tempDicomPath;
        } else {
          int fileNameLength = fileName.length();
          int sopInstanceUIDLenth = fileNameLength;
          if (fileName.contains(DCM_EXTENSION)) {
            sopInstanceUIDLenth = fileNameLength - DCM_EXTENSION.length();
          }
          String sopInstanceUID = fileName.substring(0, sopInstanceUIDLenth);
          dicomPath = new DicomPath.Builder(DicomPathLevel.INSTANCE)
              .dicomStoreId(pathREST[0])
              .studyInstanceUID(pathREST[1])
              .seriesInstanceUID(pathREST[2])
              .sopInstanceUID(sopInstanceUID)
              .build();
        }
        break;
      default:
        throw new DicomFuseException("Error parsing path!");
    }
    return dicomPath;
  }
}
