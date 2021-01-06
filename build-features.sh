#!/bin/bash -e
node parse-features.js features.txt features.json
cp features.json src/main/resources
node transform-features-config.js
