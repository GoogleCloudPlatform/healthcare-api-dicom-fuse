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

import com.google.dicomwebfuse.dao.http.HttpClientFactory;
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
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Util class with helper methods for unit tests
 */
public class TestUtils {

  /**
   * Generates http client with mocked responses
   *
   * @param entityCount how many entities would be generated
   * @param maxEntities top limit of entities which may be displayed
   * @param entityLimit limit of entities for each response
   * @param entityContent String content of http entity
   * @param statusCode http status code
   *
   */
  static public HttpClientFactory prepareHttpClient(int entityCount, int maxEntities,
      int entityLimit, String entityContent, int statusCode) throws IOException {

    List<BasicHttpEntity> basicHttpEntities = prepareHttpEntities(entityCount, maxEntities,
        entityLimit, entityContent);

    CloseableHttpResponse httpResponse = prepareHttpResponse(statusCode, basicHttpEntities);
    CloseableHttpClient closeableHttpClient = Mockito.mock(CloseableHttpClient.class);
    Mockito.when(closeableHttpClient.execute(ArgumentMatchers.any())).thenReturn(httpResponse);

    HttpClientFactory httpClientFactory = Mockito.mock(HttpClientFactory.class);
    Mockito.when(httpClientFactory.createHttpClient()).thenReturn(closeableHttpClient);
    return httpClientFactory;
  }

  /**
   * Generates http entities list
   *
   * @param entityCount how many entities would be generated
   * @param maxEntities top limit of entities which may be displayed
   * @param entityLimit limit of entities for each response
   * @param entityContent String content of http entity
   *
   */
  private static List<BasicHttpEntity> prepareHttpEntities(int entityCount, int maxEntities,
      int entityLimit, String entityContent) {
    List<BasicHttpEntity> basicHttpEntities = new ArrayList<>();
    int iterationCount = (int) Math.ceil((double) maxEntities / entityLimit);

    if (entityCount < entityLimit) {
      BasicHttpEntity httpEntity = prepareHttpEntityWithContent(entityCount, entityContent);
      basicHttpEntities.add(httpEntity);
      return basicHttpEntities;
    }

    if (entityCount == entityLimit) {
      BasicHttpEntity httpEntity = prepareHttpEntityWithContent(entityCount, entityContent);
      basicHttpEntities.add(httpEntity);
      for (int i = 0; i < iterationCount; i++) {
        BasicHttpEntity emptyHttpEntity = prepareEmptyHttpEntity();
        basicHttpEntities.add(emptyHttpEntity);
      }
      return basicHttpEntities;
    }

    for (int i = 0, count = entityCount; i < iterationCount + 1; i++, count -= entityLimit) {

      if (count > 0 && count < entityLimit) {
        BasicHttpEntity httpEntityCallable = prepareHttpEntityWithContent(count, entityContent);
        basicHttpEntities.add(httpEntityCallable);
      }

      if (count > 0 && count >= entityLimit) {
        BasicHttpEntity httpEntityCallable = prepareHttpEntityWithContent(entityLimit,
            entityContent);
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
   * Generates BasicHttpEntity with json array of entityContent
   *
   * @param entityCount size of array
   * @param entityContent element of array
   * @return entity object
   */
  static public BasicHttpEntity prepareHttpEntityWithContent(int entityCount,
      String entityContent) {
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

  static public BasicHttpEntity prepareEmptyHttpEntity() {
    return prepareHttpEntity("[]");
  }

  /**
   * Generates BasicHttpEntity
   *
   * @param body String body of entity
   * @return entity object
   */
  static public BasicHttpEntity prepareHttpEntity(String body) {
    BasicHttpEntity emptyHttpEntity = new BasicHttpEntity();
    emptyHttpEntity.setContent(
        new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    return emptyHttpEntity;
  }

  /**
   * Generates CloseableHttpResponse with multiple entities
   *
   * @param statusCode Http response code
   * @param httpEntities list of entities
   * @return response object
   */
  static public CloseableHttpResponse prepareHttpResponse(int statusCode,
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

  /**
   * Generates CloseableHttpResponse with single entity
   *
   * @param statusCode Http response code
   * @param httpEntity entity of response
   * @return response object
   */
  static public CloseableHttpResponse prepareHttpResponse(int statusCode,
      BasicHttpEntity httpEntity) {
    List<BasicHttpEntity> httpEntities = new ArrayList<>();
    httpEntities.add(httpEntity);
    return prepareHttpResponse(statusCode, httpEntities);
  }

  /**
   * Generates CloseableHttpResponse
   *
   * @param statusCode Http response code
   * @param responseBody String body of http response
   * @return response object
   */
  static public CloseableHttpResponse prepareSimpleHttpResponse(int statusCode,
      String responseBody) {
    return prepareHttpResponse(statusCode, prepareHttpEntity(responseBody));
  }


}
