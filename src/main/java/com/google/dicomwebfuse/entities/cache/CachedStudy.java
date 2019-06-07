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

import com.google.dicomwebfuse.entities.Study;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class CachedStudy {

  private final Study study;
  private ConcurrentHashMap<String, CachedSeries> cachedSeries; // key - seriesInstanceUID
  private Instant studyCacheTime;

  public CachedStudy(Study study) {
    this.study = study;
    cachedSeries = new ConcurrentHashMap<>();
    studyCacheTime = Instant.now();
  }

  public Study getStudy() {
    return study;
  }

  public ConcurrentHashMap<String, CachedSeries> getCachedSeries() {
    return cachedSeries;
  }

  public Instant getStudyCacheTime() {
    return studyCacheTime;
  }

  public void setStudyCacheTime(Instant studyCacheTime) {
    this.studyCacheTime = studyCacheTime;
  }
}
