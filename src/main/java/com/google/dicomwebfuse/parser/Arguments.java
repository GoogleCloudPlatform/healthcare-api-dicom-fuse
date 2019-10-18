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

package com.google.dicomwebfuse.parser;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.BooleanConverter;
import com.beust.jcommander.converters.LongConverter;
import com.beust.jcommander.converters.PathConverter;
import com.google.dicomwebfuse.entities.CloudConf;
import com.google.dicomwebfuse.entities.cache.CacheTime;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Arguments {

  @Parameter(
      names = {"--datasetAddr", "-a"},
      descriptionKey = "option.datasetAddr",
      required = true,
      order = 0,
      converter = CloudConfigurationConverter.class
  )
  public CloudConf cloudConf;

  @Parameter(
      names = {"--mountPath", "-p"},
      descriptionKey = "option.mountPath",
      required = true,
      order = 1,
      converter = PathConverter.class
  )
  public Path mountPath;

  @Parameter(
      names = {"--cacheTime", "-t"},
      descriptionKey = "option.cacheTime",
      converter = CacheTimeConverter.class,
      order = 2,
      validateWith = CacheTimePositiveValidator.class
  )
  public CacheTime cacheTime = new CacheTime(60, 300);

  @Parameter(
      names = {"--cacheSize", "-s"},
      descriptionKey = "option.cacheSize",
      converter = LongConverter.class,
      order = 3,
      validateWith = CacheSizePositiveValidator.class
  )
  public long cacheSize = 10000;

  @Parameter(
      names = {"--enableDeletion", "-d"},
      descriptionKey = "option.enableDeletion",
      order = 4,
      converter = BooleanConverter.class
  )
  public boolean enableDeletion = true;

  @Parameter(
      names = {"--keyFile", "-k"},
      descriptionKey = "option.keyFile",
      order = 5,
      converter = PathConverter.class
  )
  public Path keyPath;

  @Parameter(
      names = {"--extraMountOptions"},
      descriptionKey = "option.extraMountOptions",
      order = 6
  )
  public List<String> extraMountOptions = new ArrayList<>();

  @Parameter(
      names = {"--help", "-h"},
      help = true,
      descriptionKey = "option.help",
      order = 7
  )
  public boolean help = false;
}