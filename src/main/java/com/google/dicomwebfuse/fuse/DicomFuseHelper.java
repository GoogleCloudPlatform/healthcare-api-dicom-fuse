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

import static com.google.dicomwebfuse.entities.DicomPathLevel.SERIES;
import static com.google.dicomwebfuse.entities.DicomPathLevel.STUDY;
import static com.google.dicomwebfuse.fuse.FuseConstants.DCM_EXTENSION;
import static com.google.dicomwebfuse.fuse.FuseConstants.LINUX_FORBIDDEN_PATHS;
import static com.google.dicomwebfuse.fuse.FuseConstants.MAC_OS_FORBIDDEN_PATHS;
import static com.google.dicomwebfuse.fuse.FuseConstants.WINDOWS_FORBIDDEN_PATHS;

import com.google.dicomwebfuse.dao.FuseDaoHelper;
import com.google.dicomwebfuse.entities.DicomPath;
import com.google.dicomwebfuse.entities.DicomPathLevel;
import com.google.dicomwebfuse.entities.DicomStore;
import com.google.dicomwebfuse.entities.Instance;
import com.google.dicomwebfuse.entities.Series;
import com.google.dicomwebfuse.entities.Study;
import com.google.dicomwebfuse.entities.cache.Cache;
import com.google.dicomwebfuse.entities.cache.CachedDicomStore;
import com.google.dicomwebfuse.entities.cache.CachedSeries;
import com.google.dicomwebfuse.entities.cache.CachedStudy;
import com.google.dicomwebfuse.entities.cache.InstanceContent;
import com.google.dicomwebfuse.exception.DicomFuseException;
import com.google.dicomwebfuse.fuse.cacher.DicomPathCacher;
import com.google.dicomwebfuse.fuse.cacher.DownloadCacher;
import com.google.dicomwebfuse.fuse.cacher.UploadCacher;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import jnr.ffi.Platform.OS;
import jnr.ffi.Pointer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.struct.FileStat;

class DicomFuseHelper {

  private static final Logger LOGGER = LogManager.getLogger();
  private final Parameters parameters;
  private final DownloadCacher downloadCacher;
  private final UploadCacher uploadCacher;
  private final Cache cache;
  private final DicomPathCacher dicomPathCacher;
  private final OS os;
  private final Instant defaultInstant;


  DicomFuseHelper(Parameters parameters, DicomPathCacher dicomPathCacher, Cache cache) {
    this.parameters = parameters;
    downloadCacher = new DownloadCacher(parameters);
    uploadCacher = new UploadCacher();
    this.cache = cache;
    this.dicomPathCacher = dicomPathCacher;
    os = parameters.getOs();
    defaultInstant = Instant.EPOCH.plusSeconds(60 * 60 * 24).plusNanos(1000);
  }

  void checkExistingObject(DicomPath dicomPath) throws DicomFuseException {
    switch (dicomPath.getDicomPathLevel()) {
      case DATASET:
        break;
      case DICOM_STORE:
        if (cache.isDicomStoreNotExist(dicomPath)) {
          cacheDicomStoreIfPresent(dicomPath);
        }
        break;
      case STUDY:
        if (cache.isStudyNotExist(dicomPath)) {
          cacheStudyIfPresent(dicomPath);
        }
        break;
      case SERIES:
        if (cache.isSeriesNotExist(dicomPath)) {
          cacheSeriesIfPresent(dicomPath);
        }
        break;
      case INSTANCE:
        if (cache.isInstanceNotExist(dicomPath)) {
          cacheInstanceIfPresent(dicomPath);
        }
        break;
      case TEMP_FILE_IN_DICOM_STORE:
      case TEMP_FILE_IN_SERIES:
        if (cache.isInstanceNotExist(dicomPath)) {
          throw new DicomFuseException("Invalid path to the temp instance - " + dicomPath);
        }
        break;
      default:
        throw new DicomFuseException("Invalid path - " + dicomPath);
    }
  }

