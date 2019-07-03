# Copyright 2019 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#!/bin/bash
STAGE=$1
PROJECT=$2
LOCATION=$3
DATASET=$4
# Creates unique DICOM Store name
DICOM_STORE_NAME=$(openssl rand -hex 12)
# Creates a folder to mount DICOMFuse
MOUNT_FOLDER='dicom'
mkdir $MOUNT_FOLDER
# Creates unique DICOM Store
gcloud alpha healthcare dicom-stores create $DICOM_STORE_NAME \
--location=$LOCATION --dataset=$DATASET --quiet
# Installs libfuse
apt update
apt install -y libfuse2
# Gets JAR version from pom.xml
JAR_VERSION=$(grep -m 1 "<version>" /workspace/pom.xml | grep -Eo "[[:digit:]]+.[[:digit:]]+")
JAR_NAME="healthcare-api-dicom-fuse-"$JAR_VERSION".jar"
# Runs DICOMFuse
ADDR='https://healthcare.googleapis.com/'$STAGE'/projects/'$PROJECT'/locations/'$LOCATION'/datasets/'$DATASET
java -jar /workspace/target/$JAR_NAME -a $ADDR -p /workspace/$MOUNT_FOLDER &
# Waits mounting DICOMFuse
for ((;;))
do
if [[ -d /workspace/$MOUNT_FOLDER/$DICOM_STORE_NAME/ ]]; then
break
fi
done
# Copies example.dcm into created DICOM Store. example.dcm has 111 StudyInstanceUID 111
# SeriesInstanceUID and 111 SOPInstanceUID
cp /workspace/build/example.dcm /workspace/$MOUNT_FOLDER/$DICOM_STORE_NAME/
CP_TO_DICOM_STORE_RESULT=$?
# Copies uploaded example.dcm from DICOM Store to the local workspace folder. Downloaded dcm file
# has SOPInstanceUID.dcm name or 111.dcm
cp /workspace/$MOUNT_FOLDER/$DICOM_STORE_NAME/111/111/111.dcm /workspace/build/
CP_FROM_DICOM_STORE_RESULT=$?
# Runs files comparison using the diff app
diff /workspace/build/example.dcm /workspace/build/111.dcm
DIFF_RESULT=$?
# Runs 111.dcm instance deletion in DICOM Store
rm /workspace/$MOUNT_FOLDER/$DICOM_STORE_NAME/111/111/111.dcm
RM_RESULT=$?
# Deletes created DICOMStore
gcloud alpha healthcare dicom-stores delete $DICOM_STORE_NAME \
--location=$LOCATION --dataset=$DATASET --quiet
# Checks exit codes of all operations
if [[ $CP_TO_DICOM_STORE_RESULT != 0 || $CP_FROM_DICOM_STORE_RESULT != 0 || $DIFF_RESULT != 0 || $RM_RESULT != 0 ]]; then
    exit 1;
fi