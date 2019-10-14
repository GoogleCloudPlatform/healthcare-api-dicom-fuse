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

package com.google.dicomwebfuse.entities.cache;


import com.google.dicomwebfuse.entities.DicomPath;
import com.google.dicomwebfuse.entities.DicomPathLevel;
import com.google.dicomwebfuse.entities.DicomStore;
import com.google.dicomwebfuse.entities.Instance;
import com.google.dicomwebfuse.entities.Series;
import com.google.dicomwebfuse.entities.Study;
import com.google.dicomwebfuse.exception.DicomFuseException;
import com.google.dicomwebfuse.fuse.Command;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Cache {

  private CachedDataset cachedDataset = new CachedDataset();

  public CachedDicomStore getCachedDicomStore(DicomPath dicomPath) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = cachedDataset.getCachedDicomStores()
        .get(dicomPath.getDicomStoreId());
    if (cachedDicomStore == null) {
      throw new DicomFuseException("null cached DICOM Store - " + dicomPath);
    }
    return cachedDicomStore;
  }

  private CachedStudy getCachedStudy(CachedDicomStore cachedDicomStore, DicomPath dicomPath)
      throws DicomFuseException {
    CachedStudy cachedStudy = cachedDicomStore.getCachedStudies()
        .get(dicomPath.getStudyInstanceUID());
    if (cachedStudy == null) {
      throw new DicomFuseException("null cached Study - " + dicomPath);
    }
    return cachedStudy;
  }

  private CachedSeries getCachedSeries(CachedStudy cachedStudy, DicomPath dicomPath)
      throws DicomFuseException {
    CachedSeries cachedSeries = cachedStudy.getCachedSeries().get(dicomPath.getSeriesInstanceUID());
    if (cachedSeries == null) {
      throw new DicomFuseException("null cached Series - " + dicomPath);
    }
    return cachedSeries;
  }

  private InstanceContent getInstanceContent(CachedSeries cachedSeries, DicomPath dicomPath)
      throws DicomFuseException {
    InstanceContent instanceContent = cachedSeries.getCachedInstances()
        .get(dicomPath.getSopInstanceUID());
    if (instanceContent == null) {
      throw new DicomFuseException("null instance content - " + dicomPath);
    }
    return instanceContent;
  }

  private InstanceContent getTempInstanceContent(CachedDicomStore cachedDicomStore,
      DicomPath dicomPath) throws DicomFuseException {
    InstanceContent instanceContent = cachedDicomStore.getCachedTempInstances()
        .get(dicomPath.getFileName());
    if (instanceContent == null) {
      throw new DicomFuseException("null instance content - " + dicomPath);
    }
    return instanceContent;
  }

  public ConcurrentHashMap<String, CachedDicomStore> getCachedDicomStores() {
    return cachedDataset.getCachedDicomStores();
  }

  public ConcurrentHashMap<String, CachedStudy> getCachedStudies(DicomPath dicomPath)
      throws DicomFuseException {
    return getCachedDicomStore(dicomPath).getCachedStudies();
  }

  public ConcurrentHashMap<String, CachedSeries> getCachedSeries(DicomPath dicomPath)
      throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    return getCachedStudy(cachedDicomStore, dicomPath).getCachedSeries();
  }

  public ConcurrentHashMap<String, InstanceContent> getCachedInstances(DicomPath dicomPath)
      throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    CachedStudy cachedStudy = getCachedStudy(cachedDicomStore, dicomPath);
    return getCachedSeries(cachedStudy, dicomPath).getCachedInstances();
  }

  public ConcurrentHashMap<String, InstanceContent> getCachedTempInstances(DicomPath dicomPath)
      throws DicomFuseException {
    return getCachedDicomStore(dicomPath).getCachedTempInstances();
  }

  public List<DicomStore> getCachedDicomStoreList() {
    List<DicomStore> cachedDicomStoreList = new ArrayList<>();
    for (CachedDicomStore cachedDicomStore : cachedDataset.getCachedDicomStores().values()) {
      cachedDicomStoreList.add(cachedDicomStore.getDicomStore());
    }
    return cachedDicomStoreList;
  }

  public List<Study> getCachedStudyList(DicomPath dicomPath) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    List<Study> cachedStudyList = new ArrayList<>();
    for (CachedStudy cachedStudy : cachedDicomStore.getCachedStudies().values()) {
      cachedStudyList.add(cachedStudy.getStudy());
    }
    return cachedStudyList;
  }

  public List<Series> getCachedSeriesList(DicomPath dicomPath) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    CachedStudy cachedStudy = getCachedStudy(cachedDicomStore, dicomPath);
    List<Series> cachedSeriesList = new ArrayList<>();
    for (CachedSeries cachedSeries : cachedStudy.getCachedSeries().values()) {
      cachedSeriesList.add(cachedSeries.getSeries());
    }
    return cachedSeriesList;
  }

  public List<Instance> getCachedInstanceList(DicomPath dicomPath) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    CachedStudy cachedStudy = getCachedStudy(cachedDicomStore, dicomPath);
    CachedSeries cachedSeries = getCachedSeries(cachedStudy, dicomPath);
    List<Instance> cachedInstancesList = new ArrayList<>();
    for (InstanceContent instanceContent : cachedSeries.getCachedInstances().values()) {
      cachedInstancesList.add(instanceContent.getInstance());
    }
    return cachedInstancesList;
  }

  public InstanceContent getInstanceContent(DicomPath dicomPath) throws DicomFuseException {
    DicomPathLevel dicomPathLevel = dicomPath.getDicomPathLevel();
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    switch (dicomPathLevel) {
      case INSTANCE:
        CachedStudy cachedStudy = getCachedStudy(cachedDicomStore, dicomPath);
        CachedSeries cachedSeries = getCachedSeries(cachedStudy, dicomPath);
        return getInstanceContent(cachedSeries, dicomPath);
      case TEMP_FILE_IN_DICOM_STORE:
      case TEMP_FILE_IN_SERIES:
        return getTempInstanceContent(cachedDicomStore, dicomPath);
      default:
        throw new DicomFuseException("Invalid dicom path level - " + dicomPath);
    }
  }

  public long getInstanceSize(DicomPath dicomPath) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    CachedStudy cachedStudy = getCachedStudy(cachedDicomStore, dicomPath);
    CachedSeries cachedSeries = getCachedSeries(cachedStudy, dicomPath);
    return getInstanceContent(cachedSeries, dicomPath).getInstanceSize();
  }

  public Command getInstanceCommand(DicomPath dicomPath) throws DicomFuseException {
    DicomPathLevel dicomPathLevel = dicomPath.getDicomPathLevel();
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    InstanceContent instanceContent;
    switch (dicomPathLevel) {
      case INSTANCE:
        CachedStudy cachedStudy = getCachedStudy(cachedDicomStore, dicomPath);
        CachedSeries cachedSeries = getCachedSeries(cachedStudy, dicomPath);
        instanceContent = getInstanceContent(cachedSeries, dicomPath);
        return instanceContent.getCommand();
      case TEMP_FILE_IN_DICOM_STORE:
      case TEMP_FILE_IN_SERIES:
        instanceContent = getTempInstanceContent(cachedDicomStore, dicomPath);
        return instanceContent.getCommand();
      default:
        throw new DicomFuseException("Invalid dicom path level - " + dicomPath);
    }
  }

  public void setInstanceCommand(DicomPath dicomPath, Command command) throws DicomFuseException {
    DicomPathLevel dicomPathLevel = dicomPath.getDicomPathLevel();
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    InstanceContent instanceContent;
    switch (dicomPathLevel) {
      case INSTANCE:
        CachedStudy cachedStudy = getCachedStudy(cachedDicomStore, dicomPath);
        CachedSeries cachedSeries = getCachedSeries(cachedStudy, dicomPath);
        instanceContent = getInstanceContent(cachedSeries, dicomPath);
        instanceContent.setCommand(command);
        break;
      case TEMP_FILE_IN_DICOM_STORE:
      case TEMP_FILE_IN_SERIES:
        instanceContent = getTempInstanceContent(cachedDicomStore, dicomPath);
        instanceContent.setCommand(command);
        break;
      default:
        throw new DicomFuseException("Invalid dicom path level - " + dicomPath);
    }
  }

  /**
   * Checks that Dataset outdated or not.
   *
   * @return true if Dataset outdated, false if not
   */
  public boolean isDatasetOutdated() {
    // Instant.now() - Returns a value in microseconds in Java 9 and later, but returns
    // milliseconds in Java 8. That's why, to avoid incorrect isBefore(instantNow) results in
    // cases where less than a millisecond has passed equals(instantNow) were included.
    Instant instantNow = Instant.now();
    return cachedDataset.getDatasetCacheTime().isBefore(instantNow) ||
        cachedDataset.getDatasetCacheTime().equals(instantNow);
  }

  /**
   * Checks that DICOM Store outdated or not.
   *
   * @param dicomPath current DICOM path to the DICOM Store
   * @return true if DICOM Store outdated, false if not
   */
  public boolean isDicomStoreOutdated(DicomPath dicomPath) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    // Instant.now() - Returns a value in microseconds in Java 9 and later, but returns
    // milliseconds in Java 8. That's why, to avoid incorrect isBefore(instantNow) results in
    // cases where less than a millisecond has passed equals(instantNow) were included.
    Instant instantNow = Instant.now();
    return cachedDicomStore.getDicomStoreCacheTime().isBefore(instantNow) ||
        cachedDicomStore.getDicomStoreCacheTime().equals(instantNow);
  }

  /**
   * Checks that Study outdated or not.
   *
   * @param dicomPath current DICOM path to the Study
   * @return true if Study outdated, false if not
   */
  public boolean isStudyOutdated(DicomPath dicomPath) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    CachedStudy cachedStudy = getCachedStudy(cachedDicomStore, dicomPath);
    // Instant.now() - Returns a value in microseconds in Java 9 and later, but returns
    // milliseconds in Java 8. That's why, to avoid incorrect isBefore(instantNow) results in
    // cases where less than a millisecond has passed equals(instantNow) were included.
    Instant instantNow = Instant.now();
    return cachedStudy.getStudyCacheTime().isBefore(instantNow) ||
        cachedStudy.getStudyCacheTime().equals(instantNow);
  }

  /**
   * Checks that Series outdated or not.
   *
   * @param dicomPath current DICOM path to the Series
   * @return true if Series outdated, false if not
   */
  public boolean isSeriesOutdated(DicomPath dicomPath) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    CachedStudy cachedStudy = getCachedStudy(cachedDicomStore, dicomPath);
    CachedSeries cachedSeries = getCachedSeries(cachedStudy, dicomPath);
    // Instant.now() - Returns a value in microseconds in Java 9 and later, but returns
    // milliseconds in Java 8. That's why, to avoid incorrect isBefore(instantNow) results in
    // cases where less than a millisecond has passed equals(instantNow) were included.
    Instant instantNow = Instant.now();
    return cachedSeries.getSeriesCacheTime().isBefore(Instant.now()) ||
        cachedStudy.getStudyCacheTime().equals(instantNow);
  }

  public boolean isDicomStoreNotExist(DicomPath dicomPath) {
    return !cachedDataset.getCachedDicomStores().containsKey(dicomPath.getDicomStoreId());
  }

  public boolean isStudyNotExist(DicomPath dicomPath) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    return !cachedDicomStore.getCachedStudies().containsKey(dicomPath.getStudyInstanceUID());
  }

  public boolean isSeriesNotExist(DicomPath dicomPath) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    CachedStudy cachedStudy = getCachedStudy(cachedDicomStore, dicomPath);
    return !cachedStudy.getCachedSeries().containsKey(dicomPath.getSeriesInstanceUID());
  }

  public boolean isInstanceNotExist(DicomPath dicomPath) throws DicomFuseException {
    DicomPathLevel dicomPathLevel = dicomPath.getDicomPathLevel();
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    switch (dicomPathLevel) {
      case INSTANCE:
        CachedStudy cachedStudy = getCachedStudy(cachedDicomStore, dicomPath);
        CachedSeries cachedSeries = getCachedSeries(cachedStudy, dicomPath);
        return !cachedSeries.getCachedInstances().containsKey(dicomPath.getSopInstanceUID());
      case TEMP_FILE_IN_DICOM_STORE:
      case TEMP_FILE_IN_SERIES:
        return !cachedDicomStore.getCachedTempInstances()
            .containsKey(dicomPath.getFileName());
      default:
        throw new DicomFuseException("Invalid dicom path level - " + dicomPath);
    }
  }

  public void setDatasetCacheTime(Instant instant) {
    cachedDataset.setDatasetCacheTime(instant);
  }

  public void setDicomStoreCacheTime(DicomPath dicomPath, Instant instant)
      throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    cachedDicomStore.setDicomStoreCacheTime(instant);
  }

  public void setStudyCacheTime(DicomPath dicomPath, Instant instant) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    CachedStudy cachedStudy = getCachedStudy(cachedDicomStore, dicomPath);
    cachedStudy.setStudyCacheTime(instant);
  }

  public void setSeriesCacheTime(DicomPath dicomPath, Instant instant) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    CachedStudy cachedStudy = getCachedStudy(cachedDicomStore, dicomPath);
    CachedSeries cachedSeries = getCachedSeries(cachedStudy, dicomPath);
    cachedSeries.setSeriesCacheTime(instant);
  }

  public AtomicLong getOffset(DicomPath dicomPath) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = getCachedDicomStore(dicomPath);
    return getTempInstanceContent(cachedDicomStore, dicomPath).getOffset();
  }
}
