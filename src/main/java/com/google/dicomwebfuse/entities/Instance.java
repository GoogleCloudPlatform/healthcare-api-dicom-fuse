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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Instance {

  @JsonProperty("0020000D")
  private DicomAttribute<String> studyInstanceUID;
  @JsonProperty("0020000E")
  private DicomAttribute<String> seriesInstanceUID;
  @JsonProperty("00080018")
  private DicomAttribute<String> sopInstanceUID;

  public DicomAttribute<String> getStudyInstanceUID() {
    return studyInstanceUID;
  }

  public void setStudyInstanceUID(DicomAttribute<String> studyInstanceUID) {
    this.studyInstanceUID = studyInstanceUID;
  }

  public DicomAttribute<String> getSeriesInstanceUID() {
    return seriesInstanceUID;
  }

  public void setSeriesInstanceUID(DicomAttribute<String> seriesInstanceUID) {
    this.seriesInstanceUID = seriesInstanceUID;
  }

  public DicomAttribute<String> getSopInstanceUID() {
    return sopInstanceUID;
  }

  public void setSopInstanceUID(DicomAttribute<String> sopInstanceUID) {
    this.sopInstanceUID = sopInstanceUID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Instance instance = (Instance) o;
    return studyInstanceUID.equals(instance.studyInstanceUID) &&
        seriesInstanceUID.equals(instance.seriesInstanceUID) &&
        sopInstanceUID.equals(instance.sopInstanceUID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(studyInstanceUID, seriesInstanceUID, sopInstanceUID);
  }
}
