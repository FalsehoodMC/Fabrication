#!/bin/bash
cd "`dirname "$0"`/.."
instance="1171"
./build.sh
./test/var/apply.sh build/libs/fabrication* "fab$instance/.minecraft/mods"
./test/var/apply.sh build/libs/forgery* "fabForge$instance/.minecraft/mods"
./test/var/start.sh "fab$instance" & ./test/var/start.sh "fabForge$instance" &

