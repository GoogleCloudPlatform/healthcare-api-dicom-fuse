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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.Objects;

public class DicomAttribute<T> {

  private String vr;
  @JsonProperty
  private T[] Value;

  public String getVr() {
    return vr;
  }

  public void setVr(String vr) {
    this.vr = vr;
  }

  public void setValue(T[] Value) {
    this.Value = Value;
  }

  @JsonIgnore
  public T getValue1() {
    return Value[0];
  }

  @JsonIgnore
  public T getValue2() {
    return Value[1];
  }

  @JsonIgnore
  public T[] getValue() {
    return Value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DicomAttribute<?> that = (DicomAttribute<?>) o;
    return Objects.equals(vr, that.vr) &&
        Arrays.equals(Value, that.Value);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(vr);
    result = 31 * result + Arrays.hashCode(Value);
    return result;
  }
}
