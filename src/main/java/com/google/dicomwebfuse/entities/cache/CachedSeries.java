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

import com.google.dicomwebfuse.entities.Series;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class CachedSeries {

  private final Series series;
  private ConcurrentHashMap<String, InstanceContent> cachedInstances; //key - sopInstanceUID
  private Instant seriesCacheTime;

  public CachedSeries(Series series) {
    this.series = series;
    cachedInstances = new ConcurrentHashMap<>();
    seriesCacheTime = Instant.now();
  }

  public Series getSeries() {
    return series;
  }

  public ConcurrentHashMap<String, InstanceContent> getCachedInstances() {
    return cachedInstances;
  }

  public Instant getSeriesCacheTime() {
    return seriesCacheTime;
  }

  public void setSeriesCacheTime(Instant cachedTime) {
    this.seriesCacheTime = cachedTime;
  }
}
