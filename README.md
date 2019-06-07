# DICOMFuse

DICOMFuse is a file system that integrates with [Google Cloud Healthcare API](https://cloud.google.com/healthcare/).
DICOMFuse allows you to anonymize(using anonymize tool), read, upload and delete images in 
[DICOM Stores](https://cloud.google.com/healthcare/docs/how-tos/dicom).  
DICOMFuse is cross platform application which was written in JAVA. You can use it in Linux, Windows 
or macOS.

## Setting up and run DICOMFuse

### Applications

For using DICOMFuse you need to have or install:
* Java SE Runtime Environment 8.
* [WinFps](https://github.com/billziss-gh/winfsp) for using FUSE in Windows.
* [OSXFuse](https://osxfuse.github.io/) for using FUSE in macOS (macOS 10.14 Mojave).

>[libfuse](https://github.com/libfuse/libfuse) has been supported in Linux kernel since 2.6.14 version.

### Credentials

In order to be able to access to Google Cloud Healthcare API using [Google application default credentials](https://cloud.google.com/docs/authentication/production#howtheywork).
You can set GOOGLE_APPLICATION_CREDENTIALS environment variable, or you can use --keyFile option 
when you launch DICOMFuse.

### Mount options

1) Download the latest JAR from the releases tab.  
2) To start DICOMFuse open a terminal in the DICOMFuse folder and input `java -jar dicom-fuse-0.1.jar [options]`
3) To stop DICOMFuse press CTRL+C buttons.

```
* --datasetAddr, -a
    Web address to the Dataset. 
    Example: https://healthcare.googleapis.com/STAGE/projects/PROJECT/locations/LOCATION/datasets/DATASET
* --mountPath, -p
    Path to mount the Dataset.
    Example on Linux: /home/user/fuse-mount-folder
    Example on macOS: /Users/user/fuse-mount-folder
    Example on Windows: J:\
  --cacheSize, -s
    Maximum cache size in megabytes for cached instances. The maximum file size that can be 
    download/upload is cacheSize / 4
    Default: 10000
  --cacheTime, -t
    Each value in this option is measured in seconds.
    For optimization of the DICOMFuse, caching DICOM Store folders, Study folders, Series folders, 
    list of Instances(first parameter in option), and opened Instances files(second parameter) were 
    included. Caching opened Instances files are performed to the temporary folder in user space on 
    the disk. Caching other objects are performed to the RAM. After closing DICOMFuse or, if cached 
    files out of date, they will be deleted. If you delete an Instance file locally, the cache will 
    be updated. If you upload an Instance file, the cache will be updated.
    Default: 60,300
  --help, -h
    Print help
  --keyFile, -k
    Path to the account service key
  --enableDeletion, -d
    Some programs can delete files and you can do this, including accidental deletion. If you 
    don't want to delete files, you can set --enableDeletion=false
    Default: true
```
> Note: * are required options.  
> The current implementation supports 15 000 results in folders.     
> In Windows recommended using `java -Dfile.encoding=UTF8 -jar dicom-fuse-0.1.jar [options]`

## Description 

### Data [model](https://cloud.google.com/healthcare/docs/concepts/projects-datasets-data-stores)

* Dataset: mounted folder.
* DICOM Store: subfolder of Dataset.
* DICOM Study: subfolder of Store.
* DICOM Series: subfolder of Study.
* DICOM Instance: a file within Series.

### Operations supported

Whereas Dicom store is specifical that is why not all operations are supported.  
You can use graphical interface on OS or commands in the terminal when you navigate through folders.

#### Operations supported in graphical interface:

* Upload a file in the DICOM Store folder(drag and drop).
* Upload a file in the Series folder(drag and drop).
* Delete a *.dcm file in the Series folder.
* Overwrite existing file.
* Replace operation(drag and drop) is not supported.

#### Operations supported in the terminal:

* cd - change a directory.
* ls - list of objects existing in a folder.
* cp - copy **only instances files** (not folders) to another DICOM Store or to a local computer.
* rm - delete instances files.
* mkdir - is not supported. If you try to create new folder, you will get the error:   
_mkdir: cannot create directory ‘new-folder’: Function not implemented._
* mv - is not supported.

Any reading use cases can be used e.g. de-identification, manual editing of the (binary) dicom object.

### Caching objects

For optimization of the DICOMFuse, caching DICOM Store folders, Study folders, Series folders, list 
of Instances, and opened Instances files were included. Caching opened Instances files are performed 
to the temporary folder in user space on the disk. Caching other objects are performed to the RAM. 
After closing DICOMFuse or, if cached files out of date, they will be deleted.
* If you delete an Instance, the current Study folder, Series folder and list of Instances will be 
updated in the local cache. 
* If you upload an Instance, all Study folders, Series folders and all Instances will be updated 
in current DICOM Store in the local cache. 
* Before opening an Instance, the size of it is 0 byte. If you open the Instance, it will be 
downloaded from the server and you will see the Instance size. If you read the Instance again, it 
will be reading from the local cache, not from the server. After repeated request if the cacheTime 
of the Instance is out of date, the Instance will be downloaded from the server again.
* If you create a request (e.g. cd command in a Dataset folder) and the object does not exist in the
local cache, the request will be sent to the server and list of objects will be updated in the local 
cache.

## Packaging

1) **git clone https://gitlab.com/arvgord/dicom-fuse.git**
2) **mvn package** in the dicom-fuse folder in the terminal (for packaging to jar).

There is **dicom-fuse-0.1.jar** in **dicom-fuse/target** folder.

## License

This application is licensed under Apache License, Version 2.0. Full license text is available in LICENSE.



