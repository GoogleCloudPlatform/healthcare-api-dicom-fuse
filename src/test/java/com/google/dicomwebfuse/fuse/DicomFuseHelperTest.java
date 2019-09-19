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

package com.google.dicomwebfuse.fuse;

import static com.google.dicomwebfuse.TestUtils.prepareSimpleHttpResponse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.dicomwebfuse.auth.AuthAdc;
import com.google.dicomwebfuse.dao.FuseDao;
import com.google.dicomwebfuse.dao.FuseDaoImpl;
import com.google.dicomwebfuse.dao.http.HttpClientFactory;
import com.google.dicomwebfuse.entities.CloudConf;
import com.google.dicomwebfuse.entities.DicomPath;
import com.google.dicomwebfuse.exception.DicomFuseException;
import com.google.dicomwebfuse.fuse.cacher.DicomPathCacher;
import com.google.dicomwebfuse.parser.Arguments;
import java.io.IOException;
import java.util.Objects;
import jnr.ffi.Platform;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DicomFuseHelperTest {

  private static final String TEST = "test";
  private static AuthAdc authAdc;
  private static CloudConf cloudConf;
  private static DicomPath dicomPath;

  @BeforeAll
  static void setup() {
    cloudConf = new CloudConf(TEST, TEST, TEST, TEST);
    AccessToken accessToken = new AccessToken(TEST, null);
    GoogleCredentials googleCredentials = GoogleCredentials.create(accessToken);
    authAdc = new AuthAdc(googleCredentials);
  }

  @Test
  void testShouldSuccessfullyOpenUnlistedStudy() throws IOException, DicomFuseException {
    CloseableHttpClient closeableHttpClient = Mockito.mock(CloseableHttpClient.class);

    addMockStoreResponse(closeableHttpClient);
    addMockStudiesResponse(closeableHttpClient);
    addMockWrongStudyResponse(closeableHttpClient);

    HttpClientFactory httpClientFactory = Mockito.mock(HttpClientFactory.class);
    Mockito.when(httpClientFactory.createHttpClient()).thenReturn(closeableHttpClient);

    DicomPathCacher dicomPathCacher = new DicomPathCacher();
    DicomPathParser dicomPathParser = new DicomPathParser(dicomPathCacher);
    DicomFuseHelper dicomFuseHelper = prepareDicomFuseHelper(httpClientFactory);

    dicomPath = dicomPathParser.parsePath("/test");
    dicomFuseHelper.checkExistingObject(dicomPath);
    dicomFuseHelper.updateDir(dicomPath);
    dicomPath = dicomPathParser.parsePath("/test/123");

    assertThrows(DicomFuseException.class,
        () -> dicomFuseHelper.checkExistingObject(dicomPath));

    addMockStudyResponse(closeableHttpClient);
    dicomFuseHelper.checkExistingObject(dicomPath);
  }

  @Test
  void testShouldSuccessfullyOpenUnlistedSeries() throws IOException, DicomFuseException {
    CloseableHttpClient closeableHttpClient = Mockito.mock(CloseableHttpClient.class);

    addMockStoreResponse(closeableHttpClient);
    addMockStudiesResponse(closeableHttpClient);
    addMockStudyResponse(closeableHttpClient);
    addMockSeriesResponse(closeableHttpClient);
    addMockSingleSeriesResponse(closeableHttpClient);

    HttpClientFactory httpClientFactory = Mockito.mock(HttpClientFactory.class);
    Mockito.when(httpClientFactory.createHttpClient()).thenReturn(closeableHttpClient);

    DicomPathCacher dicomPathCacher = new DicomPathCacher();
    DicomPathParser dicomPathParser = new DicomPathParser(dicomPathCacher);
    DicomFuseHelper dicomFuseHelper = prepareDicomFuseHelper(httpClientFactory);

    dicomPath = dicomPathParser.parsePath("/test");
    dicomFuseHelper.checkExistingObject(dicomPath);
    dicomFuseHelper.updateDir(dicomPath);
    dicomPath = dicomPathParser.parsePath("/test/123");
    dicomFuseHelper.checkExistingObject(dicomPath);
    dicomFuseHelper.updateDir(dicomPath);
    dicomPath = dicomPathParser.parsePath("/test/123/1234");
    dicomFuseHelper.checkExistingObject(dicomPath);


  }

  @Test
  void testShouldSuccessfullyOpenUnlistedInstance() throws IOException, DicomFuseException {
    CloseableHttpClient closeableHttpClient = Mockito.mock(CloseableHttpClient.class);

    addMockStoreResponse(closeableHttpClient);
    addMockStudiesResponse(closeableHttpClient);
    addMockStudyResponse(closeableHttpClient);
    addMockSeriesResponse(closeableHttpClient);
    addMockSingleSeriesResponse(closeableHttpClient);
    addMockInstancesResponse(closeableHttpClient);
    addMockInstanceResponse(closeableHttpClient);

    HttpClientFactory httpClientFactory = Mockito.mock(HttpClientFactory.class);
    Mockito.when(httpClientFactory.createHttpClient()).thenReturn(closeableHttpClient);

    DicomPathCacher dicomPathCacher = new DicomPathCacher();
    DicomPathParser dicomPathParser = new DicomPathParser(dicomPathCacher);
    DicomFuseHelper dicomFuseHelper = prepareDicomFuseHelper(httpClientFactory);

    dicomPath = dicomPathParser.parsePath("/test");
    dicomFuseHelper.checkExistingObject(dicomPath);
    dicomFuseHelper.updateDir(dicomPath);
    dicomPath = dicomPathParser.parsePath("/test/123");
    dicomFuseHelper.checkExistingObject(dicomPath);
    dicomFuseHelper.updateDir(dicomPath);
    dicomPath = dicomPathParser.parsePath("/test/123/1234");
    dicomFuseHelper.checkExistingObject(dicomPath);
    dicomFuseHelper.updateDir(dicomPath);
    dicomPath = dicomPathParser.parsePath("/test/123/1234/12345");
    dicomFuseHelper.checkExistingObject(dicomPath);


  }

  DicomFuseHelper prepareDicomFuseHelper(HttpClientFactory httpClientFactory) {
    FuseDao fuseDao = new FuseDaoImpl(authAdc, httpClientFactory);
    Arguments arguments = new com.google.dicomwebfuse.parser.Arguments();
    arguments.cloudConf = cloudConf;
    Parameters parameters = new Parameters(fuseDao, arguments,
        Platform.getNativePlatform().getOS());
    DicomPathCacher dicomPathCacher = new DicomPathCacher();
    DicomFuseHelper dicomFuseHelper = new DicomFuseHelper(parameters, dicomPathCacher);
    return dicomFuseHelper;
  }

  void addMockResponse(CloseableHttpClient httpClient, CloseableHttpResponse httpResponse,
      String path, String query)
      throws IOException {
    Mockito.doReturn(httpResponse).when(httpClient).execute(argThat(
        (HttpGet get) -> get.getURI().getPath().equals(path) && Objects
            .equals(get.getURI().getQuery(), query)));
  }

  void addMockStoreResponse(CloseableHttpClient httpClient) throws IOException {
    CloseableHttpResponse storeResponse = prepareSimpleHttpResponse(200, "{\n"
        + "  \"dicomStores\": [\n"
        + "    {\n"
        + "      \"name\": \"projects/project/locations/location/datasets/dataset/dicomStores/test\"\n"
        + "    }\n"
        + "  ]\n"
        + "}");
    addMockResponse(httpClient, storeResponse,
        "/test/projects/test/locations/test/datasets/test/dicomStores/", null);
  }

  void addMockStudiesResponse(CloseableHttpClient httpClient) throws IOException {
    CloseableHttpResponse studiesResponse = prepareSimpleHttpResponse(200, "[\n"
        + "    {\n"
        + "        \"0020000D\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"1.2.276.0.7230010.3.1.2.2148188943.12.1559318675.533393\"\n"
        + "            ]\n"
        + "        }\n"
        + "    }\n"
        + "]\n");
    addMockResponse(httpClient, studiesResponse,
        "/test/projects/test/locations/test/datasets/test/dicomStores/test/dicomWeb/studies/",
        "limit=5000&offset=0");
  }

  void addMockStudyResponse(CloseableHttpClient httpClient) throws IOException {
    CloseableHttpResponse studyResponse = prepareSimpleHttpResponse(200, "[\n"
        + "    {\n"
        + "        \"0020000D\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"123\"\n"
        + "            ]\n"
        + "        }\n"
        + "    }\n"
        + "]\n");
    addMockResponse(httpClient, studyResponse,
        "/test/projects/test/locations/test/datasets/test/dicomStores/test/dicomWeb/studies/",
        "StudyInstanceUID=123");
  }

  void addMockWrongStudyResponse(CloseableHttpClient httpClient) throws IOException {
    CloseableHttpResponse studyResponse = prepareSimpleHttpResponse(200, "[\n"
        + "    {\n"
        + "        \"0020000D\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"321\"\n"
        + "            ]\n"
        + "        }\n"
        + "    }\n"
        + "]\n");
    addMockResponse(httpClient, studyResponse,
        "/test/projects/test/locations/test/datasets/test/dicomStores/test/dicomWeb/studies/",
        "StudyInstanceUID=123");
  }

  void addMockSeriesResponse(CloseableHttpClient httpClient) throws IOException {
    CloseableHttpResponse studiesResponse = prepareSimpleHttpResponse(200, "[\n"
        + "    {\n"
        + "        \"00080060\": {\n"
        + "            \"vr\": \"CS\",\n"
        + "            \"Value\": [\n"
        + "                \"SM\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"0008103E\": {\n"
        + "            \"vr\": \"LO\",\n"
        + "            \"Value\": [\n"
        + "                \"tumor\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"0020000D\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"123\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"0020000E\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"1.2.276.0.7230010.3.1.3.2148188943.12.1559318675.533394\"\n"
        + "            ]\n"
        + "        }\n"
        + "    }\n"
        + "]");
    addMockResponse(httpClient, studiesResponse,
        "/test/projects/test/locations/test/datasets/test/dicomStores/test/dicomWeb/studies/123/series/",
        "includefield=0020000D&limit=5000&offset=0");
  }

  void addMockSingleSeriesResponse(CloseableHttpClient httpClient) throws IOException {
    CloseableHttpResponse studiesResponse = prepareSimpleHttpResponse(200, "[\n"
        + "    {\n"
        + "        \"00080060\": {\n"
        + "            \"vr\": \"CS\",\n"
        + "            \"Value\": [\n"
        + "                \"SM\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"0008103E\": {\n"
        + "            \"vr\": \"LO\",\n"
        + "            \"Value\": [\n"
        + "                \"tumor\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"0020000D\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"123\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"0020000E\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"1234\"\n"
        + "            ]\n"
        + "        }\n"
        + "    }\n"
        + "]");
    addMockResponse(httpClient, studiesResponse,
        "/test/projects/test/locations/test/datasets/test/dicomStores/test/dicomWeb/studies/123/series/",
        "includefield=0020000D&SeriesInstanceUID=1234");
  }

  void addMockInstancesResponse(CloseableHttpClient httpClient) throws IOException {
    CloseableHttpResponse studiesResponse = prepareSimpleHttpResponse(200, "[\n"
        + "    {\n"
        + "        \"00080016\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"1.2.840.10008.5.1.4.1.1.77.1.6\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"00080018\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"1.2.276.0.7230010.3.1.4.2148188943.12.1559318703.533395\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"0020000D\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"123\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"0020000E\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"1234\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"00200013\": {\n"
        + "            \"vr\": \"IS\",\n"
        + "            \"Value\": [\n"
        + "                1\n"
        + "            ]\n"
        + "        },\n"
        + "        \"00280008\": {\n"
        + "            \"vr\": \"IS\",\n"
        + "            \"Value\": [\n"
        + "                2048\n"
        + "            ]\n"
        + "        },\n"
        + "        \"00280010\": {\n"
        + "            \"vr\": \"US\",\n"
        + "            \"Value\": [\n"
        + "                500\n"
        + "            ]\n"
        + "        },\n"
        + "        \"00280011\": {\n"
        + "            \"vr\": \"US\",\n"
        + "            \"Value\": [\n"
        + "                500\n"
        + "            ]\n"
        + "        },\n"
        + "        \"00280100\": {\n"
        + "            \"vr\": \"US\",\n"
        + "            \"Value\": [\n"
        + "                8\n"
        + "            ]\n"
        + "        }\n"
        + "    }\n"
        + "]");
    addMockResponse(httpClient, studiesResponse,
        "/test/projects/test/locations/test/datasets/test/dicomStores/test/dicomWeb/studies/123/series/1234/instances/",
        "includefield=0020000D&includefield=0020000E&limit=15000&offset=0");
  }

  void addMockInstanceResponse(CloseableHttpClient httpClient) throws IOException {
    CloseableHttpResponse studiesResponse = prepareSimpleHttpResponse(200, "[\n"
        + "    {\n"
        + "        \"00080016\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"1.2.840.10008.5.1.4.1.1.77.1.6\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"00080018\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"12345\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"0020000D\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"123\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"0020000E\": {\n"
        + "            \"vr\": \"UI\",\n"
        + "            \"Value\": [\n"
        + "                \"1234\"\n"
        + "            ]\n"
        + "        },\n"
        + "        \"00200013\": {\n"
        + "            \"vr\": \"IS\",\n"
        + "            \"Value\": [\n"
        + "                1\n"
        + "            ]\n"
        + "        },\n"
        + "        \"00280008\": {\n"
        + "            \"vr\": \"IS\",\n"
        + "            \"Value\": [\n"
        + "                2048\n"
        + "            ]\n"
        + "        },\n"
        + "        \"00280010\": {\n"
        + "            \"vr\": \"US\",\n"
        + "            \"Value\": [\n"
        + "                500\n"
        + "            ]\n"
        + "        },\n"
        + "        \"00280011\": {\n"
        + "            \"vr\": \"US\",\n"
        + "            \"Value\": [\n"
        + "                500\n"
        + "            ]\n"
        + "        },\n"
        + "        \"00280100\": {\n"
        + "            \"vr\": \"US\",\n"
        + "            \"Value\": [\n"
        + "                8\n"
        + "            ]\n"
        + "        }\n"
        + "    }\n"
        + "]");
    addMockResponse(httpClient, studiesResponse,
        "/test/projects/test/locations/test/datasets/test/dicomStores/test/dicomWeb/studies/123/series/1234/instances/",
        "includefield=0020000D&includefield=0020000E&SOPInstanceUID=12345");
  }


}
