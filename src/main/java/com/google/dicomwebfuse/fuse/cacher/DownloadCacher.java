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

package com.google.dicomwebfuse.fuse.cacher;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.dicomwebfuse.dao.FuseDaoHelper;
import com.google.dicomwebfuse.entities.DicomPath;
import com.google.dicomwebfuse.exception.DicomFuseException;
import com.google.dicomwebfuse.fuse.Parameters;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DownloadCacher {

  private static final Logger LOGGER = LogManager.getLogger();
  private static final int BYTES_IN_MEGABYTE = 1000 * 1000;
  private final Parameters parameters;
  private LoadingCache<DicomPath, Path> downloadedFiles;

  public DownloadCacher(Parameters parameters) {
    this.parameters = parameters;
    long instancesTime = parameters.getCacheTime().getInstanceFilesCacheTime();
    downloadedFiles = CacheBuilder.newBuilder()
        .expireAfterWrite(instancesTime, TimeUnit.SECONDS)
        .removalListener((RemovalListener<DicomPath, Path>) notification -> {
          Path path = notification.getValue();
          try {
            CacherUtils.deleteFile(path);
          } catch (DicomFuseException e) {
            LOGGER.error("Error deleting the downloaded file!", e);
          }
        })
        .maximumWeight(parameters.getCacheSize())
        .weigher((dicomPath, path) -> {
          try {
            return (int) Files.size(path) / BYTES_IN_MEGABYTE;
          } catch (IOException e) {
            LOGGER.error("Error getting file size!", e);
            return 0;
          }
        })
        .build(new CacheLoader<DicomPath, Path>() {
          @Override
          public Path load(@NonNull DicomPath dicomPath) throws DicomFuseException {
            return getInstance(dicomPath);
          }
        });
  }

  public Path getPath(DicomPath dicomPath) throws DicomFuseException {
    try {
      return downloadedFiles.get(dicomPath);
    } catch (ExecutionException e) {
      throw new DicomFuseException(e);
    }
  }

  public Path getPathIfPresent(DicomPath dicomPath) {
    return downloadedFiles.getIfPresent(dicomPath);
  }

  public void removePath(DicomPath dicomPath) {
    downloadedFiles.invalidate(dicomPath);
  }

  private Path getInstance(DicomPath dicomPath) throws DicomFuseException {
    LOGGER.info("File caching started  - " + dicomPath);
    Path instanceDataPath = CacherUtils.createTempPath();
    FuseDaoHelper.downloadInstanceToTempFile(parameters.getFuseDAO(), parameters.getCloudConf(),
        dicomPath, instanceDataPath);
    return instanceDataPath;
  }
}
