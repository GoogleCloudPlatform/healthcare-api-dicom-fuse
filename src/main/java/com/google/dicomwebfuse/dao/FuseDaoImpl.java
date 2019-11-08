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

import static com.google.dicomwebfuse.dao.Constants.APPLICATION_DICOM_JSON_CHARSET_UTF8;
import static com.google.dicomwebfuse.dao.Constants.APPLICATION_DICOM_TRANSFER_SYNTAX;
import static com.google.dicomwebfuse.dao.Constants.APPLICATION_JSON_CHARSET_UTF8;
import static com.google.dicomwebfuse.dao.Constants.BEARER;
import static com.google.dicomwebfuse.dao.Constants.HEALTHCARE_HOST;
import static com.google.dicomwebfuse.dao.Constants.MULTIPART_RELATED_TYPE_APPLICATION_DICOM_BOUNDARY;
import static com.google.dicomwebfuse.dao.Constants.PARAM_DICOM_STORE_ID;
import static com.google.dicomwebfuse.dao.Constants.PARAM_INCLUDE_FIELD;
import static com.google.dicomwebfuse.dao.Constants.PARAM_INSTANCE_ID;
import static com.google.dicomwebfuse.dao.Constants.PARAM_LIMIT;
import static com.google.dicomwebfuse.dao.Constants.PARAM_OFFSET;
import static com.google.dicomwebfuse.dao.Constants.PARAM_PAGE_TOKEN;
import static com.google.dicomwebfuse.dao.Constants.PARAM_SERIES_ID;
import static com.google.dicomwebfuse.dao.Constants.PARAM_STUDY_ID;
import static com.google.dicomwebfuse.dao.Constants.SCHEME;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_MAX_LIMIT_FOR_SERIES;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_MAX_LIMIT_FOR_STUDY;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_SERIES_INSTANCE_UID;
import static com.google.dicomwebfuse.dao.Constants.VALUE_PARAM_STUDY_INSTANCE_UID;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpStatusCodes;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.dicomwebfuse.auth.AuthAdc;
import com.google.dicomwebfuse.dao.http.HttpClientFactory;
import com.google.dicomwebfuse.dao.spec.SingleDicomStorePathBuilder;
import com.google.dicomwebfuse.dao.spec.DicomStoresPathBuilder;
import com.google.dicomwebfuse.dao.spec.InstancePathBuilder;
import com.google.dicomwebfuse.dao.spec.InstancesPathBuilder;
import com.google.dicomwebfuse.dao.spec.QueryBuilder;
import com.google.dicomwebfuse.dao.spec.SeriesPathBuilder;
import com.google.dicomwebfuse.dao.spec.StudiesPathBuilder;
import com.google.dicomwebfuse.entities.DicomPath;
import com.google.dicomwebfuse.entities.DicomStore;
import com.google.dicomwebfuse.entities.DicomStores;
import com.google.dicomwebfuse.entities.Instance;
import com.google.dicomwebfuse.entities.Series;
import com.google.dicomwebfuse.entities.Study;
import com.google.dicomwebfuse.exception.DicomFuseException;
import com.google.dicomwebfuse.exception.StowErrorFormatter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;


public class FuseDaoImpl implements FuseDao {

  private AuthAdc authAdc;
  private ObjectMapper objectMapper;
  private HttpClientFactory httpClientFactory;

