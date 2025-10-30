#!/usr/bin/env bash

set -e

cleanup () {
  rm -rf swagger-ui-*
}

download_swagger_ui () {
  local version=$1

  status=$(curl --location --request GET \
   "https://github.com/swagger-api/swagger-ui/archive/refs/tags/v${version}.tar.gz" \
    --output swagger-ui-"${version}".tar.gz -s -w "%{http_code}\\n")

  if [[ ! $status == "200" ]]; then
    echo "Error while downloading swagger ui"
    exit 1
  else
    echo "Successfully downloaded swagger ui"
  fi
}

move_swagger_ui_bundle_to_target () {
  local version=$1

  # untar the swagger bundle
  tar -xf swagger-ui-"${version}".tar.gz

  # copy dist files to resources
  cp -R swagger-ui-"${version}"/dist/* src/main/resources/webroot/swagger/

  # replace swagger configuration file location
  sed -i'' -e '/url:/s/.*/    url: "swagger.yaml",/' src/main/resources/webroot/swagger/swagger-initializer.js
}

main () {
  local version=$1

  echo "Downloading swagger ui..."
  download_swagger_ui "${version}"
  move_swagger_ui_bundle_to_target "${version}"
  cleanup
}

main "$@"
