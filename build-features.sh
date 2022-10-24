#!/bin/bash -e
./gradlew fabGenFeatures
node tools/create-pages.js || echo 'Cannot build pages'
