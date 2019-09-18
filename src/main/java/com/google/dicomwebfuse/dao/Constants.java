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

package com.google.dicomwebfuse.dao;

public class Constants {

  static final String SCHEME = "https";
  static final String HEALTHCARE_HOST = "healthcare.googleapis.com";
  public static final String PROJECTS = "/projects/";
  public static final String LOCATIONS = "/locations/";
  public static final String DATASETS = "/datasets/";
  public static final String DICOM_STORES = "/dicomStores/";
  public static final String DICOM_WEB = "/dicomWeb";
  public static final String STUDIES = "/studies/";
  public static final String SERIES = "/series/";
  public static final String INSTANCES = "/instances/";

  static final String PARAM_PAGE_TOKEN = "pageToken";
  static final String PARAM_INCLUDE_FIELD = "includefield";
  static final String PARAM_LIMIT = "limit";
  static final String PARAM_OFFSET = "offset";
  static final String VALUE_PARAM_STUDY_INSTANCE_UID = "0020000D";
  static final String VALUE_PARAM_SERIES_INSTANCE_UID = "0020000E";
  static final Integer VALUE_PARAM_MAX_LIMIT_FOR_STUDY = 5000;
  static final Integer VALUE_PARAM_MAX_LIMIT_FOR_SERIES = 5000;
  static final Integer VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES = 15000; // max - 50 000 results
  static final Integer MAX_STUDIES_IN_DICOM_STORE = VALUE_PARAM_MAX_LIMIT_FOR_STUDY * 3;
  static final Integer MAX_SERIES_IN_STUDY = VALUE_PARAM_MAX_LIMIT_FOR_SERIES * 3;
  static final Integer MAX_INSTANCES_IN_SERIES = VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES;
  static final Integer THREAD_COUNT = 3;

  static final String BEARER = "Bearer ";
  static final String APPLICATION_JSON_CHARSET_UTF8 = "application/json; charset=utf-8";
  static final String APPLICATION_DICOM_TRANSFER_SYNTAX = "application/dicom; transfer-syntax=*";
  static final String APPLICATION_DICOM_JSON_CHARSET_UTF8 = "application/dicom+json; charset=utf-8";
  static final String MULTIPART_RELATED_TYPE_APPLICATION_DICOM_BOUNDARY =
      "multipart/related; type=application/dicom; boundary=";
}