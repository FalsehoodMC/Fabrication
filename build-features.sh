#!/bin/bash -e
node tools/parse-features.js features.yml features.json
cp features.json src/main/resources
node tools/transform-features-config.js
node tools/create-pages.js
