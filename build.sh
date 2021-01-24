#!/bin/bash -e
if [ -f ~/ForgeryTools.jar ]; then
	canforgery=1
else
	echo "Forgery tools not found. As of the time of writing, the Forgery tooling is not public."
	echo "Performing a Fabric build only."
	echo
	canforgery=0
fi
./build-features.sh
rm -rf build/libs
echo Building Fabrication...
gw clean build
rm build/libs/*-dev.jar
fabrication=$(echo build/libs/fabrication*.jar)
zip -d "$fabrication" com/mrcrayfish/*
if [ "$canforgery" == "1" ]; then
	cd forgery
	echo Building Forgery runtime...
	gw clean build
	cd ..
	forgery=$(echo "$fabrication" | sed "s/fabrication/forgery/")
	echo Running Forgery...
	java -jar ~/ForgeryTools.jar "$fabrication" "$forgery" ~/.gradle/caches/fabric-loom/mappings/intermediary-1.16.4-v2.tiny ~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.16.4/mcp_mappings.tsrg ./forgery/build/libs/forgery.jar ~/.gradle/caches/fabric-loom/minecraft-1.16.4-intermediary-net.fabricmc.yarn-1.16.4+build.6-v2.jar com.unascribed.fabrication
fi
echo Done
