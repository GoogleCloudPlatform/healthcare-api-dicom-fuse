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


import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;


class StowErrorFormatterTest {

  @Test
  void testEmptyBodyShouldNotBeParsed() {
    assertEquals("", StowErrorFormatter.formatByMimeType("", ""));
  }

  @Test
  void testDicomXmlErrorShouldBeParsed() {
    String rawString = "<NativeDicomModel><DicomAttribute tag=\"00081190\" "
        + "vr=\"UR\" keyword=\"RetrieveURL\"><Value number=\"1\">"
        + "https://healthcare.googleapis.com/</Value></DicomAttribute>"
        + "</NativeDicomModel>";
    String prettyString = "<NativeDicomModel>\n"
        + " <DicomAttribute tag=\"00081190\" vr=\"UR\" keyword=\"RetrieveURL\">\n"
        + "  <Value number=\"1\">\n"
        + "   https://healthcare.googleapis.com/\n"
        + "  </Value>\n"
        + " </DicomAttribute>\n"
        + "</NativeDicomModel>";

    assertEquals(prettyString,
        StowErrorFormatter.formatByMimeType(rawString, "application/dicom+xml"
        ));
  }

  @Test
  void testJsonErrorShouldNotBeParsed() {
    String jsonString = "  {"
        + "    \"error\": {"
        + "      \"code\": 401,"
        + "      \"message\": \"Request had invalid auth\","
        + "      \"status\": \"UNAUTHENTICATED\""
        + "    }"
        + "  }";
    String jsonStringWithNewLines = "  {\n"
        + "    \"error\": {\n"
        + "      \"code\": 401,\n"
        + "      \"message\": \"Request had invalid auth\",\n"
        + "      \"status\": \"UNAUTHENTICATED\"\n"
        + "    }\n"
        + "  }";

    assertEquals(jsonStringWithNewLines,
        StowErrorFormatter
            .formatByMimeType(jsonStringWithNewLines, "application/json"
            ));
    assertEquals(jsonString,
        StowErrorFormatter.formatByMimeType(jsonString, "application/json"
        ));
  }

  @Test
  void testPlainTextShouldNotBeParsed() {
    String plaintext = "plain text error";
    assertEquals(plaintext,
        StowErrorFormatter.formatByMimeType(plaintext, "something unknown"
        ));
  }
}
