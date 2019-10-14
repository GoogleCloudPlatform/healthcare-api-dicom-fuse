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

package com.google.dicomwebfuse;

import static com.google.dicomwebfuse.EntityType.DICOM_STORE;
import static com.google.dicomwebfuse.dao.Constants.MAX_INSTANCES_IN_SERIES;
import static com.google.dicomwebfuse.dao.Constants.MAX_SERIES_IN_STUDY;
import static com.google.dicomwebfuse.dao.Constants.MAX_STUDIES_IN_DICOM_STORE;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_MAX_LIMIT_FOR_SERIES;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_MAX_LIMIT_FOR_STUDY;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.dicomwebfuse.auth.AuthAdc;
import com.google.dicomwebfuse.dao.http.HttpClientFactory;
import com.google.dicomwebfuse.exception.DicomFuseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Util class with helper methods for unit tests.
 */
public class TestUtils {

  /**
   * Generates mocked HttpClientFactory.
   *
   * @param closeableHttpClient mocked CloseableHttpClient
   * @return generated HttpClientFactory
   */
  public static HttpClientFactory prepareHttpClientFactory(
      CloseableHttpClient closeableHttpClient) {
    HttpClientFactory httpClientFactory = Mockito.mock(HttpClientFactory.class);
    Mockito.when(httpClientFactory.createHttpClient()).thenReturn(closeableHttpClient);
    return httpClientFactory;
  }

  /**
   * Prepares content for closeableHttpClient.
   *
   * @param closeableHttpClient mocked CloseableHttpClient
   * @param entityCount how many entities would be generated
   * @param entityType type of entity
   * @param statusCode http status code
   */
  public static void prepareHttpClient(CloseableHttpClient closeableHttpClient, int entityCount,
      EntityType entityType, int statusCode) throws IOException, DicomFuseException {
    prepareHttpClient(closeableHttpClient, entityCount, 0, entityType, statusCode, null, null);
  }

  /**
   * Prepares content for closeableHttpClient.
   *
   * @param closeableHttpClient mocked CloseableHttpClient
   * @param entityCount how many entities would be generated
   * @param offset start offset
   * @param entityType type of entity
   * @param statusCode http status code
   * @param path http path for request
   * @param query http query for request
   */
  public static void prepareHttpClient(CloseableHttpClient closeableHttpClient, int entityCount,
      int offset, EntityType entityType, int statusCode, String path, String query)
      throws IOException, DicomFuseException {
    List<BasicHttpEntity> basicHttpEntities = prepareHttpEntities(entityCount, offset, entityType);
    CloseableHttpResponse httpResponse = prepareHttpResponse(statusCode, basicHttpEntities);
    Mockito.doReturn(httpResponse)
        .when(closeableHttpClient)
        .execute(ArgumentMatchers.argThat((HttpUriRequest request) ->
            query == null || path == null ||
                request.getURI().getPath().equals(path) && query.equals(request.getURI().getQuery())
        ));
  }

  /**
   * Generates AuthAdc with a mocked token.
   *
   * @param token mocked token
   * @return generated AuthAdc
   */
  public static AuthAdc prepareAuthAdc(String token) {
    AccessToken accessToken = new AccessToken(token, null);
    GoogleCredentials googleCredentials = GoogleCredentials.create(accessToken);
    return new AuthAdc(googleCredentials);
  }

