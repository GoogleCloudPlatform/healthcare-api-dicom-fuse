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

package com.google.dicomwebfuse.exception;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

public class StowErrorFormatter {

  public static final String CONTENT_TYPE_DICOM_XML = "application/dicom+xml";

  public static String formatByMimeType(String errorBody, String mimeType){
    String formattedError;
    switch (mimeType) {
      case CONTENT_TYPE_DICOM_XML:
        formattedError = Jsoup.parse(errorBody, "", Parser.xmlParser())
            .toString();
        break;
      default:
        formattedError = errorBody;
    }
    return formattedError;
  }
}