  public FuseDaoImpl(AuthAdc authAdc, HttpClientFactory httpClientFactory) {
    this.authAdc = authAdc;
    this.httpClientFactory = httpClientFactory;
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  public List<DicomStore> getAllDicomStores(QueryBuilder queryBuilder)
      throws DicomFuseException {
    DicomStoresPathBuilder dicomStoresPathBuilder = new DicomStoresPathBuilder(queryBuilder);
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .setPath(dicomStoresPathBuilder.toPath());
    DicomStores dicomStoresOnPage = createRequestForObjectList(uriBuilder,
        new TypeReference<DicomStores>() {});
    List<DicomStore> dicomStoresList = new ArrayList<>();
    if (dicomStoresOnPage.getDicomStores() != null) {
      dicomStoresList.addAll(dicomStoresOnPage.getDicomStores());
    }
    String nextPageToken = dicomStoresOnPage.getNextPageToken();
    while (nextPageToken != null) {
      uriBuilder.setParameter(PARAM_PAGE_TOKEN, nextPageToken);
      dicomStoresOnPage = createRequestForObjectList(uriBuilder,
          new TypeReference<DicomStores>() {});
      dicomStoresList.addAll(dicomStoresOnPage.getDicomStores());
      nextPageToken = dicomStoresOnPage.getNextPageToken();
    }
    return dicomStoresList;
  }

  @Override
  public DicomStore getSingleDicomStore(QueryBuilder queryBuilder) throws DicomFuseException {
    SingleDicomStorePathBuilder singleDicomStorePathBuilder =
        new SingleDicomStorePathBuilder(queryBuilder);
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .setPath(singleDicomStorePathBuilder.toPath());
    return createRequestForObjectList(uriBuilder, new TypeReference<DicomStore>() {});
  }

  @Override
  public List<Study> getStudies(QueryBuilder queryBuilder) throws DicomFuseException {
    StudiesPathBuilder studiesPathBuilder = new StudiesPathBuilder(queryBuilder);
    String path = studiesPathBuilder.toPath();
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .addParameter(PARAM_LIMIT, VALUE_PARAM_MAX_LIMIT_FOR_STUDY.toString())
        .addParameter(PARAM_OFFSET, queryBuilder.getOffset().toString())
        .setPath(path);
    return createRequestForObjectList(uriBuilder, new TypeReference<List<Study>>() {});
  }

  @Override
  public Study getSingleStudy(QueryBuilder queryBuilder) throws DicomFuseException {
    StudiesPathBuilder studiesPathBuilder = new StudiesPathBuilder(queryBuilder);
    String path = studiesPathBuilder.toPath();
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .addParameter(PARAM_STUDY_ID, queryBuilder.getStudyId())
        .setPath(path);
    List<Study> studies =
        createRequestForObjectList(uriBuilder, new TypeReference<List<Study>>() {});
    if (studies.size() == 0) {
      throw new DicomFuseException("Study not found");
    }
    return studies.get(0);
  }

  @Override
  public List<Series> getSeries(QueryBuilder queryBuilder) throws DicomFuseException {
    SeriesPathBuilder seriesPathBuilder = new SeriesPathBuilder(queryBuilder);
    String path = seriesPathBuilder.toPath();
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .addParameter(PARAM_INCLUDE_FIELD, VALUE_PARAM_STUDY_INSTANCE_UID)
        .addParameter(PARAM_LIMIT, VALUE_PARAM_MAX_LIMIT_FOR_SERIES.toString())
        .addParameter(PARAM_OFFSET, queryBuilder.getOffset().toString())
        .setPath(path);
    return createRequestForObjectList(uriBuilder, new TypeReference<List<Series>>() {});
  }


  @Override
  public Series getSingleSeries(QueryBuilder queryBuilder) throws DicomFuseException {
    SeriesPathBuilder studiesPathBuilder = new SeriesPathBuilder(queryBuilder);
    String path = studiesPathBuilder.toPath();
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .addParameter(PARAM_INCLUDE_FIELD, VALUE_PARAM_STUDY_INSTANCE_UID)
        .addParameter(PARAM_SERIES_ID, queryBuilder.getSeriesId())
        .setPath(path);
    List<Series> series =
        createRequestForObjectList(uriBuilder, new TypeReference<List<Series>>() {});
    if (series.size() == 0) {
      throw new DicomFuseException("Series not found");
    }
    return series.get(0);
  }

  @Override
  public List<Instance> getInstances(QueryBuilder queryBuilder) throws DicomFuseException {
    InstancesPathBuilder instancesPathBuilder = new InstancesPathBuilder(queryBuilder);
    String path = instancesPathBuilder.toPath();
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .addParameter(PARAM_INCLUDE_FIELD, VALUE_PARAM_STUDY_INSTANCE_UID)
        .addParameter(PARAM_INCLUDE_FIELD, VALUE_PARAM_SERIES_INSTANCE_UID)
        .addParameter(PARAM_LIMIT, VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES.toString())
        .addParameter(PARAM_OFFSET, queryBuilder.getOffset().toString())
        .setPath(path);
    return createRequestForObjectList(uriBuilder, new TypeReference<List<Instance>>() {});
  }

  @Override
  public Instance getSingleInstance(QueryBuilder queryBuilder) throws DicomFuseException {
    InstancesPathBuilder instancesPathBuilder = new InstancesPathBuilder(queryBuilder);
    String path = instancesPathBuilder.toPath();
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .addParameter(PARAM_INCLUDE_FIELD, VALUE_PARAM_STUDY_INSTANCE_UID)
        .addParameter(PARAM_INCLUDE_FIELD, VALUE_PARAM_SERIES_INSTANCE_UID)
        .addParameter(PARAM_INSTANCE_ID, queryBuilder.getInstanceId())
        .setPath(path);
    List<Instance> instances =
        createRequestForObjectList(uriBuilder, new TypeReference<List<Instance>>() {});
    if (instances.size() == 0) {
      throw new DicomFuseException("Instance not found");
    }
    return instances.get(0);
  }

  @Override
  public void downloadInstance(QueryBuilder queryBuilder) throws DicomFuseException {
    InstancePathBuilder instancePathBuilder = new InstancePathBuilder(queryBuilder);
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .setPath(instancePathBuilder.toPath());
    createRequestToDownloadInstance(uriBuilder, queryBuilder.getInstanceDataPath());
  }

  @Override
  public void uploadInstance(QueryBuilder queryBuilder) throws DicomFuseException {
    StudiesPathBuilder studiesPathBuilder = new StudiesPathBuilder(queryBuilder);
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .setPath(studiesPathBuilder.toPath());
    createRequestToUploadInstance(uriBuilder, queryBuilder.getInstanceDataPath(),
        queryBuilder.getDicomPath());
  }

  @Override
  public void deleteInstance(QueryBuilder queryBuilder) throws DicomFuseException {
    InstancePathBuilder instancePathBuilder = new InstancePathBuilder(queryBuilder);
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .setPath(instancePathBuilder.toPath());
    createRequestToDeleteInstance(uriBuilder);
  }

  @Override
  public void createDicomStore(QueryBuilder queryBuilder) throws DicomFuseException {
    DicomStoresPathBuilder dicomStoresPathBuilder = new DicomStoresPathBuilder(queryBuilder);
    try (CloseableHttpClient httpclient =  httpClientFactory.createHttpClient()) {
      URI uri = new URIBuilder()
          .setScheme(SCHEME)
          .setHost(HEALTHCARE_HOST)
          .setPath(dicomStoresPathBuilder.toPath())
          .setParameter(PARAM_DICOM_STORE_ID, queryBuilder.getDicomStoreId())
          .build();
      HttpPost request = new HttpPost(uri);
      request.addHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF8);
      GoogleCredentials credentials = authAdc.getCredentials();
      String tokenValue = credentials.getAccessToken().getTokenValue();
      request.addHeader(AUTHORIZATION, BEARER + tokenValue);
      try (CloseableHttpResponse response = httpclient.execute(request)) {
        checkStatusCode(response, uri);
      }
    } catch (IOException | URISyntaxException e) {
      throw new DicomFuseException(e);
    }
  }

