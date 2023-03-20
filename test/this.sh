#!/bin/bash
cd `dirname "$0"`
instance="1194"
cp -r .. "./$instance"
cd "$instance"
git checkout 3.0/1.19.4
./build.sh
../var/apply.sh build/libs/fabrication* "fab$instance/.minecraft/mods"
../var/apply.sh build/libs/forgery* "fabForge$instance/.minecraft/mods"
cd ..
rm -rf "$instance"
./var/start.sh "fab$instance" & ./var/start.sh "fabForge$instance" &