  void checkPath(String path) throws DicomFuseException {
    switch (os) {
      case LINUX:
        for (String partPath : LINUX_FORBIDDEN_PATHS) {
          if (path.contains(partPath)) {
            throw new DicomFuseException("Invalid path - " + path);
          }
        }
        break;
      case WINDOWS:
        for (String partPath : WINDOWS_FORBIDDEN_PATHS) {
          if (path.contains(partPath)) {
            throw new DicomFuseException("Invalid path - " + path);
          }
        }
        break;
      case DARWIN:
        for (String partPath : MAC_OS_FORBIDDEN_PATHS) {
          if (path.contains(partPath)) {
            throw new DicomFuseException("Invalid path - " + path);
          }
        }
        break;
      default:
        throw new DicomFuseException("Invalid OS - " + path);
    }
  }

  void updateDir(DicomPath dicomPath) throws DicomFuseException {
    switch (dicomPath.getDicomPathLevel()) {
      case DATASET:
        if (cache.isDatasetOutdated()) {
          updateDicomStoresInDataset();
        }
        break;
      case DICOM_STORE:
        if (cache.isDicomStoreOutdated(dicomPath)) {
          updateStudiesInDicomStore(dicomPath);
        }
        break;
      case STUDY:
        if (cache.isStudyOutdated(dicomPath)) {
          updateSeriesInStudy(dicomPath);
        }
        break;
      case SERIES:
        if (cache.isSeriesOutdated(dicomPath)) {
          updateInstancesInSeries(dicomPath);
        }
        break;
      default:
        throw new DicomFuseException("Invalid path - " + dicomPath);
    }
  }

  void setAttr(DicomPath dicomPath, DicomFuse dicomFuse, FileStat fileStat)
      throws DicomFuseException {
    switch (dicomPath.getDicomPathLevel()) {
      case DATASET:
        setStat(dicomFuse, fileStat, FileStat.S_IFDIR | 0777);
        break;
      case DICOM_STORE:
        setStat(dicomFuse, fileStat, FileStat.S_IFDIR | 0777);
        break;
      case STUDY:
        setStat(dicomFuse, fileStat, FileStat.S_IFDIR | 0777);
        break;
      case SERIES:
        setStat(dicomFuse, fileStat, FileStat.S_IFDIR | 0777);
        break;
      case INSTANCE:
      case TEMP_FILE_IN_DICOM_STORE:
      case TEMP_FILE_IN_SERIES:
        setStat(dicomFuse, fileStat, FileStat.S_IFREG | 0666, dicomPath);
        break;
      default:
        throw new DicomFuseException("Error level");
    }
  }

  private void setStat(DicomFuse dicomFuse, FileStat fileStat, int perm) throws DicomFuseException {
    setStat(dicomFuse, fileStat, perm, null);
  }

  private void setStat(DicomFuse dicomFuse, FileStat fileStat, int perm, DicomPath dicomPath)
      throws DicomFuseException {
    fileStat.st_mode.set(perm);
    fileStat.st_nlink.set(1);
    // set file size if exists
    if (dicomPath != null) {
      if (dicomPath.getDicomPathLevel() == DicomPathLevel.INSTANCE) {
        long instanceSize = cache.getInstanceSize(dicomPath);
        if (instanceSize != 0) {
          fileStat.st_size.set(instanceSize);
        } else {
          fileStat.st_size.set(0);
        }
      } else {
        fileStat.st_size.set(0);
      }
    }

    // set uid and gid
    fileStat.st_uid.set(dicomFuse.getContext().uid.get());
    fileStat.st_gid.set(dicomFuse.getContext().gid.get());

    // set default data, it needs to be implemented because
    // FileStat contains garbage from uninitialized memory in getattr.
    // See: https://github.com/SerCeMan/jnr-fuse/issues/68
    if (os == OS.DARWIN || os == OS.WINDOWS) {
      fileStat.st_birthtime.tv_sec.set(defaultInstant.getEpochSecond());
      fileStat.st_birthtime.tv_nsec.set(defaultInstant.getNano());
    }

    fileStat.st_dev.set(0);
    fileStat.st_ino.set(0);
    fileStat.st_rdev.set(0);
    fileStat.st_blksize.set(1024 * 64);
    fileStat.st_blocks.set(0);

    fileStat.st_mtim.tv_sec.set(defaultInstant.getEpochSecond());
    fileStat.st_mtim.tv_nsec.set(defaultInstant.getNano());

    fileStat.st_ctim.tv_sec.set(defaultInstant.getEpochSecond());
    fileStat.st_ctim.tv_nsec.set(defaultInstant.getNano());

    fileStat.st_atim.tv_sec.set(defaultInstant.getEpochSecond());
    fileStat.st_atim.tv_nsec.set(defaultInstant.getNano());

    if (os == OS.DARWIN) {
      fileStat.st_flags.set(0);
      fileStat.st_gen.set(0);
    }
  }

