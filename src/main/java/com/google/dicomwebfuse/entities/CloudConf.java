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

package com.google.dicomwebfuse.entities;

public class CloudConf {

  private String stage;
  private String project;
  private String location;
  private String dataSet;

  public CloudConf(String stage, String project, String location, String dataSet) {
    this.stage = stage;
    this.project = project;
    this.location = location;
    this.dataSet = dataSet;
  }

  public String getStage() {
    return stage;
  }

  public String getProject() {
    return project;
  }

  public String getLocation() {
    return location;
  }

  public String getDataSet() {
    return dataSet;
  }
}
