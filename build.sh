#!/bin/bash -e
if [ -f ~/ForgeryTools.jar ]; then
	canforgery=1
else
	echo "Forgery tools not found. As of the time of writing, the Forgery tooling is not public."
fi
if [ -n "$JAVA17_HOME" ]; then
	export JAVA_HOME=$JAVA17_HOME
fi
./build-features.sh
rm -rf build/libs
echo Building Fabrication...
./gradlew clean build
rm build/libs/*-dev.jar
fabrication=$(echo build/libs/fabrication*.jar)
zip -d "$fabrication" com/mrcrayfish/* svenhjol/charm/*
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
	java -jar ~/ForgeryTools.jar "$forgeryIn" "$forgery" /home/una/.gradle/caches/fabric-loom/1.18.1/intermediary-v2.tiny ~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.18.1/mcp_mappings.tsrg ./forgery/build/libs/forgery.jar ~/.gradle/caches/fabric-loom/1.18.1/net.fabricmc.yarn.1_18_1.1.18.1+build.7-v2/minecraft-intermediary.jar com.unascribed.fabrication ~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.18.1/client_mappings.txt ~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.18.1/server_mappings.txt
	rm -rf $tmp
fi
echo Done
