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

readonly REPO_NAME=$1
readonly TAG_NAME=$2
# Get GitHub user and GitHub repo from REPO_NAME
IFS='_' read -ra array <<< "${REPO_NAME}"
githubUser="${array[1]}"
githubRepo="${array[2]}"
if [[ -z "${githubUser}" ]]
then
  githubUser="GoogleCloudPlatform"
  githubRepo="${REPO_NAME}"
fi
# Create request.json with request parameters
echo "{\"tag_name\": \"${TAG_NAME}\",\"name\": \"${TAG_NAME}\"}" > request.json
# Create a request for creating a release on GitHub page
readonly RESP_FILE="response.json"
responseCode="$(curl -# -X POST \
-H "Content-Type:application/json" \
-H "Accept:application/json" \
-w "%{http_code}" \
--data-binary "@/workspace/request.json" \
"https://api.github.com/repos/${githubUser}/${githubRepo}/releases?access_token=${ACCESS_TOKEN}" \
-o "${RESP_FILE}")"
# Check status code
if [[ "${responseCode}" != 201 ]]; then
  cat "${RESP_FILE}"
  exit 1;
fi
# Get release id from response.json
releaseId="$(grep -wm 1 "id" /workspace/response.json \
 | grep -Eo "[[:digit:]]+")"
# Get JAR version from pom.xml
jarVersion=$(grep -m 1 "<version>" /workspace/pom.xml \
 | grep -Eo "[[:digit:]]+.[[:digit:]]+")
jarName="healthcare-api-dicom-fuse-${jarVersion}.jar"
# Upload JAR to GitHub releases page
responseCode="$(curl -# -X POST -H "Authorization: token "${ACCESS_TOKEN} \
-H "Content-Type:application/octet-stream" \
-w "%{http_code}" \
--data-binary "@/workspace/target/${jarName}" \
"https://uploads.github.com/repos/${githubUser}/${githubRepo}/releases/${releaseId}/assets?name=${jarName}" \
-o "${RESP_FILE}")"
# Check status code
if [[ "${responseCode}" != 201 ]]; then
  cat "${RESP_FILE}"
  exit 2;
fi