  void fillFolder(DicomPath dicomPath, Pointer buf, FuseFillDir filler) throws DicomFuseException {
    switch (dicomPath.getDicomPathLevel()) {
      case DATASET:
        fillDatasetFolder(buf, filler);
        break;
      case DICOM_STORE:
        fillDicomStoreFolder(dicomPath, buf, filler);
        break;
      case STUDY:
        fillStudyFolder(dicomPath, buf, filler);
        break;
      case SERIES:
        fillSeriesFolder(dicomPath, buf, filler);
        break;
      default:
        throw new DicomFuseException("Error level");
    }
  }

  private void fillDatasetFolder(Pointer buf, FuseFillDir filler) {
    List<DicomStore> dicomStoreList = cache.getCachedDicomStoreList();
    for (DicomStore dicomStore : dicomStoreList) {
      String dicomStoreId = dicomStore.getDicomStoreId();
      filler.apply(buf, dicomStoreId, null, 0);
    }
  }

  private void fillDicomStoreFolder(DicomPath dicomPath, Pointer buf, FuseFillDir filler)
      throws DicomFuseException {
    List<Study> studyList = cache.getCachedStudyList(dicomPath);
    for (Study study : studyList) {
      String studyInstanceUID = study.getStudyInstanceUID().getValue1();
      filler.apply(buf, studyInstanceUID, null, 0);
    }
  }

  private void fillStudyFolder(DicomPath dicomPath, Pointer buf, FuseFillDir filler)
      throws DicomFuseException {
    List<Series> seriesList = cache.getCachedSeriesList(dicomPath);
    for (Series series : seriesList) {
      String seriesInstanceUID = series.getSeriesInstanceUID().getValue1();
      filler.apply(buf, seriesInstanceUID, null, 0);
    }
  }

  private void fillSeriesFolder(DicomPath dicomPath, Pointer buf, FuseFillDir filler)
      throws DicomFuseException {
    List<Instance> instanceList = cache.getCachedInstanceList(dicomPath);
    for (Instance instance : instanceList) {
      String sopInstanceUID = instance.getSopInstanceUID().getValue1();
      filler.apply(buf, sopInstanceUID + DCM_EXTENSION, null, 0);
    }
  }

  int readInstance(DicomPath dicomPath, Pointer buf, int size, long offset)
      throws DicomFuseException {
    Path instancePath = downloadCacher.getPathIfPresent(dicomPath);
    if (instancePath == null) {
      throw new DicomFuseException("Error reading file. Try open the file again. " + dicomPath);
    }
    int length = 0;
    try (RandomAccessFile raf = new RandomAccessFile(instancePath.toFile(), "r")) {
      raf.seek(offset);
      byte[] buffer = new byte[size];
      if ((length = raf.read(buffer)) != -1) {
        buf.put(0, buffer, 0, length);
      } else {
        length = 0;
      }
    } catch (IOException e) {
      LOGGER.error("Error reading file", e);
    }
    return length;
  }

