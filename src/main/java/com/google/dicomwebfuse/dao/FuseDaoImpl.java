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
import static com.google.dicomwebfuse.dao.Constants.MAX_INSTANCES_IN_SERIES;
import static com.google.dicomwebfuse.dao.Constants.MAX_SERIES_IN_STUDY;
import static com.google.dicomwebfuse.dao.Constants.MAX_STUDIES_IN_DICOM_STORE;
import static com.google.dicomwebfuse.dao.Constants.MULTIPART_RELATED_TYPE_APPLICATION_DICOM_BOUNDARY;
import static com.google.dicomwebfuse.dao.Constants.PARAM_INCLUDE_FIELD;
import static com.google.dicomwebfuse.dao.Constants.PARAM_LIMIT;
import static com.google.dicomwebfuse.dao.Constants.PARAM_OFFSET;
import static com.google.dicomwebfuse.dao.Constants.PARAM_PAGE_TOKEN;
import static com.google.dicomwebfuse.dao.Constants.SCHEME;
import static com.google.dicomwebfuse.dao.Constants.THREAD_COUNT;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class FuseDaoImpl implements FuseDao {

  private AuthAdc authADC;

  public FuseDaoImpl(AuthAdc authADC) {
    this.authADC = authADC;
  }

  @Override
  public List<DicomStore> getDicomStoresList(QueryBuilder queryBuilder)
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
  public List<Study> getStudiesList(QueryBuilder queryBuilder) throws DicomFuseException {
    StudiesPathBuilder studiesPathBuilder = new StudiesPathBuilder(queryBuilder);
    String path = studiesPathBuilder.toPath();
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .addParameter(PARAM_LIMIT, VALUE_PARAM_MAX_LIMIT_FOR_STUDY.toString())
        .setPath(path);
    List<Study> studyList =
        createRequestForObjectList(uriBuilder, new TypeReference<List<Study>>() {});

    if (studyList.size() == VALUE_PARAM_MAX_LIMIT_FOR_STUDY) {
      ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
      List<Future<List<Study>>> futureList = new ArrayList<>();

      int startOffset = VALUE_PARAM_MAX_LIMIT_FOR_STUDY * 2;
      int maxOffset = MAX_STUDIES_IN_DICOM_STORE + VALUE_PARAM_MAX_LIMIT_FOR_STUDY;
      for (int offset = startOffset; offset <= maxOffset;
          offset += VALUE_PARAM_MAX_LIMIT_FOR_STUDY) {
        StudyListCallable studyCallable = new StudyListCallable(path, offset);
        Future<List<Study>> future = executorService.submit(studyCallable);
        futureList.add(future);
      }
      for (Future<List<Study>> future : futureList) {
        try {
          studyList.addAll(future.get());
        } catch (InterruptedException | ExecutionException e) {
          throw new DicomFuseException(e);
        }
      }
      executorService.shutdown();
    }

    if (studyList.size() > MAX_STUDIES_IN_DICOM_STORE) {
      throw new DicomFuseException(
          "Too large DICOM Store. In " + queryBuilder.getDicomStoreId() + " over "
              + MAX_STUDIES_IN_DICOM_STORE + " Studies!");
    }

    return studyList;
  }

  private class StudyListCallable implements Callable<List<Study>> {

    private final String path;
    private final Integer offset;

    StudyListCallable(String path, Integer offset) {
      this.path = path;
      this.offset = offset;
    }

    @Override
    public List<Study> call() throws Exception {
      URIBuilder uriBuilder = new URIBuilder()
          .setScheme(SCHEME)
          .setHost(HEALTHCARE_HOST)
          .addParameter(PARAM_LIMIT, VALUE_PARAM_MAX_LIMIT_FOR_STUDY.toString())
          .addParameter(PARAM_OFFSET, offset.toString())
          .setPath(path);
      return createRequestForObjectList(uriBuilder, new TypeReference<List<Study>>() {});
    }
  }

  @Override
  public List<Series> getSeriesList(QueryBuilder queryBuilder) throws DicomFuseException {
    SeriesPathBuilder seriesPathBuilder = new SeriesPathBuilder(queryBuilder);
    String path = seriesPathBuilder.toPath();
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .addParameter(PARAM_INCLUDE_FIELD, VALUE_PARAM_STUDY_INSTANCE_UID)
        .addParameter(PARAM_LIMIT, VALUE_PARAM_MAX_LIMIT_FOR_SERIES.toString())
        .setPath(path);
    List<Series> seriesList =
        createRequestForObjectList(uriBuilder, new TypeReference<List<Series>>() {});

    if (seriesList.size() == VALUE_PARAM_MAX_LIMIT_FOR_SERIES) {
      ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
      List<Future<List<Series>>> futureList = new ArrayList<>();

      int startOffset = VALUE_PARAM_MAX_LIMIT_FOR_SERIES * 2;
      int maxOffset = MAX_SERIES_IN_STUDY + VALUE_PARAM_MAX_LIMIT_FOR_SERIES;
      for (int offset = startOffset; offset <= maxOffset;
          offset += VALUE_PARAM_MAX_LIMIT_FOR_SERIES) {
        SeriesListCallable seriesListCallable = new SeriesListCallable(path, offset);
        Future<List<Series>> future = executorService.submit(seriesListCallable);
        futureList.add(future);
      }
      for (Future<List<Series>> future : futureList) {
        try {
          seriesList.addAll(future.get());
        } catch (InterruptedException | ExecutionException e) {
          throw new DicomFuseException(e);
        }
      }
      executorService.shutdown();
    }

    if (seriesList.size() > MAX_SERIES_IN_STUDY) {
      throw new DicomFuseException(
          "Too large Study. In " + queryBuilder.getStudyId() + " over " + MAX_SERIES_IN_STUDY +
              " Series!");
    }

    return seriesList;
  }

  private class SeriesListCallable implements Callable<List<Series>> {

    private final String path;
    private final Integer offset;

    SeriesListCallable(String path, Integer offset) {
      this.path = path;
      this.offset = offset;
    }

    @Override
    public List<Series> call() throws Exception {
      URIBuilder uriBuilder = new URIBuilder()
          .setScheme(SCHEME)
          .setHost(HEALTHCARE_HOST)
          .addParameter(PARAM_INCLUDE_FIELD, VALUE_PARAM_STUDY_INSTANCE_UID)
          .addParameter(PARAM_LIMIT, VALUE_PARAM_MAX_LIMIT_FOR_SERIES.toString())
          .addParameter(PARAM_OFFSET, offset.toString())
          .setPath(path);
      return createRequestForObjectList(uriBuilder, new TypeReference<List<Series>>() {});
    }
  }

  @Override
  public List<Instance> getInstancesList(QueryBuilder queryBuilder) throws DicomFuseException {
    InstancesPathBuilder instancesPathBuilder = new InstancesPathBuilder(queryBuilder);
    String path = instancesPathBuilder.toPath();
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .addParameter(PARAM_INCLUDE_FIELD, VALUE_PARAM_STUDY_INSTANCE_UID)
        .addParameter(PARAM_INCLUDE_FIELD, VALUE_PARAM_SERIES_INSTANCE_UID)
        .addParameter(PARAM_LIMIT, VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES.toString())
        .setPath(path);
    List<Instance> instanceList =
        createRequestForObjectList(uriBuilder, new TypeReference<List<Instance>>() {});
    if (instanceList.size() == VALUE_PARAM_MAX_LIMIT_FOR_INSTANCES) {
      uriBuilder = new URIBuilder()
          .setScheme(SCHEME)
          .setHost(HEALTHCARE_HOST)
          .addParameter(PARAM_LIMIT, "1")
          .addParameter(PARAM_OFFSET, MAX_INSTANCES_IN_SERIES.toString())
          .setPath(path);
      List<Instance> overMaxLimitInstanceList =
          createRequestForObjectList(uriBuilder, new TypeReference<List<Instance>>() {});
      if (overMaxLimitInstanceList.size() > 0) {
        throw new DicomFuseException(
            "Too large Series. In " + queryBuilder.getSeriesId() + " over " +
                MAX_INSTANCES_IN_SERIES + " Instances!");
      }
    }
    return instanceList;
  }

  @Override
  public void downloadInstanceToTempFile(QueryBuilder queryBuilder) throws DicomFuseException {
    InstancePathBuilder instancePathBuilder = new InstancePathBuilder(queryBuilder);
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .setPath(instancePathBuilder.toPath());
    createRequestForDownloadInstance(uriBuilder, queryBuilder.getInstanceDataPath());
  }

  @Override
  public void uploadInstance(QueryBuilder queryBuilder) throws DicomFuseException {
    StudiesPathBuilder studiesPathBuilder = new StudiesPathBuilder(queryBuilder);
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .setPath(studiesPathBuilder.toPath());
    createRequestForUploadInstance(uriBuilder, queryBuilder.getInstanceDataPath(),
        queryBuilder.getDicomPath());
  }

  @Override
  public void deleteInstance(QueryBuilder queryBuilder) throws DicomFuseException {
    InstancePathBuilder instancePathBuilder = new InstancePathBuilder(queryBuilder);
    URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEME)
        .setHost(HEALTHCARE_HOST)
        .setPath(instancePathBuilder.toPath());
    createRequestForDeleteInstance(uriBuilder);
  }

  private <T> T createRequestForObjectList(URIBuilder uriBuilder, TypeReference<T> typeReference)
      throws DicomFuseException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    T result;
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      URI uri = uriBuilder.build();
      HttpGet request = new HttpGet(uri);
      request.addHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF8);
      GoogleCredentials credentials = authADC.getCredentials();
      String tokenValue = credentials.getAccessToken().getTokenValue();
      request.addHeader(AUTHORIZATION, BEARER + tokenValue);
      try (CloseableHttpResponse response = httpclient.execute(request)) {
        checkStatusCode(response, uri);
        try (InputStream inputStream = response.getEntity().getContent()) {
          result = mapper.readValue(inputStream, typeReference);
        }
      }
    } catch (IOException | URISyntaxException e) {
      throw new DicomFuseException(e);
    }
    return result;
  }

  private void createRequestForDownloadInstance(URIBuilder uriBuilder, Path instanceDataPath)
      throws DicomFuseException {
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      URI uri = uriBuilder.build();
      HttpGet request = new HttpGet(uri);
      request.addHeader(ACCEPT, APPLICATION_DICOM_TRANSFER_SYNTAX);
      request.addHeader(CONTENT_TYPE, APPLICATION_DICOM_JSON_CHARSET_UTF8);
      GoogleCredentials credentials = authADC.getCredentials();
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

  private void createRequestForUploadInstance(URIBuilder uriBuilder, Path instanceDataPath,
      DicomPath dicomPath) throws DicomFuseException {
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
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
      GoogleCredentials credentials = authADC.getCredentials();
      String tokenValue = credentials.getAccessToken().getTokenValue();
      request.addHeader(AUTHORIZATION, BEARER + tokenValue);
      try (CloseableHttpResponse response = httpclient.execute(request)) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatusCodes.STATUS_CODE_OK) {
          throw new DicomFuseException(
              "Failed to upload - " + dicomPath + "! Status code: " + statusCode + " " + uri);
        }
      }
    } catch (IOException | URISyntaxException e) {
      throw new DicomFuseException(e);
    }
  }

  private void createRequestForDeleteInstance(URIBuilder uriBuilder) throws DicomFuseException {
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      URI uri = uriBuilder.build();
      HttpDelete request = new HttpDelete(uri);
      request.addHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF8);
      GoogleCredentials credentials = authADC.getCredentials();
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
      throw new DicomFuseException("Failed HTTP! Status code: " + statusCode + " " + uri);
    }
  }
}
