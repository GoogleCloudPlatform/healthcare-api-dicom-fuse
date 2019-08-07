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

public class Arguments {

  @Parameter(
      names = {"--datasetAddr", "-a"},
      description = "Web address to the Dataset. " +
          "Example: https://healthcare.googleapis.com/v1beta1/projects/PROJECT/locations/" +
          "LOCATION/datasets/DATASET",
      required = true,
      converter = CloudConfigurationConverter.class
  )
  public CloudConf cloudConf;

  @Parameter(
      names = {"--mountPath", "-p"},
      description = "Path to mount the Dataset. Example on Linux: /home/user/fuse-mount-folder. "
          + "Example on macOS: /Users/user/fuse-mount-folder "
          + "Example on Windows: J:\\",
      required = true,
      converter = PathConverter.class
  )
  public Path mountPath;

  @Parameter(
      names = {"--cacheTime", "-t"},
      description = "Each value in this option is measured in seconds. To optimize DICOMFuse, the "
          + "following resources are cached: DICOM store folders, Study folders, Series folders, "
          + "list of Instances (first parameter in option), and opened Instances files (second "
          + "parameter). Opened Instance files are cached to the temporary folder in the user space "
          + "on disk. Other objects are cached to RAM. Cached files will be deleted if you close "
          + "DICOMFuse or if the cached files become out of date. If you delete an Instance file "
          + "locally, the cache will be updated. If you upload an Instance file, the cache will be "
          + "updated.",
      converter = CacheTimeConverter.class,
      validateWith = CacheTimePositiveValidator.class
  )
  public CacheTime cacheTime = new CacheTime(60, 300);

  @Parameter(
      names = {"--cacheSize", "-s"},
      description = "Maximum cache size in megabytes for cached instances. "
          + "The maximum file size that can be downloaded/uploaded is cacheSize / 4",
      converter = LongConverter.class,
      validateWith = CacheSizePositiveValidator.class
  )
  public long cacheSize = 10000;

  @Parameter(
      names = {"--enableDeletion", "-d"},
      description = "Some programs can delete files and deletion can also be done manually. "
          + "Sometimes accidental deletions can occur. If you don't want to delete files, you can "
          + "set --enableDeletion=false",
      converter = BooleanConverter.class
  )
  public boolean enableDeletion = true;

  @Parameter(
      names = {"--keyFile", "-k"},
      description = "Path to the account service key",
      converter = PathConverter.class
  )
  public Path keyPath;

  @Parameter(
      names = {"--help", "-h"},
      help = true,
      description = "Print help"
  )
  public boolean help = false;
}