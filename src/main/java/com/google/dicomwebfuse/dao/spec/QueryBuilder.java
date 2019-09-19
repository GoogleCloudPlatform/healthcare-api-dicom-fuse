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
import java.util.Objects;

public class QueryBuilder {

  private CloudConf cloudConf;
  private String dicomStoreId;
  private String studyId;
  private String seriesId;
  private String instanceId;
  private Path instanceDataPath;
  private DicomPath dicomPath;
  private Integer offset = 0;

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

  public QueryBuilder setOffset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public CloudConf getCloudConf() {
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

  public String getInstanceId() {
    return instanceId;
  }

  public Path getInstanceDataPath() {
    return instanceDataPath;
  }

  public DicomPath getDicomPath() {
    return dicomPath;
  }

  public Integer getOffset() {
    return offset;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueryBuilder that = (QueryBuilder) o;
    return Objects.equals(cloudConf, that.cloudConf) &&
        Objects.equals(dicomStoreId, that.dicomStoreId) &&
        Objects.equals(studyId, that.studyId) &&
        Objects.equals(seriesId, that.seriesId) &&
        Objects.equals(instanceId, that.instanceId) &&
        Objects.equals(instanceDataPath, that.instanceDataPath) &&
        Objects.equals(dicomPath, that.dicomPath) &&
        Objects.equals(offset, that.offset);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(cloudConf, dicomStoreId, studyId, seriesId, instanceId, instanceDataPath, dicomPath,
            offset);
  }
}
