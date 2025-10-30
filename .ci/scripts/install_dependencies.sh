#!/usr/bin/env bash

set -e

install_jq () {
    echo "installing jq"
    jq --version
    status=$?
    if [[ $status -ne 0 ]]; then
        sudo apt install jq
    fi
    echo "jq installed"
}

install_yq () {
    echo "installing yq"
    if ! command -v yq &> /dev/null; then
        sudo add-apt-repository -y ppa:rmescandon/yq
        sudo apt update
        sudo apt install yq -y
    fi
    echo "yq installed"
}

main () {
    install_jq
    install_yq
}

main
