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

package com.google.dicomwebfuse.auth;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class AuthAdc {

  private static final List<String> SCOPES = Collections.singletonList(
      "https://www.googleapis.com/auth/cloud-healthcare"
  );

  private GoogleCredentials credentials;

  public void createCredentials(Path keyPath) throws IOException {
    InputStream inputStream = new FileInputStream(keyPath.toFile());
    credentials = GoogleCredentials.fromStream(inputStream).createScoped(SCOPES);
  }

  public void createCredentials() throws IOException {
    credentials = GoogleCredentials.getApplicationDefault().createScoped(SCOPES);
  }

  public GoogleCredentials getCredentials() throws IOException {
    credentials.getRequestMetadata(); // Update token if it necessary.
    return credentials;
  }
}