  /**
   * Generates http entities list.
   *
   * @param entityCount how many entities would be generated
   * @param offset start offset
   * @param entityType  type of entity
   */
  private static List<BasicHttpEntity> prepareHttpEntities(int entityCount, int offset, EntityType entityType)
      throws DicomFuseException {
    List<BasicHttpEntity> basicHttpEntities = new ArrayList<>();
    int entityLimit;
    int maxEntities;
    switch (entityType) {
      case DICOM_STORE:
        entityLimit = 100;
        maxEntities = 100;
        break;
      case STUDY:
        entityLimit = VALUE_PARAM_MAX_LIMIT_FOR_STUDY;
        maxEntities = MAX_STUDIES_IN_DICOM_STORE;
        break;
      case SERIES:
        entityLimit = VALUE_PARAM_MAX_LIMIT_FOR_SERIES;
        maxEntities = MAX_SERIES_IN_STUDY;
        break;
      case INSTANCE:
        entityLimit = VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES;
        maxEntities = MAX_INSTANCES_IN_SERIES;
        break;
      default:
        throw new DicomFuseException("Error");
    }
    int iterationCount = (int) Math.ceil((double) maxEntities / entityLimit) + 1;

    int startOfEntityId = offset +1;
    if (entityCount < entityLimit) {
      BasicHttpEntity httpEntity = prepareHttpEntityWithContent(entityCount, entityType, startOfEntityId);
      basicHttpEntities.add(httpEntity);
      return basicHttpEntities;
    }

    if (entityCount == entityLimit) {
      BasicHttpEntity httpEntity = prepareHttpEntityWithContent(entityCount, entityType, startOfEntityId);
      basicHttpEntities.add(httpEntity);
      for (int i = 0; i < iterationCount; i++) {
        BasicHttpEntity emptyHttpEntity = prepareEmptyHttpEntity();
        basicHttpEntities.add(emptyHttpEntity);
      }
      return basicHttpEntities;
    }

    for (int i = 1, count = entityCount;
        i <= iterationCount;
        i++, count -= entityLimit, startOfEntityId += entityLimit) {

      if (count > 0 && count >= entityLimit) {
        BasicHttpEntity httpEntityCallable = prepareHttpEntityWithContent(entityLimit, entityType,
            startOfEntityId);
        basicHttpEntities.add(httpEntityCallable);
      }

      if (count > 0 && count < entityLimit) {
        BasicHttpEntity httpEntityCallable = prepareHttpEntityWithContent(count, entityType,
            startOfEntityId);
        basicHttpEntities.add(httpEntityCallable);
      }

      if (count <= 0) {
        BasicHttpEntity emptyHttpEntity = prepareEmptyHttpEntity();
        basicHttpEntities.add(emptyHttpEntity);
      }

    }

    return basicHttpEntities;
  }

  /**
   * Generates BasicHttpEntity with unique JSON entities content.
   *
   * @param entityCount size of array
   * @param entityType  type of entity
   * @param startOfEntityId starting ID from which the array of entities starts
   * @return entity object
   */
  private static BasicHttpEntity prepareHttpEntityWithContent(int entityCount,
      EntityType entityType, int startOfEntityId) throws DicomFuseException {
    BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
    StringBuilder stringBuilder = new StringBuilder();
    if (entityType == DICOM_STORE) {
      stringBuilder.append("{\"dicomStores\":[");
    } else {
      stringBuilder.append("[");
    }
    int count = startOfEntityId + entityCount;
    for (int i = startOfEntityId; i < count; i++) {
      switch (entityType) {
        case DICOM_STORE:
          stringBuilder
              .append("{\"name\":\"projects/test/locations/test/datasets/test/dicomStores/test");
          stringBuilder.append(i);
          stringBuilder.append("\"}");
          break;
        case STUDY:
          stringBuilder.append("{\"0020000D\":{\"vr\":\"UI\",\"Value\":[\"");
          stringBuilder.append(i);
          stringBuilder.append("\"]}}");
          break;
        case SERIES:
          stringBuilder.append("{\"0020000E\":{\"vr\":\"UI\",\"Value\":[\"");
          stringBuilder.append(i);
          stringBuilder.append("\"]}}");
          break;
        case INSTANCE:
          stringBuilder.append("{\"00080018\":{\"vr\":\"UI\",\"Value\":[\"");
          stringBuilder.append(i);
          stringBuilder.append("\"]}}");
          break;
        default:
          throw new DicomFuseException("Error");
      }
      if (i != count - 1) {
        stringBuilder.append(",");
      }
    }
    if (entityType == DICOM_STORE) {
      stringBuilder.append("]}");
    } else {
      stringBuilder.append("]");
    }
    basicHttpEntity.setContent(new ByteArrayInputStream(stringBuilder.toString().getBytes(UTF_8)));
    return basicHttpEntity;
  }

  /**
   * Generates BasicHttpEntity with with empty content.
   *
   * @return entity object
   */
  private static BasicHttpEntity prepareEmptyHttpEntity() {
    BasicHttpEntity emptyHttpEntity = new BasicHttpEntity();
    emptyHttpEntity.setContent(new ByteArrayInputStream("[]".getBytes(UTF_8)));
    return emptyHttpEntity;
  }

  /**
   * Generates CloseableHttpResponse with multiple entities.
   *
   * @param statusCode Http response code
   * @param httpEntities list of entities
   * @return response object
   */
  private static CloseableHttpResponse prepareHttpResponse(int statusCode,
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
