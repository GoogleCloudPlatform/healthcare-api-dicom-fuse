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

package com.google.dicomwebfuse.dao.spec;

import static com.google.dicomwebfuse.dao.Constants.DATASETS;
import static com.google.dicomwebfuse.dao.Constants.DICOM_STORES;
import static com.google.dicomwebfuse.dao.Constants.DICOM_WEB;
import static com.google.dicomwebfuse.dao.Constants.INSTANCES;
import static com.google.dicomwebfuse.dao.Constants.LOCATIONS;
import static com.google.dicomwebfuse.dao.Constants.PROJECTS;
import static com.google.dicomwebfuse.dao.Constants.SERIES;
import static com.google.dicomwebfuse.dao.Constants.STUDIES;

import com.google.dicomwebfuse.exception.DicomFuseException;

public class InstancePathBuilder implements PathBuilder {

  private QueryBuilder queryBuilder;

  public InstancePathBuilder(QueryBuilder queryBuilder) {
    this.queryBuilder = queryBuilder;
  }

  @Override
  public String toPath() throws DicomFuseException {
    String stage = queryBuilder.getCloudConf().getStage();
    if (stage == null) {
      throw new DicomFuseException("Stage must not be null!");
    }
    String project = queryBuilder.getCloudConf().getProject();
    if (project == null) {
      throw new DicomFuseException("Project must not be null!");
    }
    String location = queryBuilder.getCloudConf().getLocation();
    if (location == null) {
      throw new DicomFuseException("Location must not be null!");
    }
    String dataset = queryBuilder.getCloudConf().getDataSet();
    if (dataset == null) {
      throw new DicomFuseException("Dataset must not be null!");
    }
    String dicomStoreId = queryBuilder.getDicomStoreId();
    if (dicomStoreId == null) {
      throw new DicomFuseException("Dicom store must not be null!");
    }
    String studyId = queryBuilder.getStudyId();
    if (studyId == null) {
      throw new DicomFuseException("Study must not be null!");
    }
    String seriesId = queryBuilder.getSeriesId();
    if (seriesId == null) {
      throw new DicomFuseException("Series must not be null!");
    }
    String instanceId = queryBuilder.getInstanceId();
    if (instanceId == null) {
      throw new DicomFuseException("Instance must not be null!");
    }
    return stage + PROJECTS + project + LOCATIONS + location + DATASETS + dataset + DICOM_STORES +
        dicomStoreId + DICOM_WEB + STUDIES + studyId + SERIES + seriesId + INSTANCES +
        instanceId;
  }
}