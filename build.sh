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
./gradlew clean build
rm build/libs/*-dev.jar
fabrication=$(echo build/libs/fabrication*.jar)
zip -d "$fabrication" com/mrcrayfish/*
if [ "$canforgery" == "1" ]; then
	forgery=$(echo "$fabrication" | sed "s/fabrication/forgery/")
	tmp=$(mktemp -d)
	forgeryIn="$tmp/forgery-in.jar"
	echo Processing JIJ dependencies...
	zip -d "$fabrication" META-INF/jars/*
	unzip -p "$fabrication" fabric.mod.json > forgery/shadow/fabric.mod.json
	cp "$fabrication" forgery/shadow/in.jar
	cd forgery/shadow
	./gradlew clean shadowHack
	cp fabric.mod.json build/libs
	cd build/libs
	zip -9 out.jar fabric.mod.json
	cd ../../../..
	cp forgery/shadow/build/libs/out.jar "$forgeryIn"
	cd forgery
	echo Building Forgery runtime...
	./gradlew clean build
	cd ..
	echo Running Forgery...
	java -jar ~/ForgeryTools.jar "$forgeryIn" "$forgery" ~/.gradle/caches/fabric-loom/mappings/intermediary-1.16.4-v2.tiny ~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.16.4/mcp_mappings.tsrg ./forgery/build/libs/forgery.jar ~/.gradle/caches/fabric-loom/minecraft-1.16.4-intermediary-net.fabricmc.yarn-1.16.4+build.6-v2.jar com.unascribed.fabrication
	rm -rf $tmp
fi
echo Done
