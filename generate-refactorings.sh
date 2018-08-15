#!/usr/bin/env bash

if [ $# -ne "2" ]; then
    echo "usage: generate-refactorings <path to project> <path to output file>"
    exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )" # from https://stackoverflow.com/a/246128

$DIR/gradlew -p $DIR runRefactoringsGeneration -PpathToProject="$PWD/$1" -PpathToOutput="$PWD/$2"

