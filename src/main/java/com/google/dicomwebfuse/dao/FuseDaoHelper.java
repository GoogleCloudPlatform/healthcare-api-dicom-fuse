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

package com.google.dicomwebfuse.dao;

import com.google.dicomwebfuse.exception.DicomFuseException;
import com.google.dicomwebfuse.dao.spec.QueryBuilder;
import com.google.dicomwebfuse.entities.CloudConf;
import com.google.dicomwebfuse.entities.DicomPath;
import com.google.dicomwebfuse.entities.DicomStore;
import com.google.dicomwebfuse.entities.Instance;
import com.google.dicomwebfuse.entities.Series;
import com.google.dicomwebfuse.entities.Study;
import java.nio.file.Path;
import java.util.List;

public class FuseDaoHelper {

  public static List<DicomStore> getDicomStoresList(FuseDao fuseDAO, CloudConf cloudConf)
      throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf);
    return fuseDAO.getDicomStoresList(queryBuilder);
  }

  public static List<Study> getStudiesList(FuseDao fuseDAO, CloudConf cloudConf,
      DicomPath dicomPath) throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId());
    return fuseDAO.getStudiesList(queryBuilder);
  }

  public static List<Series> getSeriesList(FuseDao fuseDAO, CloudConf cloudConf,
      DicomPath dicomPath) throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId())
        .setStudyId(dicomPath.getStudyInstanceUID());
    return fuseDAO.getSeriesList(queryBuilder);
  }

  public static List<Instance> getInstancesList(FuseDao fuseDAO, CloudConf cloudConf,
      DicomPath dicomPath) throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId())
        .setStudyId(dicomPath.getStudyInstanceUID())
        .setSeriesId(dicomPath.getSeriesInstanceUID());
    return fuseDAO.getInstancesList(queryBuilder);
  }

  public static void downloadInstanceToTempFile(FuseDao fuseDAO, CloudConf cloudConf,
      DicomPath dicomPath, Path instanceDataPath) throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId())
        .setStudyId(dicomPath.getStudyInstanceUID())
        .setSeriesId(dicomPath.getSeriesInstanceUID())
        .setInstanceId(dicomPath.getSopInstanceUID())
        .setInstanceDataPath(instanceDataPath);
    fuseDAO.downloadInstanceToTempFile(queryBuilder);
  }

  public static void uploadInstance(FuseDao fuseDAO, CloudConf cloudConf, DicomPath dicomPath,
      Path instanceDataPath) throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId())
        .setInstanceDataPath(instanceDataPath)
        .setDicomPath(dicomPath);
    fuseDAO.uploadInstance(queryBuilder);
  }

  public static void deleteInstance(FuseDao fuseDAO, CloudConf cloudConf, DicomPath dicomPath)
      throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId())
        .setStudyId(dicomPath.getStudyInstanceUID())
        .setSeriesId(dicomPath.getSeriesInstanceUID())
        .setInstanceId(dicomPath.getSopInstanceUID());
    fuseDAO.deleteInstance(queryBuilder);
  }

  private FuseDaoHelper() {
  }
}
