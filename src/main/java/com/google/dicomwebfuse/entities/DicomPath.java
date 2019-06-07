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

package com.google.dicomwebfuse.entities;

import java.util.Objects;

public class DicomPath {

  private static final String DCM_EXTENSION = ".dcm";
  private final DicomPathLevel dicomPathLevel;
  private final String dicomStoreId;
  private final String studyInstanceUID;
  private final String seriesInstanceUID;
  private final String sopInstanceUID;
  private final String fileName;


  public static class Builder {

    private final DicomPathLevel dicomPathLevel;
    private String dicomStoreId = "";
    private String studyInstanceUID = "";
    private String seriesInstanceUID = "";
    private String sopInstanceUID = "";
    private String fileName = "";

    public Builder(DicomPathLevel dicomPathLevel) {
      this.dicomPathLevel = dicomPathLevel;
    }

    public Builder dicomStoreId(String dicomStoreId) {
      this.dicomStoreId = dicomStoreId;
      return this;
    }

    public Builder studyInstanceUID(String studyInstanceUID) {
      this.studyInstanceUID = studyInstanceUID;
      return this;
    }

    public Builder seriesInstanceUID(String seriesInstanceUID) {
      this.seriesInstanceUID = seriesInstanceUID;
      return this;
    }

    public Builder sopInstanceUID(String sopInstanceUID) {
      this.sopInstanceUID = sopInstanceUID;
      return this;
    }

    public Builder fileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    public DicomPath build() {
      return new DicomPath(this);
    }
  }

  private DicomPath(Builder builder) {
    dicomPathLevel = builder.dicomPathLevel;
    dicomStoreId = builder.dicomStoreId;
    studyInstanceUID = builder.studyInstanceUID;
    seriesInstanceUID = builder.seriesInstanceUID;
    sopInstanceUID = builder.sopInstanceUID;
    fileName = builder.fileName;
  }

  public DicomPathLevel getDicomPathLevel() {
    return dicomPathLevel;
  }

  public String getDicomStoreId() {
    return dicomStoreId;
  }

  public String getStudyInstanceUID() {
    return studyInstanceUID;
  }

  public String getSeriesInstanceUID() {
    return seriesInstanceUID;
  }

  public String getSopInstanceUID() {
    return sopInstanceUID;
  }

  public String getFileName() {
    return fileName;
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    switch (dicomPathLevel) {
      case DATASET:
        break;
      case DICOM_STORE:
        stringBuilder.append(dicomStoreId);
        break;
      case STUDY:
        stringBuilder.append(dicomStoreId);
        stringBuilder.append("/");
        stringBuilder.append(studyInstanceUID);
        break;
      case SERIES:
        stringBuilder.append(dicomStoreId);
        stringBuilder.append("/");
        stringBuilder.append(studyInstanceUID);
        stringBuilder.append("/");
        stringBuilder.append(seriesInstanceUID);
        break;
      case INSTANCE:
        stringBuilder.append(dicomStoreId);
        stringBuilder.append("/");
        stringBuilder.append(studyInstanceUID);
        stringBuilder.append("/");
        stringBuilder.append(seriesInstanceUID);
        stringBuilder.append("/");
        stringBuilder.append(sopInstanceUID);
        stringBuilder.append(DCM_EXTENSION);
        break;
      case TEMP_FILE_IN_DICOM_STORE:
        stringBuilder.append(dicomStoreId);
        stringBuilder.append("/");
        stringBuilder.append(fileName);
        break;
      case TEMP_FILE_IN_SERIES:
        stringBuilder.append(dicomStoreId);
        stringBuilder.append("/");
        stringBuilder.append(studyInstanceUID);
        stringBuilder.append("/");
        stringBuilder.append(seriesInstanceUID);
        stringBuilder.append("/");
        stringBuilder.append(fileName);
        break;
      default:
    }
    return stringBuilder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DicomPath dicomPath = (DicomPath) o;
    return dicomPathLevel == dicomPath.dicomPathLevel &&
        Objects.equals(dicomStoreId, dicomPath.dicomStoreId) &&
        Objects.equals(studyInstanceUID, dicomPath.studyInstanceUID) &&
        Objects.equals(seriesInstanceUID, dicomPath.seriesInstanceUID) &&
        Objects.equals(sopInstanceUID, dicomPath.sopInstanceUID) &&
        Objects.equals(fileName, dicomPath.fileName);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(dicomPathLevel, dicomStoreId, studyInstanceUID, seriesInstanceUID, sopInstanceUID,
            fileName);
  }
}
