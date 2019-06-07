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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class DicomStore {

  private String dicomStoreId;

  public DicomStore() {
  }

  @JsonCreator
  public DicomStore(@JsonProperty("name") String name) {
    String[] arr = name.split("/");
    this.dicomStoreId = arr[arr.length - 1];
  }

  public String getDicomStoreId() {
    return dicomStoreId;
  }

  public void setDicomStoreId(String dicomStoreId) {
    this.dicomStoreId = dicomStoreId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DicomStore that = (DicomStore) o;
    return dicomStoreId.equals(that.dicomStoreId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dicomStoreId);
  }
}
