#!/bin/bash
pushd
cd `dirname "$0"`
git checkout 3.0/1.18
./this.sh
git checkout 3.0/1.19
./this.sh
git checkout 3.0/1.17
./this.sh
git checkout 3.0/1.16
./this.sh

popd
