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

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class CachedDataset {

  private ConcurrentHashMap<String, CachedDicomStore> cachedDicomStores; // key - dicomStoreId
  private Instant datasetCacheTime;

  public CachedDataset() {
    cachedDicomStores = new ConcurrentHashMap<>();
    datasetCacheTime = Instant.now();
  }

  public ConcurrentHashMap<String, CachedDicomStore> getCachedDicomStores() {
    return cachedDicomStores;
  }

  public Instant getDatasetCacheTime() {
    return datasetCacheTime;
  }

  public void setDatasetCacheTime(Instant datasetCacheTime) {
    this.datasetCacheTime = datasetCacheTime;
  }
}
