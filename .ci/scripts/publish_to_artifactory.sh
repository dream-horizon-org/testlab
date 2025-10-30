#!/usr/bin/env bash

set -e

cleanup () {
  rm -rf ./target
}

create_zip () {
  local artifact_name=$1
  local artifact_version=$2
  local zip_name="${artifact_name}-${artifact_version}.zip"

  cp -r .odin/"${artifact_name}" target/"${artifact_name}/.odin/"
  cd ./target || exit 1
  zip -q -r -y "${zip_name}" "${artifact_name}"

  echo "Compressed target to zip ${zip_name}"
  cd ..
}

upload_to_jfrog () {
  local artifact_name=$1
  local artifact_version=$2
  local zip_name="${artifact_name}-${artifact_version}.zip"

  cd ./target || exit 1

  status=$(curl --location -u "${JFROG_USERNAME}":"${JFROG_PASSWORD}" \
  --request PUT "${JFROG_ARTIFACTORY_SERVER}/${JFROG_ARTIFACTORY_REPOSITORY}/${artifact_name}/${artifact_version}/${zip_name}" \
    -T "${zip_name}" -w "%{http_code}\\n" -o response.json)

  if [[ ! $status == "201" ]]; then
    echo "Error while uploading artifact to jfrog"
    cat response.json
    cd ..
    cleanup
    exit 1
  else
    echo "Successfully uploaded ${zip_name} to Jfrog"
    cd ..
  fi
}

update_artifact_property () {
  local artifact_name=$1
  local artifact_version=$2
  local commit_id=$3

  cd ./target || exit 1

  url_artifact_name_final=$(basename "$(jq '.path' response.json)" | sed 's/"//g')
  echo "Artifact name in Jfrog: ${url_artifact_name_final}"

  echo "Updating commitId -> ${commit_id}  as a property for the artifact -> ${url_artifact_name_final}"
  commit_status=$(curl --location -u "${JFROG_USERNAME}":"${JFROG_PASSWORD}" \
  --request PUT "${JFROG_ARTIFACTORY_SERVER}/api/storage/${JFROG_ARTIFACTORY_REPOSITORY}/${artifact_name}/${artifact_version}/${url_artifact_name_final}?properties=commitId=${commit_id}" \
    -s -w "%{http_code}\\n")

  echo "commitStatus=${commit_status}"

  if [[ ! ${commit_status} == "204" ]]; then
    echo "Error while setting up commit Id in the artifact to jfrog"
  fi

  cd ..
}

main () {
  local artifact_name=$1
  local artifact_version=$2
  local commit_id=$3

  create_zip "${artifact_name}" "${artifact_version}"
  upload_to_jfrog "${artifact_name}" "${artifact_version}"
  update_artifact_property "${artifact_name}" "${artifact_version}" "${commit_id}"

  cleanup

  echo "Successfully pushed ${artifact_name}:${artifact_version} with commit:${commit_id} to Jfrog"
}

main "$@"
