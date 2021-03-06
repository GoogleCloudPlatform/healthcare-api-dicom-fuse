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

import com.beust.jcommander.IStringConverter;
import com.google.dicomwebfuse.entities.cache.CacheTime;

public class CacheTimeConverter implements IStringConverter<CacheTime> {

  @Override
  public CacheTime convert(String parameters) {
    String[] param = parameters.split(",");
    return new CacheTime(
        Long.parseLong(param[0]),
        Long.parseLong(param[1])
    );
  }
}
