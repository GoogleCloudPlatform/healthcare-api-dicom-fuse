#!/bin/bash

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

readonly STAGE="${1}"
readonly PROJECT="${2}"
readonly LOCATION="${3}"
readonly DATASET="${4}"
readonly MAX_STUDY_STORE="${5}"
readonly LAST_STUDY="${6}"

# Check exit codes of all operations
check_exit_code() {
  exit_code="${1}"
  error_message="${2}"
  if [[ "${exit_code}" != 0 ]]; then
    echo "${error_message}"
    exit 1
  fi
}

# Create unique DICOM Store names
readonly dicom_store_name="dicom_store_name_$(openssl rand -hex 12)"
readonly test_store_1="test_store_1_$(openssl rand -hex 12)"
readonly test_store_2="test_store_2_$(openssl rand -hex 12)"
# Create a folder to mount DICOMFuse
readonly mount_folder="dicom"
mkdir "${mount_folder}"
# Create unique DICOM Store
if ! gcloud beta healthcare dicom-stores create "${dicom_store_name}" \
  --location="${LOCATION}" \
  --dataset="${DATASET}" \
  --quiet
then
  echo "Failed to create DICOM Store"
  exit 1
fi
# Install libfuse
apt update
apt install -y libfuse2
# Get JAR version from pom.xml
jar_version="$(grep -m 1 "<version>" /workspace/pom.xml \
  | grep -Eo "[[:digit:]]+.[[:digit:]]+.[[:digit:]]+")"
jar_name="healthcare-api-dicom-fuse-${jar_version}.jar"
# Run DICOMFuse
addr="https://healthcare.googleapis.com/${STAGE}/projects/${PROJECT}/locations/${LOCATION}/datasets/${DATASET}"
java -jar "/workspace/target/${jar_name}" -a "${addr}" -p "/workspace/${mount_folder}" &
# Wait mounting DICOMFuse
for ((;;)); do
  if [[ -d "/workspace/${mount_folder}/${dicom_store_name}/" ]]; then
    break
  fi
done
# Copy example.dcm into created DICOM Store. example.dcm has 111
# StudyInstanceUID 111 SeriesInstanceUID and 111 SOPInstanceUID
cp /workspace/build/example.dcm "/workspace/${mount_folder}/${dicom_store_name}/"
cp_to_dicom_store_result=$?
check_exit_code "${cp_to_dicom_store_result}" \
  "Copying to DICOM Store failed"
# Copy uploaded example.dcm from DICOM Store to the local workspace folder.
# Downloaded dcm file has SOPInstanceUID.dcm name or 111.dcm
cp "/workspace/${mount_folder}/${dicom_store_name}/111/111/111.dcm" /workspace/build/
cp_from_dicom_store_result=$?
check_exit_code "${cp_from_dicom_store_result}" \
  "Copying from DICOM Store failed"
# Run files comparison using the diff app
diff /workspace/build/example.dcm /workspace/build/111.dcm
diff_result=$?
check_exit_code "${diff_result}" \
  "Files are not equal"
# Run 111.dcm instance deletion in DICOM Store
rm "/workspace/${mount_folder}/${dicom_store_name}/111/111/111.dcm"
rm_result=$?
check_exit_code "${rm_result}" \
  "Removing 111.dcm instance in ${dicom_store_name} DICOM Store failed"
# Create DICOM Store
mkdir "/workspace/${mount_folder}/${test_store_1}"
mkdir_result=$?
check_exit_code "${mkdir_result}" \
  "Failed to create DICOM Store"
# Rename DICOM Store
mv "/workspace/${mount_folder}/${test_store_1}" \
  "/workspace/${mount_folder}/${test_store_2}"
mv_result=$?
check_exit_code "${mv_result}" \
  "Failed to rename DICOM Store"
# Delete created DICOM Stores
if ! gcloud beta healthcare dicom-stores delete "${dicom_store_name}" \
  --location="${LOCATION}" \
  --dataset="${DATASET}" \
  --quiet
then
  echo "Failed to delete DICOM Store"
  exit 1
fi
if ! gcloud beta healthcare dicom-stores delete "${test_store_2}" \
  --location="${LOCATION}" \
  --dataset="${DATASET}" \
  --quiet
then
  echo "Failed to delete DICOM Store"
  exit 1
fi

max_study_path="/workspace/${mount_folder}/${MAX_STUDY_STORE}/"
studies_in_store=$(($(find "${max_study_path}" -maxdepth 1 -type d | wc -l) - 1))

if [[ "${studies_in_store}" != 15000 ]]; then
  echo "DICOM Store don't have enough studies for test"
  exit 1
fi
if [[ $(find "${max_study_path}" -maxdepth 1 -type d -name "${LAST_STUDY}" \
  | grep -c "${LAST_STUDY}") != 0 ]]
then
  echo "15001nd Study shouldn't be in folder initially"
  exit 1
fi
if [[ ! -d "${max_study_path}${LAST_STUDY}" ]]; then
  echo "Can't navigate to 15001nd Study"
  exit 1
fi

# Check creation of forbidden folder
folder_name=".Trash-1000"
mkdir "/workspace/${mount_folder}/${folder_name}"
if gcloud beta healthcare dicom-stores describe "${folder_name}" \
  --location="${LOCATION}" \
  --dataset="${DATASET}" \
  --quiet
then
  echo "Error. The forbidden folder ${folder_name} was created."
  gcloud beta healthcare dicom-stores delete "${folder_name}" \
  --location="${LOCATION}" \
  --dataset="${DATASET}" \
  --quiet
  exit 1
fi