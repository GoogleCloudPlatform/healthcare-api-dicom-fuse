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

import com.google.dicomwebfuse.dao.FuseDao;
import com.google.dicomwebfuse.entities.CloudConf;
import com.google.dicomwebfuse.entities.cache.CacheTime;
import com.google.dicomwebfuse.parser.Arguments;
import jnr.ffi.Platform.OS;

public class Parameters {

  private final FuseDao fuseDAO;
  private final CloudConf cloudConf;
  private final CacheTime cacheTime;
  private final long cacheSize;
  private final boolean enableDeletion;
  private final OS os;

  public Parameters(FuseDao fuseDAO, Arguments arguments, OS os) {
    this.fuseDAO = fuseDAO;
    this.cloudConf = arguments.cloudConf;
    this.cacheTime = arguments.cacheTime;
    this.cacheSize = arguments.cacheSize;
    this.enableDeletion = arguments.enableDeletion;
    this.os = os;
  }

  public FuseDao getFuseDAO() {
    return fuseDAO;
  }

  public CloudConf getCloudConf() {
    return cloudConf;
  }

  public CacheTime getCacheTime() {
    return cacheTime;
  }

  public long getCacheSize() {
    return cacheSize;
  }

  boolean isEnableDeletion() {
    return enableDeletion;
  }

  OS getOs() {
    return os;
  }
}