  int writeInstance(DicomPath dicomPath, Pointer buf, int size, long offset)
      throws DicomFuseException {
    AtomicLong instanceOffset = cache.getOffset(dicomPath);
    if (offset == 0) {
      // Double check for macOS.
      // See: https://github.com/osxfuse/osxfuse/issues/587
      if (uploadCacher.getPath(dicomPath) == null) {
        uploadCacher.createPath(dicomPath);
      }
    }
    // check for macOS
    if (offset < instanceOffset.get()) {
      return size;
    }
    if (cache.getInstanceCommand(dicomPath) != Command.WRITE) {
      cache.setInstanceCommand(dicomPath, Command.WRITE);
    }
    Path instancePath = uploadCacher.getPath(dicomPath);
    try (RandomAccessFile raf = new RandomAccessFile(instancePath.toFile(), "rw")) {
      raf.seek(offset);
      byte[] buffer = new byte[size];
      buf.get(0, buffer, 0, size);
      raf.write(buffer);
    } catch (IOException e) {
      LOGGER.error("Random access file write error", e);
      return 0;
    }
    instanceOffset.set(offset + size); // for macOS
    return size;
  }

  void cacheInstanceData(DicomPath dicomPath) throws DicomFuseException {
    if (dicomPath.getDicomPathLevel() == DicomPathLevel.INSTANCE) {
      Path path = downloadCacher.getPath(dicomPath);
      long instanceSize = cache.getInstanceSize(dicomPath);
      if (instanceSize == 0) {
        InstanceContent instanceContent = cache.getInstanceContent(dicomPath);
        try {
          instanceContent.setInstanceSize(Files.size(path));
        } catch (IOException e) {
          throw new DicomFuseException(e);
        }
      }
    }
  }

  void flushInstance(DicomPath dicomPath) throws DicomFuseException {
    Command command = null;
    try {
      command = cache.getInstanceCommand(dicomPath);
    } catch (DicomFuseException e) {
      LOGGER.debug("Null command in instance: " + dicomPath, e);
    }
    if (command == Command.WRITE) {
      cache.setInstanceCommand(dicomPath, null);
      saveInstance(dicomPath);
      LOGGER.info("Instance was uploaded - " + dicomPath);
    }
  }

  private void saveInstance(DicomPath dicomPath) throws DicomFuseException {
    Path instanceDataPath = uploadCacher.getPath(dicomPath);
    switch (dicomPath.getDicomPathLevel()) {
      case TEMP_FILE_IN_DICOM_STORE:
      case TEMP_FILE_IN_SERIES:
        try {
          FuseDaoHelper.uploadInstance(parameters.getFuseDAO(), parameters.getCloudConf(),
              dicomPath, instanceDataPath);
        } finally {
          Runnable clearResources = () -> {
            try {
              TimeUnit.SECONDS.sleep(3);
              cache.getCachedTempInstances(dicomPath).remove(dicomPath.getFileName());
              uploadCacher.removePath(dicomPath);
              dicomPathCacher.removeDicomPath(dicomPath.getFileName());
            } catch (DicomFuseException | InterruptedException e) {
              LOGGER.error("Clear resources error", e);
            }
          };
          Thread thread = new Thread(clearResources);
          thread.start();
        }
        invalidateDicomStoreCache(dicomPath);
        break;
      case INSTANCE:
        try {
          FuseDaoHelper.deleteInstance(parameters.getFuseDAO(), parameters.getCloudConf(),
              dicomPath);
        } finally {
          downloadCacher.removePath(dicomPath);
        }
        LOGGER.info("Instance was deleted - " + dicomPath);
        try {
          FuseDaoHelper.uploadInstance(parameters.getFuseDAO(), parameters.getCloudConf(),
              dicomPath, instanceDataPath);
        } finally {
          uploadCacher.removePath(dicomPath);
          invalidateDicomStoreCache(dicomPath);
        }
        break;
      default:
    }
  }

  void createTemporaryInstance(DicomPath dicomPath) throws DicomFuseException {
    InstanceContent instanceContent = new InstanceContent(new Instance());
    cache.getCachedTempInstances(dicomPath).put(dicomPath.getFileName(), instanceContent);
    LOGGER.debug("Temporary instance was created " + dicomPath);
  }

  void unlinkInstance(DicomPath dicomPath) throws DicomFuseException {
    FuseDaoHelper.deleteInstance(parameters.getFuseDAO(), parameters.getCloudConf(), dicomPath);
    LOGGER.info("Instance was deleted - " + dicomPath);
    downloadCacher.removePath(dicomPath);
    invalidateDicomStoreCache(dicomPath);
  }