  private <T> T createRequestForObjectList(URIBuilder uriBuilder, TypeReference<T> typeReference)
      throws DicomFuseException {
    T result;
    try (CloseableHttpClient httpclient = httpClientFactory.createHttpClient()) {
      URI uri = uriBuilder.build();
      HttpGet request = new HttpGet(uri);
      request.addHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF8);
      GoogleCredentials credentials = authAdc.getCredentials();
      String tokenValue = credentials.getAccessToken().getTokenValue();
      request.addHeader(AUTHORIZATION, BEARER + tokenValue);
      try (CloseableHttpResponse response = httpclient.execute(request)) {
        checkStatusCode(response, uri);
        try (InputStream inputStream = response.getEntity().getContent()) {
          result = objectMapper.readValue(inputStream, typeReference);
        }
      }
    } catch (IOException | URISyntaxException e) {
      throw new DicomFuseException(e);
    }
    return result;
  }

  private void createRequestToDownloadInstance(URIBuilder uriBuilder, Path instanceDataPath)
      throws DicomFuseException {
    try (CloseableHttpClient httpclient = httpClientFactory.createHttpClient()) {
      URI uri = uriBuilder.build();
      HttpGet request = new HttpGet(uri);
      request.addHeader(ACCEPT, APPLICATION_DICOM_TRANSFER_SYNTAX);
      request.addHeader(CONTENT_TYPE, APPLICATION_DICOM_JSON_CHARSET_UTF8);
      GoogleCredentials credentials = authAdc.getCredentials();
      String tokenValue = credentials.getAccessToken().getTokenValue();
      request.addHeader(AUTHORIZATION, BEARER + tokenValue);
      try (CloseableHttpResponse response = httpclient.execute(request)) {
        checkStatusCode(response, uri);
        HttpEntity entity = response.getEntity();
        try (InputStream is = entity.getContent()) {
          Files.copy(is, instanceDataPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
          throw new DicomFuseException(e);
        }
      }
    } catch (IOException | URISyntaxException e) {
      throw new DicomFuseException(e);
    }
  }

  private void createRequestToUploadInstance(URIBuilder uriBuilder, Path instanceDataPath,
      DicomPath dicomPath) throws DicomFuseException {
    try (CloseableHttpClient httpclient = httpClientFactory.createHttpClient()) {
      URI uri = uriBuilder.build();
      HttpPost request = new HttpPost(uri);

      ContentType contentType = ContentType.create("application/dicom");
      String boundary = UUID.randomUUID().toString();

      HttpEntity httpEntity = MultipartEntityBuilder.create()
          .setBoundary(boundary)
          .addBinaryBody("DICOMFile", instanceDataPath.toFile(), contentType, "")
          .build();
      request.setEntity(httpEntity);

      request.addHeader(CONTENT_TYPE, MULTIPART_RELATED_TYPE_APPLICATION_DICOM_BOUNDARY + boundary);
      GoogleCredentials credentials = authAdc.getCredentials();
      String tokenValue = credentials.getAccessToken().getTokenValue();
      request.addHeader(AUTHORIZATION, BEARER + tokenValue);
      try (CloseableHttpResponse response = httpclient.execute(request)) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatusCodes.STATUS_CODE_OK) {
          HttpEntity entity = response.getEntity();
          String responseBody = EntityUtils.toString(entity);
          String mimeType = ContentType.get(entity).getMimeType();
          throw new DicomFuseException(
              "Failed to upload - " + dicomPath + "\n" + response.getStatusLine() +
                  "\n" + StowErrorFormatter.formatByMimeType(responseBody, mimeType), statusCode);
        }
      }
    } catch (IOException | URISyntaxException e) {
      throw new DicomFuseException(e);
    }
  }

  private void createRequestToDeleteInstance(URIBuilder uriBuilder) throws DicomFuseException {
    try (CloseableHttpClient httpclient = httpClientFactory.createHttpClient()) {
      URI uri = uriBuilder.build();
      HttpDelete request = new HttpDelete(uri);
      request.addHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF8);
      GoogleCredentials credentials = authAdc.getCredentials();
      String tokenValue = credentials.getAccessToken().getTokenValue();
      request.addHeader(AUTHORIZATION, BEARER + tokenValue);
      try (CloseableHttpResponse response = httpclient.execute(request)) {
        checkStatusCode(response, uri);
      }
    } catch (IOException | URISyntaxException e) {
      throw new DicomFuseException(e);
    }
  }

  private void checkStatusCode(CloseableHttpResponse response, URI uri) throws DicomFuseException {
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != HttpStatusCodes.STATUS_CODE_OK) {
      throw new DicomFuseException("Failed HTTP " + response.getStatusLine() + " " + uri,
          statusCode);
    }
  }
}
