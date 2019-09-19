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

import static com.google.dicomwebfuse.dao.Constants.MAX_INSTANCES_IN_SERIES;
import static com.google.dicomwebfuse.dao.Constants.MAX_SERIES_IN_STUDY;
import static com.google.dicomwebfuse.dao.Constants.MAX_STUDIES_IN_DICOM_STORE;
import static com.google.dicomwebfuse.dao.Constants.THREAD_COUNT;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_MAX_LIMIT_FOR_SERIES;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_MAX_LIMIT_FOR_STUDY;

import com.google.dicomwebfuse.exception.DicomFuseException;
import com.google.dicomwebfuse.dao.spec.QueryBuilder;
import com.google.dicomwebfuse.entities.CloudConf;
import com.google.dicomwebfuse.entities.DicomPath;
import com.google.dicomwebfuse.entities.DicomStore;
import com.google.dicomwebfuse.entities.Instance;
import com.google.dicomwebfuse.entities.Series;
import com.google.dicomwebfuse.entities.Study;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FuseDaoHelper {

  private static final Logger LOGGER = LogManager.getLogger();

  public static List<DicomStore> getAllDicomStores(FuseDao fuseDao, CloudConf cloudConf)
      throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf);
    return fuseDao.getAllDicomStores(queryBuilder);
  }

  public static List<Study> getStudies(FuseDao fuseDao, CloudConf cloudConf,
      DicomPath dicomPath) throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId());
    List<Study> studyList = fuseDao.getStudies(queryBuilder);

    if (studyList.size() == VALUE_PARAM_MAX_LIMIT_FOR_STUDY) {
      ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
      List<Future<List<Study>>> futureList = new ArrayList<>();

      int iterationCount = (int) Math
          .ceil((double) MAX_STUDIES_IN_DICOM_STORE / VALUE_PARAM_MAX_LIMIT_FOR_STUDY);
      for (int offset = VALUE_PARAM_MAX_LIMIT_FOR_STUDY, i = 0; i < iterationCount;
          i++, offset += VALUE_PARAM_MAX_LIMIT_FOR_STUDY) {
        QueryBuilder queryBuilderCallable = QueryBuilder.forConfiguration(cloudConf)
            .setDicomStoreId(dicomPath.getDicomStoreId())
            .setOffset(offset);
        Callable<List<Study>> callable = () -> fuseDao.getStudies(queryBuilderCallable);
        Future<List<Study>> future = executorService.submit(callable);
        futureList.add(future);
      }
      for (Future<List<Study>> future : futureList) {
        try {
          studyList.addAll(future.get());
        } catch (InterruptedException | ExecutionException e) {
          throw new DicomFuseException(e);
        }
      }
      executorService.shutdown();
    }

    if (studyList.size() > MAX_STUDIES_IN_DICOM_STORE) {
      LOGGER.warn("DICOM Store {} has more than {} studies in it, so only showing the first {}",
          dicomPath.getDicomStoreId(), MAX_STUDIES_IN_DICOM_STORE, MAX_STUDIES_IN_DICOM_STORE);
      studyList = studyList.subList(0, MAX_STUDIES_IN_DICOM_STORE);
    }

    return studyList;
  }

  public static Study getSingleStudy(FuseDao fuseDao, CloudConf cloudConf,
      DicomPath dicomPath) throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId()).setStudyId(dicomPath.getStudyInstanceUID());
    Study study = fuseDao.getSingleStudy(queryBuilder);

    return study;
  }

  public static List<Series> getSeries(FuseDao fuseDao, CloudConf cloudConf,
      DicomPath dicomPath) throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId())
        .setStudyId(dicomPath.getStudyInstanceUID());
    List<Series> seriesList = fuseDao.getSeries(queryBuilder);

    if (seriesList.size() == VALUE_PARAM_MAX_LIMIT_FOR_SERIES) {
      ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
      List<Future<List<Series>>> futureList = new ArrayList<>();

      int iterationCount = (int) Math
          .ceil((double) MAX_SERIES_IN_STUDY / VALUE_PARAM_MAX_LIMIT_FOR_SERIES);
      for (int offset = VALUE_PARAM_MAX_LIMIT_FOR_SERIES, i = 0; i < iterationCount;
          i++, offset += VALUE_PARAM_MAX_LIMIT_FOR_SERIES) {
        QueryBuilder queryBuilderCallable = QueryBuilder.forConfiguration(cloudConf)
            .setDicomStoreId(dicomPath.getDicomStoreId())
            .setStudyId(dicomPath.getStudyInstanceUID())
            .setOffset(offset);
        Callable<List<Series>> callable = () -> fuseDao.getSeries(queryBuilderCallable);
        Future<List<Series>> future = executorService.submit(callable);
        futureList.add(future);
      }
      for (Future<List<Series>> future : futureList) {
        try {
          seriesList.addAll(future.get());
        } catch (InterruptedException | ExecutionException e) {
          throw new DicomFuseException(e);
        }
      }
      executorService.shutdown();
    }

    if (seriesList.size() > MAX_SERIES_IN_STUDY) {
      LOGGER.warn("DICOM Study {} has more than {} series in it, so only showing the first {}",
          dicomPath.getStudyInstanceUID(), MAX_SERIES_IN_STUDY, MAX_SERIES_IN_STUDY);
      seriesList = seriesList.subList(0, MAX_SERIES_IN_STUDY);
    }

    return seriesList;
  }


  public static Series getSingleSeries(FuseDao fuseDao, CloudConf cloudConf,
      DicomPath dicomPath) throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId()).setStudyId(dicomPath.getStudyInstanceUID())
        .setSeriesId(dicomPath.getSeriesInstanceUID());
    Series study = fuseDao.getSingleSeries(queryBuilder);

    return study;
  }

  public static List<Instance> getInstances(FuseDao fuseDao, CloudConf cloudConf,
      DicomPath dicomPath) throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId())
        .setStudyId(dicomPath.getStudyInstanceUID())
        .setSeriesId(dicomPath.getSeriesInstanceUID());
    List<Instance> instancesList = fuseDao.getInstances(queryBuilder);

    if (instancesList.size() == VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES) {
      QueryBuilder secondRequestQueryBuilder = QueryBuilder.forConfiguration(cloudConf)
          .setDicomStoreId(dicomPath.getDicomStoreId())
          .setStudyId(dicomPath.getStudyInstanceUID())
          .setSeriesId(dicomPath.getSeriesInstanceUID())
          .setOffset(VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES);
      List<Instance> resultInstancesList = fuseDao.getInstances(secondRequestQueryBuilder);
      instancesList.addAll(resultInstancesList);
    }
    if (instancesList.size() > MAX_INSTANCES_IN_SERIES) {
      LOGGER.warn("DICOM Series {} has more than {} instances in it, so only showing the first {}",
          dicomPath.getStudyInstanceUID(), MAX_INSTANCES_IN_SERIES, MAX_INSTANCES_IN_SERIES);
      instancesList = instancesList.subList(0, MAX_INSTANCES_IN_SERIES);
    }
    return instancesList;
  }

  public static Instance getSingleInstance(FuseDao fuseDao, CloudConf cloudConf,
      DicomPath dicomPath) throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId())
        .setStudyId(dicomPath.getStudyInstanceUID())
        .setSeriesId(dicomPath.getSeriesInstanceUID())
        .setInstanceId(dicomPath.getSopInstanceUID());
    Instance instance = fuseDao.getSingleInstance(queryBuilder);
    return instance;
  }

  public static void downloadInstance(FuseDao fuseDao, CloudConf cloudConf,
      DicomPath dicomPath, Path instanceDataPath) throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId())
        .setStudyId(dicomPath.getStudyInstanceUID())
        .setSeriesId(dicomPath.getSeriesInstanceUID())
        .setInstanceId(dicomPath.getSopInstanceUID())
        .setInstanceDataPath(instanceDataPath);
    fuseDao.downloadInstance(queryBuilder);
  }

  public static void uploadInstance(FuseDao fuseDao, CloudConf cloudConf, DicomPath dicomPath,
      Path instanceDataPath) throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId())
        .setInstanceDataPath(instanceDataPath)
        .setDicomPath(dicomPath);
    fuseDao.uploadInstance(queryBuilder);
  }

  public static void deleteInstance(FuseDao fuseDao, CloudConf cloudConf, DicomPath dicomPath)
      throws DicomFuseException {
    QueryBuilder queryBuilder = QueryBuilder.forConfiguration(cloudConf)
        .setDicomStoreId(dicomPath.getDicomStoreId())
        .setStudyId(dicomPath.getStudyInstanceUID())
        .setSeriesId(dicomPath.getSeriesInstanceUID())
        .setInstanceId(dicomPath.getSopInstanceUID());
    fuseDao.deleteInstance(queryBuilder);
  }

  private FuseDaoHelper() {
  }
}