  private void updateDicomStoresInDataset() throws DicomFuseException {
    List<DicomStore> dicomStoreList =
        FuseDaoHelper.getAllDicomStores(parameters.getFuseDAO(), parameters.getCloudConf());
    List<DicomStore> cachedDicomStoreList = cache.getCachedDicomStoreList();
    for (DicomStore dicomStore : cachedDicomStoreList) {
      if (dicomStoreList.contains(dicomStore)) {
        dicomStoreList.remove(dicomStore);
      } else {
        String dicomStoreId = dicomStore.getDicomStoreId();
        cache.getCachedDicomStores().remove(dicomStoreId);
      }
    }
    for (DicomStore dicomStore : dicomStoreList) {
      String dicomStoreId = dicomStore.getDicomStoreId();
      CachedDicomStore newCachedDicomStore = new CachedDicomStore(dicomStore);
      cache.getCachedDicomStores().put(dicomStoreId, newCachedDicomStore);
    }
    Instant newInstant = Instant.now().plusSeconds(parameters.getCacheTime().getObjectsCacheTime());
    cache.setDatasetCacheTime(newInstant);
  }

  private void updateStudiesInDicomStore(DicomPath dicomPath) throws DicomFuseException {
    List<Study> studyList = FuseDaoHelper.getStudies(parameters.getFuseDAO(),
        parameters.getCloudConf(), dicomPath);
    List<Study> cachedStudyList;
    try {
      cachedStudyList = cache.getCachedStudyList(dicomPath);
    } catch (DicomFuseException e) {
      LOGGER.debug("Study null in " + dicomPath.getDicomStoreId() + " dicom store");
      return;
    }
    for (Study study : cachedStudyList) {
      if (studyList.contains(study)) {
        studyList.remove(study);
      } else {
        String studyInstanceUID = study.getStudyInstanceUID().getValue1();
        cache.getCachedStudies(dicomPath).remove(studyInstanceUID);
      }
    }
    for (Study study : studyList) {
      String studyInstanceUID = study.getStudyInstanceUID().getValue1();
      CachedStudy newCachedStudy = new CachedStudy(study);
      cache.getCachedStudies(dicomPath).put(studyInstanceUID, newCachedStudy);
    }
    Instant newInstant = Instant.now().plusSeconds(parameters.getCacheTime().getObjectsCacheTime());
    cache.setDicomStoreCacheTime(dicomPath, newInstant);
  }

  private void updateSeriesInStudy(DicomPath dicomPath) throws DicomFuseException {
    List<Series> seriesList = FuseDaoHelper.getSeries(parameters.getFuseDAO(),
        parameters.getCloudConf(), dicomPath);
    List<Series> cachedSeriesList;
    try {
      cachedSeriesList = cache.getCachedSeriesList(dicomPath);
    } catch (DicomFuseException e) {
      LOGGER.debug("Series null in " + dicomPath.getStudyInstanceUID() + " study");
      return;
    }
    for (Series series : cachedSeriesList) {
      if (seriesList.contains(series)) {
        seriesList.remove(series);
      } else {
        String seriesInstanceUID = series.getSeriesInstanceUID().getValue1();
        cache.getCachedSeries(dicomPath).remove(seriesInstanceUID);
      }
    }
    for (Series series : seriesList) {
      String seriesInstanceUID = series.getSeriesInstanceUID().getValue1();
      CachedSeries newCachedSeries = new CachedSeries(series);
      cache.getCachedSeries(dicomPath).put(seriesInstanceUID, newCachedSeries);
    }
    Instant newInstant = Instant.now().plusSeconds(parameters.getCacheTime().getObjectsCacheTime());
    cache.setStudyCacheTime(dicomPath, newInstant);
  }

