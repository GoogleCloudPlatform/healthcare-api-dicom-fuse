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

import static org.junit.jupiter.api.Assertions.*;

import com.google.dicomwebfuse.entities.DicomPath;
import com.google.dicomwebfuse.entities.DicomPathLevel;
import com.google.dicomwebfuse.exception.DicomFuseException;
import com.google.dicomwebfuse.fuse.cacher.DicomPathCacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DicomPathParserTest {

  private static final String dicomStoreId = "DicomStore";
  private static final String studyInstanceUID = "111";
  private static final String seriesInstanceUID = "222";
  private static final String sopInstanceUID = "333";
  private static final String newDcmFile = "file1" + FuseConstants.DCM_EXTENSION;

  private DicomPathCacher dicomPathCacher;
  private DicomPathParser dicomPathParser;

  @BeforeEach
  void setup() {
    dicomPathCacher = new DicomPathCacher();
    dicomPathParser = new DicomPathParser(dicomPathCacher);
  }

  @Test
  void testParsePathShouldParseWhenRootPathProvided() throws DicomFuseException {
    // given
    final String datasetPath = "/";
    // when
    DicomPath actualDicomPath = dicomPathParser.parsePath(datasetPath);
    // then
    DicomPath expectedDicomPath = new DicomPath.Builder(DicomPathLevel.DATASET).build();
    assertEquals(actualDicomPath, expectedDicomPath);
  }

  @Test
  void testParsePathShouldParseWhenDicomStorePathProvided() throws DicomFuseException {
    // given
    String dicomStorePath = "/" + dicomStoreId;
    // when
    DicomPath actualDicomPath = dicomPathParser.parsePath(dicomStorePath);
    // then
    DicomPath expectedDicomPath = new DicomPath.Builder(DicomPathLevel.DICOM_STORE)
        .dicomStoreId(dicomStoreId)
        .build();
    assertEquals(actualDicomPath, expectedDicomPath);
  }

  @Test
  void testParsePathShouldParseWhenStudyPathProvided() throws DicomFuseException {
    // given
    String studyPath = "/" + dicomStoreId + "/" + studyInstanceUID;
    // when
    DicomPath actualDicomPath = dicomPathParser.parsePath(studyPath);
    // then
    DicomPath expectedDicomPath = new DicomPath.Builder(DicomPathLevel.STUDY)
        .dicomStoreId(dicomStoreId)
        .studyInstanceUID(studyInstanceUID)
        .build();
    assertEquals(actualDicomPath, expectedDicomPath);
  }

  @Test
  void testParsePathShouldParseWhenSeriesPathProvided() throws DicomFuseException {
    // given
    String seriesPath = "/" + dicomStoreId + "/" + studyInstanceUID + "/" + seriesInstanceUID;
    // when
    DicomPath actualDicomPath = dicomPathParser.parsePath(seriesPath);
    // then
    DicomPath expectedDicomPath = new DicomPath.Builder(DicomPathLevel.SERIES)
        .dicomStoreId(dicomStoreId)
        .studyInstanceUID(studyInstanceUID)
        .seriesInstanceUID(seriesInstanceUID)
        .build();
    assertEquals(actualDicomPath, expectedDicomPath);
  }

  @Test
  void testParsePathShouldParseWhenInstancePathProvided() throws DicomFuseException {
    // given
    String instanceDcmPath =
        "/" + dicomStoreId + "/" + studyInstanceUID + "/" + seriesInstanceUID + "/" + sopInstanceUID
            + FuseConstants.DCM_EXTENSION;
    // when
    DicomPath actualDicomPath = dicomPathParser.parsePath(instanceDcmPath);
    // then
    DicomPath expectedDicomPath = new DicomPath.Builder(DicomPathLevel.INSTANCE)
        .dicomStoreId(dicomStoreId)
        .studyInstanceUID(studyInstanceUID)
        .seriesInstanceUID(seriesInstanceUID)
        .sopInstanceUID(sopInstanceUID)
        .build();
    assertEquals(actualDicomPath, expectedDicomPath);
  }

  @Test
  void testParsePathShouldParseWhenNewDcmFileInDicomStoreCreated() throws DicomFuseException {
    // given
    String newDcmFileInDicomStore = "/" + dicomStoreId + "/" + newDcmFile;
    // when
    DicomPath actualDicomPath = dicomPathParser.parsePath(newDcmFileInDicomStore, Command.CREATE);
    // then
    DicomPath expectedDicomPath = new DicomPath.Builder(DicomPathLevel.TEMP_FILE_IN_DICOM_STORE)
        .dicomStoreId(dicomStoreId)
        .fileName(newDcmFile)
        .build();
    //checking that new file was cached in DicomPathCacher
    assertNotNull(dicomPathCacher.getDicomPath(newDcmFileInDicomStore));
    assertEquals(actualDicomPath, expectedDicomPath);
  }

  @Test
  void testParsePathShouldParseWhenNewDcmFileInDicomStoreProvided() throws DicomFuseException {
    // given
    String newDcmFileInDicomStore = "/" + dicomStoreId + "/" + newDcmFile;
    dicomPathParser.parsePath(newDcmFileInDicomStore, Command.CREATE);
    // when
    DicomPath actualDicomPath = dicomPathParser.parsePath(newDcmFileInDicomStore);
    // then
    DicomPath expectedDicomPath = new DicomPath.Builder(DicomPathLevel.TEMP_FILE_IN_DICOM_STORE)
        .dicomStoreId(dicomStoreId)
        .fileName(newDcmFile)
        .build();
    assertEquals(actualDicomPath, expectedDicomPath);
    dicomPathCacher.removeDicomPath(newDcmFileInDicomStore);
    //checking that new file was removed in DicomPathCacher
    assertNull(dicomPathCacher.getDicomPath(newDcmFileInDicomStore));
  }

  @Test
  void testParsePathShouldParseWhenNewDcmFileInSeriesCreated() throws DicomFuseException {
    // given
    String newDcmFileInSeries = "/" + dicomStoreId + "/" + studyInstanceUID + "/"
        + seriesInstanceUID + "/" + newDcmFile;
    // when
    DicomPath actualDicomPath = dicomPathParser.parsePath(newDcmFileInSeries, Command.CREATE);
    // then
    DicomPath expectedDicomPath = new DicomPath.Builder(DicomPathLevel.TEMP_FILE_IN_SERIES)
        .dicomStoreId(dicomStoreId)
        .studyInstanceUID(studyInstanceUID)
        .seriesInstanceUID(seriesInstanceUID)
        .fileName(newDcmFile)
        .build();
    //checking that new file was cached in DicomPathCacher
    assertNotNull(dicomPathCacher.getDicomPath(newDcmFileInSeries));
    assertEquals(actualDicomPath, expectedDicomPath);
  }

  @Test
  void testParsePathShouldParseWhenNewDcmFileInSeriesProvided() throws DicomFuseException {
    // given
    String newDcmFileInSeries = "/" + dicomStoreId + "/" + studyInstanceUID + "/"
        + seriesInstanceUID + "/" + newDcmFile;
    dicomPathParser.parsePath(newDcmFileInSeries, Command.CREATE);
    // when
    DicomPath actualDicomPath = dicomPathParser.parsePath(newDcmFileInSeries);
    // then
    DicomPath expectedDicomPath = new DicomPath.Builder(DicomPathLevel.TEMP_FILE_IN_SERIES)
        .dicomStoreId(dicomStoreId)
        .studyInstanceUID(studyInstanceUID)
        .seriesInstanceUID(seriesInstanceUID)
        .fileName(newDcmFile)
        .build();
    assertEquals(actualDicomPath, expectedDicomPath);
    dicomPathCacher.removeDicomPath(newDcmFileInSeries);
    //checking that new file was removed in DicomPathCacher
    assertNull(dicomPathCacher.getDicomPath(newDcmFileInSeries));
  }
}