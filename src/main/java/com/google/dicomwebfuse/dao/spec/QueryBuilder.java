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

import com.google.dicomwebfuse.entities.CloudConf;
import com.google.dicomwebfuse.entities.DicomPath;
import java.nio.file.Path;

public class QueryBuilder {

  private CloudConf cloudConf;
  private String dicomStoreId;
  private String studyId;
  private String seriesId;
  private String instanceId;
  private Path instanceDataPath;
  private DicomPath dicomPath;

  private QueryBuilder() {
  }

  public static QueryBuilder forConfiguration(CloudConf cloudConf) {
    QueryBuilder queryBuilder = new QueryBuilder();
    queryBuilder.cloudConf = cloudConf;
    return queryBuilder;
  }

  public QueryBuilder setDicomStoreId(String dicomStoreId) {
    this.dicomStoreId = dicomStoreId;
    return this;
  }

  public QueryBuilder setStudyId(String studyId) {
    this.studyId = studyId;
    return this;
  }

  public QueryBuilder setSeriesId(String seriesId) {
    this.seriesId = seriesId;
    return this;
  }

  public QueryBuilder setInstanceId(String instanceId) {
    this.instanceId = instanceId;
    return this;
  }

  public QueryBuilder setInstanceDataPath(Path instanceDataPath) {
    this.instanceDataPath = instanceDataPath;
    return this;
  }

  public QueryBuilder setDicomPath(DicomPath dicomPath) {
    this.dicomPath = dicomPath;
    return this;
  }

  CloudConf getCloudConf() {
    return cloudConf;
  }

  public String getDicomStoreId() {
    return dicomStoreId;
  }

  public String getStudyId() {
    return studyId;
  }

  public String getSeriesId() {
    return seriesId;
  }

  String getInstanceId() {
    return instanceId;
  }

  public Path getInstanceDataPath() {
    return instanceDataPath;
  }

  public DicomPath getDicomPath() {
    return dicomPath;
  }
}
