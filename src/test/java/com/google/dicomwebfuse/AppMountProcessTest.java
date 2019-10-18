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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.google.dicomwebfuse.dao.FuseDao;
import com.google.dicomwebfuse.exception.DicomFuseException;
import com.google.dicomwebfuse.fuse.DicomFuse;
import com.google.dicomwebfuse.fuse.Parameters;
import com.google.dicomwebfuse.parser.Arguments;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import jnr.ffi.Platform.OS;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class AppMountProcessTest {

  @Test
  void testShouldReturnExceptionIfUserDoesNotHaveAccessToAnDataset() throws DicomFuseException {
    // Given
    Arguments arguments = new Arguments();
    // Setting OS
    OS os = OS.LINUX;
    FuseDao fuseDao = Mockito.mock(FuseDao.class);
    Mockito.when(fuseDao.getAllDicomStores(any())).thenThrow(DicomFuseException.class);
    Parameters parameters = new Parameters(fuseDao, arguments, os);
    DicomFuse dicomFuse = Mockito.mock(DicomFuse.class);
    // When
    AppMountProcess appMountProcess = new AppMountProcess(arguments, os, parameters, dicomFuse);
    // Then
    assertThrows(DicomFuseException.class, appMountProcess::startMountProcess);
  }

  @Test
  void testShouldMountDicomFuseOnLinuxAndSetExtraMountOptions()
      throws IOException, DicomFuseException {
    // Given
    Arguments arguments = new Arguments();
    // Setting extra mount options
    arguments.extraMountOptions.add("debug");
    arguments.extraMountOptions.add("allow_other");
    // Setting OS
    OS os = OS.LINUX;
    FuseDao fuseDao = Mockito.mock(FuseDao.class);
    Parameters parameters = new Parameters(fuseDao, arguments, os);
    DicomFuse dicomFuse = Mockito.mock(DicomFuse.class);
    ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
    Mockito.doNothing()
        .when(dicomFuse)
        .mount(any(), any(Boolean.class), any(Boolean.class), captor.capture());
    // When
    AppMountProcess appMountProcess = new AppMountProcess(arguments, os, parameters, dicomFuse);
    appMountProcess.startMountProcess();
    // Then
    List<String> options = Arrays.asList(captor.getValue());
    assertThat(options, hasItems("-odebug", "-oallow_other", "-ofsname=DICOMFuse",
        "-onegative_timeout=4", "-oattr_timeout=0", "-oac_attr_timeout=0", "-oentry_timeout=0"));
  }

  @Test
  void testShouldMountDicomFuseOnLinuxAndSetMountOptions()
      throws IOException, DicomFuseException {
    // Given
    Arguments arguments = new Arguments();
    // Setting OS
    OS os = OS.LINUX;
    FuseDao fuseDao = Mockito.mock(FuseDao.class);
    Parameters parameters = new Parameters(fuseDao, arguments, os);
    DicomFuse dicomFuse = Mockito.mock(DicomFuse.class);
    ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
    Mockito.doNothing()
        .when(dicomFuse)
        .mount(any(), any(Boolean.class), any(Boolean.class), captor.capture());
    // When
    AppMountProcess appMountProcess = new AppMountProcess(arguments, os, parameters, dicomFuse);
    appMountProcess.startMountProcess();
    // Then
    List<String> options = Arrays.asList(captor.getValue());
    assertThat(options, hasItems("-ofsname=DICOMFuse", "-onegative_timeout=4", "-oattr_timeout=0",
        "-oac_attr_timeout=0", "-oentry_timeout=0"));
  }

  @Test
  void testShouldMountDicomFuseOnWindowsAndSetExtraMountOptions()
      throws IOException, DicomFuseException {
    // Given
    Arguments arguments = new Arguments();
    // Setting extra mount options
    arguments.extraMountOptions.add("FileSystemName=FUSE");
    arguments.extraMountOptions.add("debug");
    // Setting OS
    OS os = OS.WINDOWS;
    FuseDao fuseDao = Mockito.mock(FuseDao.class);
    Parameters parameters = new Parameters(fuseDao, arguments, os);
    DicomFuse dicomFuse = Mockito.mock(DicomFuse.class);
    ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
    Mockito.doNothing()
        .when(dicomFuse)
        .mount(any(), any(Boolean.class), any(Boolean.class), captor.capture());
    // When
    AppMountProcess appMountProcess = new AppMountProcess(arguments, os, parameters, dicomFuse);
    appMountProcess.startMountProcess();
    // Then
    List<String> options = Arrays.asList(captor.getValue());
    assertThat(options, hasItems("-oFileSystemName=FUSE", "-odebug", "-ofsname=DICOMFuse",
        "-onegative_timeout=4", "-oattr_timeout=0", "-oac_attr_timeout=0", "-oentry_timeout=0",
        "-oThreadCount=16"));
  }

  @Test
  void testShouldMountDicomFuseOnWindowsAndSetMountOptions()
      throws IOException, DicomFuseException {
    // Given
    Arguments arguments = new Arguments();
    // Setting OS
    OS os = OS.WINDOWS;
    FuseDao fuseDao = Mockito.mock(FuseDao.class);
    Parameters parameters = new Parameters(fuseDao, arguments, os);
    DicomFuse dicomFuse = Mockito.mock(DicomFuse.class);
    ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
    Mockito.doNothing()
        .when(dicomFuse)
        .mount(any(), any(Boolean.class), any(Boolean.class), captor.capture());
    // When
    AppMountProcess appMountProcess = new AppMountProcess(arguments, os, parameters, dicomFuse);
    appMountProcess.startMountProcess();
    // Then
    List<String> options = Arrays.asList(captor.getValue());
    assertThat(options, hasItems("-ofsname=DICOMFuse", "-onegative_timeout=4", "-oattr_timeout=0",
        "-oac_attr_timeout=0", "-oentry_timeout=0", "-oThreadCount=16"));
  }

  @Test
  void testShouldMountDicomFuseOnMacOsAndSetExtraMountOptions()
      throws IOException, DicomFuseException {
    // Given
    Arguments arguments = new Arguments();
    // Setting extra mount options
    arguments.extraMountOptions.add("debug");
    arguments.extraMountOptions.add("allow_other");
    // Setting OS
    OS os = OS.DARWIN;
    FuseDao fuseDao = Mockito.mock(FuseDao.class);
    Parameters parameters = new Parameters(fuseDao, arguments, os);
    DicomFuse dicomFuse = Mockito.mock(DicomFuse.class);
    ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
    Mockito.doNothing()
        .when(dicomFuse)
        .mount(any(), any(Boolean.class), any(Boolean.class), captor.capture());
    // When
    AppMountProcess appMountProcess = new AppMountProcess(arguments, os, parameters, dicomFuse);
    appMountProcess.startMountProcess();
    // Then
    List<String> options = Arrays.asList(captor.getValue());
    assertThat(options, hasItems("-odebug", "-oallow_other", "-onegative_timeout=4",
        "-onolocalcaches", "-onoappledouble", "-odefer_permissions", "-ovolname=DICOMFuse"));
  }

  @Test
  void testShouldMountDicomFuseOnMacOsAndSetMountOptions()
      throws IOException, DicomFuseException {
    // Given
    Arguments arguments = new Arguments();
    // Setting OS
    OS os = OS.DARWIN;
    FuseDao fuseDao = Mockito.mock(FuseDao.class);
    Parameters parameters = new Parameters(fuseDao, arguments, os);
    DicomFuse dicomFuse = Mockito.mock(DicomFuse.class);
    ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
    Mockito.doNothing()
        .when(dicomFuse)
        .mount(any(), any(Boolean.class), any(Boolean.class), captor.capture());
    // When
    AppMountProcess appMountProcess = new AppMountProcess(arguments, os, parameters, dicomFuse);
    appMountProcess.startMountProcess();
    // Then
    List<String> options = Arrays.asList(captor.getValue());
    assertThat(options, hasItems("-onegative_timeout=4", "-onolocalcaches", "-onoappledouble",
        "-odefer_permissions", "-ovolname=DICOMFuse"));
  }
}