  private void updateInstancesInSeries(DicomPath dicomPath) throws DicomFuseException {
    List<Instance> instanceList = FuseDaoHelper.getInstances(parameters.getFuseDAO(),
        parameters.getCloudConf(), dicomPath);
    List<Instance> cachedInstanceList;
    try {
      cachedInstanceList = cache.getCachedInstanceList(dicomPath);
    } catch (DicomFuseException e) {
      LOGGER.debug("Instances null in " + dicomPath.getSeriesInstanceUID() + " series");
      return;
    }
    for (Instance instance : cachedInstanceList) {
      if (instanceList.contains(instance)) {
        instanceList.remove(instance);
      } else {
        String sopInstanceUID = instance.getSopInstanceUID().getValue1();
        cache.getCachedInstances(dicomPath).remove(sopInstanceUID);
      }
    }
    for (Instance instance : instanceList) {
      String sopInstanceUID = instance.getSopInstanceUID().getValue1();
      InstanceContent newInstanceContent = new InstanceContent(instance);
      cache.getCachedInstances(dicomPath).put(sopInstanceUID, newInstanceContent);
    }
    Instant newInstant = Instant.now().plusSeconds(parameters.getCacheTime().getObjectsCacheTime());
    cache.setSeriesCacheTime(dicomPath, newInstant);
  }

  private void cacheDicomStoreIfPresent(DicomPath dicomPath) throws DicomFuseException {
    DicomStore dicomStore = FuseDaoHelper.getSingleDicomStore(parameters.getFuseDAO(),
        parameters.getCloudConf(), dicomPath);
    String dicomStoreId = dicomStore.getDicomStoreId();
    CachedDicomStore newCachedDicomStore = new CachedDicomStore(dicomStore);
    cache.getCachedDicomStores().put(dicomStoreId, newCachedDicomStore);
  }

  private void cacheStudyIfPresent(DicomPath dicomPath) throws DicomFuseException {
    Study study = FuseDaoHelper.getSingleStudy(parameters.getFuseDAO(), parameters.getCloudConf(),
        dicomPath);
    String studyInstanceUID = study.getStudyInstanceUID().getValue1();
    CachedStudy newCachedStudy = new CachedStudy(study);
    cache.getCachedStudies(dicomPath).put(studyInstanceUID, newCachedStudy);
  }

  private void cacheSeriesIfPresent(DicomPath dicomPath) throws DicomFuseException {
    Series series = FuseDaoHelper.getSingleSeries(parameters.getFuseDAO(),
        parameters.getCloudConf(), dicomPath);
    String seriesInstanceUID = series.getSeriesInstanceUID().getValue1();
    CachedSeries newCachedSeries = new CachedSeries(series);
    cache.getCachedSeries(dicomPath).put(seriesInstanceUID, newCachedSeries);
  }

  private void cacheInstanceIfPresent(DicomPath dicomPath) throws DicomFuseException {
    Instance instance = FuseDaoHelper.getSingleInstance(parameters.getFuseDAO(),
        parameters.getCloudConf(), dicomPath);
    String sopInstanceUID = instance.getSopInstanceUID().getValue1();
    InstanceContent newInstanceContent = new InstanceContent(instance);
    cache.getCachedInstances(dicomPath).put(sopInstanceUID, newInstanceContent);
  }

  private void invalidateDicomStoreCache(DicomPath dicomPath) throws DicomFuseException {
    CachedDicomStore cachedDicomStore = cache.getCachedDicomStore(dicomPath);
    cachedDicomStore.setDicomStoreCacheTime(Instant.now());
    List<Study> existingStudiesInCache = cache.getCachedStudyList(dicomPath);
    for (Study study : existingStudiesInCache) {
      String studyInstanceUID = study.getStudyInstanceUID().getValue1();
      DicomPath studyDicomPath = new DicomPath.Builder(STUDY)
          .dicomStoreId(dicomPath.getDicomStoreId())
          .studyInstanceUID(studyInstanceUID)
          .build();
      List<Series> cachedSeriesList = cache.getCachedSeriesList(studyDicomPath);
      for (Series series : cachedSeriesList) {
        String seriesInstanceUID = series.getSeriesInstanceUID().getValue1();
        DicomPath seriesDicomPath = new DicomPath.Builder(SERIES)
            .dicomStoreId(dicomPath.getDicomStoreId())
            .studyInstanceUID(studyInstanceUID)
            .seriesInstanceUID(seriesInstanceUID)
            .build();
        cache.setSeriesCacheTime(seriesDicomPath, Instant.now());
      }
      cache.setStudyCacheTime(studyDicomPath, Instant.now());
    }
  }
}
