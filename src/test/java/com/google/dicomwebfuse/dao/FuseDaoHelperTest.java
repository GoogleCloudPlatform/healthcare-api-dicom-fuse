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

import static com.google.dicomwebfuse.dao.Constants.MAX_INSTANCES_IN_SERIES;
import static com.google.dicomwebfuse.dao.Constants.MAX_SERIES_IN_STUDY;
import static com.google.dicomwebfuse.dao.Constants.MAX_STUDIES_IN_DICOM_STORE;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_MAX_LIMIT_FOR_SERIES;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_MAX_LIMIT_FOR_STUDY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.api.client.http.HttpStatusCodes;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.dicomwebfuse.auth.AuthAdc;
import com.google.dicomwebfuse.dao.http.HttpClientFactory;
import com.google.dicomwebfuse.entities.CloudConf;
import com.google.dicomwebfuse.entities.DicomPath;
import com.google.dicomwebfuse.entities.DicomPathLevel;
import com.google.dicomwebfuse.entities.Instance;
import com.google.dicomwebfuse.entities.Series;
import com.google.dicomwebfuse.entities.Study;
import com.google.dicomwebfuse.exception.DicomFuseException;
import com.google.dicomwebfuse.exception.ToManyResultsException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class FuseDaoHelperTest {

  private static final String TEST = "test";
  private static final String STUDY_CONTENT = "{\"0020000D\":{\"vr\":\"UI\",\"Value\":[\"1\"]}}";
  private static final String SERIES_CONTENT = "{\"0020000E\":{\"vr\":\"UI\",\"Value\":[\"1\"]}}";
  private static final String INSTANCE_CONTENT = "{\"00080018\":{\"vr\":\"UI\",\"Value\":[\"1\"]}}";
  private static AuthAdc authAdc;
  private static CloudConf cloudConf;
  private static DicomPath dicomPath;

  @BeforeAll
  static void setup() {
    cloudConf = new CloudConf(TEST, TEST, TEST, TEST);
    dicomPath = new DicomPath.Builder(DicomPathLevel.INSTANCE)
        .dicomStoreId(TEST)
        .studyInstanceUID(TEST)
        .seriesInstanceUID(TEST)
        .sopInstanceUID(TEST)
        .fileName(TEST)
        .build();
    AccessToken accessToken = new AccessToken(TEST, null);
    GoogleCredentials googleCredentials = GoogleCredentials.create(accessToken);
    authAdc = new AuthAdc(googleCredentials);
  }

  @Test
  void testShouldReturnFourThousandStudiesIfStudiesCountFourThousand()
      throws DicomFuseException, IOException {
    // given
    int expectedStudiesCount = 4000;
    int expectedStatusCode = HttpStatusCodes.STATUS_CODE_OK;

    HttpClientFactory httpClientFactory = prepareHttpClient(expectedStudiesCount,
        MAX_STUDIES_IN_DICOM_STORE, VALUE_PARAM_MAX_LIMIT_FOR_STUDY, STUDY_CONTENT,
        expectedStatusCode);
    FuseDao fuseDao = new FuseDaoImpl(authAdc, httpClientFactory);
    // when
    List<Study> actualStudyList = FuseDaoHelper.getStudies(fuseDao, cloudConf, dicomPath);
    // then
    assertEquals(expectedStudiesCount, actualStudyList.size());
  }

  @Test
  void testShouldReturnFiveThousandStudiesIfStudiesCountFiveThousand()
      throws DicomFuseException, IOException {
    // given
    int expectedStudiesCount = VALUE_PARAM_MAX_LIMIT_FOR_STUDY;
    int expectedStatusCode = HttpStatusCodes.STATUS_CODE_OK;

    HttpClientFactory httpClientFactory = prepareHttpClient(expectedStudiesCount,
        MAX_STUDIES_IN_DICOM_STORE, VALUE_PARAM_MAX_LIMIT_FOR_STUDY, STUDY_CONTENT,
        expectedStatusCode);
    FuseDao fuseDao = new FuseDaoImpl(authAdc, httpClientFactory);
    // when
    List<Study> actualStudyList = FuseDaoHelper.getStudies(fuseDao, cloudConf, dicomPath);
    // then
    assertEquals(expectedStudiesCount, actualStudyList.size());
  }

  @Test
  void testShouldReturnExceptionIfStudiesCountAboveMaxStudiesInDicomStore() throws IOException {
    // given
    int expectedStudiesCount = MAX_STUDIES_IN_DICOM_STORE + 1;
    int expectedStatusCode = HttpStatusCodes.STATUS_CODE_OK;

    HttpClientFactory httpClientFactory = prepareHttpClient(expectedStudiesCount,
        MAX_STUDIES_IN_DICOM_STORE, VALUE_PARAM_MAX_LIMIT_FOR_STUDY, STUDY_CONTENT,
        expectedStatusCode);
    FuseDao fuseDao = new FuseDaoImpl(authAdc, httpClientFactory);
    // then
    assertThrows(ToManyResultsException.class,
        () -> FuseDaoHelper.getStudies(fuseDao, cloudConf, dicomPath));
  }

  @Test
  void testShouldReturnFourThousandSeriesIfSeriesCountFourThousand()
      throws DicomFuseException, IOException {
    // given
    int expectedSeriesCount = 4000;
    int expectedStatusCode = HttpStatusCodes.STATUS_CODE_OK;

    HttpClientFactory httpClientFactory = prepareHttpClient(expectedSeriesCount,
        MAX_SERIES_IN_STUDY, VALUE_PARAM_MAX_LIMIT_FOR_SERIES, SERIES_CONTENT, expectedStatusCode);
    FuseDao fuseDao = new FuseDaoImpl(authAdc, httpClientFactory);
    // when
    List<Series> actualSeriesList = FuseDaoHelper.getSeries(fuseDao, cloudConf, dicomPath);
    // then
    assertEquals(expectedSeriesCount, actualSeriesList.size());
  }

  @Test
  void testShouldReturnFiveThousandSeriesIfSeriesCountFiveThousand()
      throws DicomFuseException, IOException {
    // given
    int expectedSeriesCount = VALUE_PARAM_MAX_LIMIT_FOR_SERIES;
    int expectedStatusCode = HttpStatusCodes.STATUS_CODE_OK;

    HttpClientFactory httpClientFactory = prepareHttpClient(expectedSeriesCount,
        MAX_SERIES_IN_STUDY, VALUE_PARAM_MAX_LIMIT_FOR_SERIES, SERIES_CONTENT, expectedStatusCode);
    FuseDao fuseDao = new FuseDaoImpl(authAdc, httpClientFactory);
    // when
    List<Series> actualSeriesList = FuseDaoHelper.getSeries(fuseDao, cloudConf, dicomPath);
    // then
    assertEquals(expectedSeriesCount, actualSeriesList.size());
  }

  @Test
  void testShouldReturnExceptionIfSeriesCountAboveMaxSeriesInStudy() throws IOException {
    // given
    int expectedSeriesCount = MAX_SERIES_IN_STUDY + 1;
    int expectedStatusCode = HttpStatusCodes.STATUS_CODE_OK;

    HttpClientFactory httpClientFactory = prepareHttpClient(expectedSeriesCount,
        MAX_SERIES_IN_STUDY, VALUE_PARAM_MAX_LIMIT_FOR_SERIES, SERIES_CONTENT, expectedStatusCode);
    FuseDao fuseDao = new FuseDaoImpl(authAdc, httpClientFactory);
    // then
    assertThrows(ToManyResultsException.class,
        () -> FuseDaoHelper.getSeries(fuseDao, cloudConf, dicomPath));
  }

  @Test
  void testShouldReturnFifteenThousandInstancesIfInstancesCountFifteenThousand()
      throws DicomFuseException, IOException {
    // given
    int expectedInstancesCount = VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES;
    int expectedStatusCode = HttpStatusCodes.STATUS_CODE_OK;

    HttpClientFactory httpClientFactory = prepareHttpClient(expectedInstancesCount,
        MAX_INSTANCES_IN_SERIES, VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES, INSTANCE_CONTENT,
        expectedStatusCode);
    FuseDao fuseDao = new FuseDaoImpl(authAdc, httpClientFactory);
    // when
    List<Instance> actualInstancesList = FuseDaoHelper.getInstances(fuseDao, cloudConf, dicomPath);
    // then
    assertEquals(expectedInstancesCount, actualInstancesList.size());
  }

  @Test
  void testShouldReturnExceptionIfInstancesCountAboveMaxInstancesInSeries() throws IOException {
    // given
    int expectedInstancesCount = MAX_INSTANCES_IN_SERIES + 1;
    int expectedStatusCode = HttpStatusCodes.STATUS_CODE_OK;

    HttpClientFactory httpClientFactory = prepareHttpClient(expectedInstancesCount,
        MAX_INSTANCES_IN_SERIES, VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES, INSTANCE_CONTENT,
        expectedStatusCode);
    FuseDao fuseDao = new FuseDaoImpl(authAdc, httpClientFactory);
    // then
    assertThrows(ToManyResultsException.class,
        () -> FuseDaoHelper.getInstances(fuseDao, cloudConf, dicomPath));
  }

  private HttpClientFactory prepareHttpClient(int entityCount, int maxEntities, int entityLimit,
      String entityContent, int statusCode) throws IOException {

    int iterationCount = (int) Math.ceil((double) maxEntities / entityLimit);
    List<BasicHttpEntity> basicHttpEntities = new ArrayList<>();
    if (entityCount < entityLimit) {
      BasicHttpEntity httpEntity = prepareHttpEntityWithContent(entityCount, entityContent);
      basicHttpEntities.add(httpEntity);
    } else if (entityCount == entityLimit) {
      BasicHttpEntity httpEntity = prepareHttpEntityWithContent(entityCount, entityContent);
      basicHttpEntities.add(httpEntity);
      for (int i = 0; i < iterationCount; i++) {
        BasicHttpEntity emptyHttpEntity = prepareEmptyHttpEntity();
        basicHttpEntities.add(emptyHttpEntity);
      }
    } else {
      for (int i = 0, count = entityCount; i < iterationCount + 1; i++, count -= entityLimit) {
        if (count > 0 && count < entityLimit) {
          BasicHttpEntity httpEntityCallable = prepareHttpEntityWithContent(count, entityContent);
          basicHttpEntities.add(httpEntityCallable);
        } else if (count > 0 && count >= entityLimit) {
          BasicHttpEntity httpEntityCallable = prepareHttpEntityWithContent(entityLimit, entityContent);
          basicHttpEntities.add(httpEntityCallable);
        } else {
          BasicHttpEntity emptyHttpEntity = prepareEmptyHttpEntity();
          basicHttpEntities.add(emptyHttpEntity);
        }
      }
    }

    CloseableHttpResponse httpResponse = prepareHttpResponse(statusCode, basicHttpEntities);
    CloseableHttpClient closeableHttpClient = Mockito.mock(CloseableHttpClient.class);
    Mockito.when(closeableHttpClient.execute(ArgumentMatchers.any())).thenReturn(httpResponse);

    HttpClientFactory httpClientFactory = Mockito.mock(HttpClientFactory.class);
    Mockito.when(httpClientFactory.createHttpClient()).thenReturn(closeableHttpClient);
    return httpClientFactory;
  }

  private BasicHttpEntity prepareHttpEntityWithContent(int entityCount, String entityContent) {
    BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("[");
    for (int j = 1; j <= entityCount; j++) {
      stringBuilder.append(entityContent);
      if (j != entityCount) {
        stringBuilder.append(",");
      }
    }
    stringBuilder.append("]");
    basicHttpEntity.setContent(
        new ByteArrayInputStream(stringBuilder.toString().getBytes(StandardCharsets.UTF_8)));
    return basicHttpEntity;
  }

  private BasicHttpEntity prepareEmptyHttpEntity() {
    BasicHttpEntity emptyHttpEntity = new BasicHttpEntity();
    String emptyContent = "[]";
    emptyHttpEntity.setContent(
        new ByteArrayInputStream(emptyContent.getBytes(StandardCharsets.UTF_8)));
    return emptyHttpEntity;
  }

  private CloseableHttpResponse prepareHttpResponse(int statusCode,
      List<BasicHttpEntity> httpEntities) {
    ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
    BasicStatusLine basicStatusLine = new BasicStatusLine(protocolVersion, statusCode, "");
    CloseableHttpResponse closeableHttpResponse = Mockito.mock(CloseableHttpResponse.class);
    Mockito.when(closeableHttpResponse.getStatusLine()).thenReturn(basicStatusLine);

    if (httpEntities.size() == 1) {
      Mockito.when(closeableHttpResponse.getEntity()).thenReturn(httpEntities.get(0));
    } else {
      BasicHttpEntity[] array = new BasicHttpEntity[httpEntities.size() - 1];
      array = httpEntities.toArray(array);
      BasicHttpEntity[] entities = Arrays.copyOfRange(array, 1, array.length);
      Mockito.when(closeableHttpResponse.getEntity()).thenReturn(httpEntities.get(0), entities);
    }
    return closeableHttpResponse;
  }
}