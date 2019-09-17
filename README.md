# DICOMFuse

DICOMFuse is a file system that integrates with the
[Cloud Healthcare API](https://cloud.google.com/healthcare/). DICOMFuse lets you
anonymize (using an anonymization tool), read, upload, and delete images in
[DICOM stores](https://cloud.google.com/healthcare/docs/how-tos/dicom). \
DICOMFuse is cross-platform application written in Java. You can use it in
Linux, Windows or macOS.

## Prerequisites

### Applications

Before running DICOMFuse, ensure that you've installed the following
applications:

*   Java SE Runtime Environment 8.
*   [WinFsp](https://github.com/billziss-gh/winfsp) for using FUSE in Windows 
    (Windows 7x64, Windows 10x64).
*   [OSXFuse](https://osxfuse.github.io/) for using FUSE in macOS (macOS 10.14
    Mojave).
*   [libfuse](https://github.com/libfuse/libfuse) for using FUSE in Linux, install 
    libfuse - `sudo apt install libfuse2`

> FUSE has been supported in Linux kernel since 2.6.14 version.

### Credentials

DICOMFuse will use 
[application default credentials](https://cloud.google.com/docs/authentication/production)
to authenticate access to the Cloud Healthcare API. The identity must have the 
permissions within the 
[Healthcare DICOM Editor role](https://cloud.google.com/healthcare/docs/concepts/access-control#roles).  
Alternatively, you can specify a service account key via the `--keyFile` flag.
The easiest way to set up credentials is to use your user credentials with the 
[gcloud tool](https://cloud.google.com/sdk/gcloud/):

`gcloud auth application-default login`

## Running DICOMFuse

To run DICOMFuse:

1.  Download the latest JAR from the releases tab.
2.  To start DICOMFuse, open a terminal in the DICOMFuse folder and input `java
    -jar healthcare-api-dicom-fuse-X.Y.Z.jar [options]`
3.  To stop DICOMFuse, press CTRL+C.

> ERROR and INFO logs present in the terminal. During launching DICOMFuse, logs 
> folder will be created next to the DICOMFuse JAR, where DEBUG, INFO and ERROR logs
> will be saved.

### Mount options

You can specify the following mount options when you start DICOMFuse:

```
* --datasetAddr, -a
    Web address to the Dataset.
    Example: https://healthcare.googleapis.com/v1beta1/projects/PROJECT/locations/LOCATION/datasets/DATASET
* --mountPath, -p
    Path to mount the Dataset.
    Example on Linux: /home/user/fuse-mount-folder
    Example on macOS: /Users/user/fuse-mount-folder
    Example on Windows: J:\
  --cacheSize, -s
    Maximum cache size in megabytes for cached instances. The maximum file size that can be
    downloaded/uploaded is cacheSize / 4
    Default: 10000
  --cacheTime, -t
    Each value in this option is measured in seconds.
    To optimize DICOMFuse, the following resources are cached: DICOM store folders, Study folders, Series folders,
    list of Instances (first parameter in option), and opened Instances files (second parameter).
    Opened Instance files are cached to the temporary folder in the user space on disk.
    Other objects are cached to RAM. Cached files will be deleted if you close DICOMFuse or if the cached
    files become out of date. If you delete an Instance file locally, the cache will
    be updated. If you upload an Instance file, the cache will be updated.
    Default: 60,300
  --help, -h
    Print help
  --keyFile, -k
    Path to the account service key
  --enableDeletion, -d
    Some programs can delete files and deletion can also be done manually. Sometimes accidental deletions can occur.
    If you don't want to delete files, you can set `--enableDeletion=false`
    Default: true
```

> Note: * are required options. \
> The current implementation supports 15,000 results in folders. \
> For Windows, use `java -Dfile.encoding=UTF8 -jar healthcare-api-dicom-fuse-X.Y.Z.jar [options]`

## Description

### Data [model](https://cloud.google.com/healthcare/docs/concepts/projects-datasets-data-stores)

*   Dataset: mounted folder.
*   DICOM Store: subfolder of dataset.
*   DICOM Study: subfolder of store.
*   DICOM Series: subfolder of Study.
*   DICOM Instance: a file within Series.

### Operations supported

You can use a graphical interface on an OS or commands in the terminal to
navigate through folders.

#### Operations supported in graphical interface:

*   Upload a file in the DICOM Store folder (drag and drop).
*   Upload a file in the Series folder (drag and drop).
*   Delete a *.dcm file in the Series folder.
*   Overwrite existing file.
*   Replace operation (drag and drop) is not supported.

#### Operations supported in the terminal:

*   cd - change a directory.
*   ls - list the objects existing in a folder.
*   cp - copy **only instances files** (not folders) to another DICOM store or
    to a local computer.
*   rm - delete instances files.
*   mkdir - is not supported. If you try to create a new folder, you will get
    the error: \
    _mkdir: cannot create directory ‘new-folder’: Function not implemented._
*   mv - is not supported.

Any reading use cases can be used, such as de-identification or manual editing
of the (binary) DICOM object.

### Caching objects

To optimize DICOMFuse, the following resources are cached: DICOM store folders,
Study folders, Series folders, list of Instances (first parameter in option),
and opened Instances files (second parameter). Opened Instance files are cached
to the temporary folder in the user space on disk. Other objects are cached to
RAM. Cached files will be deleted if you close DICOMFuse or if the cached files
become out of date.

*   If you delete an Instance, the current Study folder, Series folder, and list
    of Instances will be updated in the local cache.
*   If you upload an Instance, all Study folders, Series folders, and all
    Instances will be updated in current DICOM store in the local cache.
*   Before opening an Instance, the size of it is 0 bytes. If you open the
    Instance, it will be downloaded from the server and you will see the
    Instance size. If you read the Instance again, it will be reading from the
    local cache, not from the server. After repeated requests, if the cacheTime
    of the Instance is out of date, the Instance will be downloaded from the
    server again.
*   If you create a request (e.g. cd command in a Dataset folder) and the object
    does not exist in the local cache, the request will be sent to the server
    and a list of objects will be updated in the local cache.

## Packaging

1.  **git clone https://github.com/GoogleCloudPlatform/healthcare-api-dicom-fuse.git**
2.  **mvn package** in the healthcare-api-dicom-fuse folder in the terminal (for 
    packaging to jar).

There is a **healthcare-api-dicom-fuse-X.Y.Z.jar** file in the **healthcare-api-dicom-fuse/target** folder.

## License

This application is licensed under Apache License, Version 2.0. Full license
text is available in LICENSE.
