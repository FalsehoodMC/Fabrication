#!/bin/bash -e
if [ -f ~/ForgeryTools.jar ]; then
	canforgery=1
else
	echo "Forgery tools not found."
	echo "Performing a Fabric build only."
	echo
	canforgery=0
fi
./build-features.sh
rm -rf build/libs
echo Building Fabrication...
./gradlew clean build -x ap:clean
rm -f build/libs/*-dev.jar
fabrication=$(echo build/libs/fabrication*.jar)
if [ "$canforgery" == "1" ]; then
	forgery=$(echo "$fabrication" | sed "s/fabrication/forgery/")
	tmp=$(mktemp -d)
	forgeryIn="$tmp/forgery-in.jar"
	echo Processing JIJ dependencies...
	unzip -p "$fabrication" fabric.mod.json |sed 's/"name": "Fabrication"/"name": "Forgery"/g' > forgery/shadow/fabric.mod.json
	cp "$fabrication" forgery/shadow/in.jar
	zip -d forgery/shadow/in.jar META-INF/jars/*
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
	java -jar ~/ForgeryTools.jar "$forgeryIn" "$forgery" ~/.gradle/caches/fabric-loom/1.19.2/intermediary-v2.tiny ~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.19.2/mcp_mappings.tsrg ./forgery/build/libs/forgery.jar ~/.gradle/caches/fabric-loom/1.19.2/minecraft-merged-intermediary.jar com.unascribed.fabrication ~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.19.2/client_mappings.txt ~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.19.2/server_mappings.txt
	rm -rf $tmp
fi
echo Done
