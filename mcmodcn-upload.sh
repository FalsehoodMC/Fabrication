#!/bin/bash
if [ -z $5 ]; then
	echo "$0: usage: $0 <classID> <mcver> <apiList> <tagList> <file>"
	exit 1
fi

while true; do
	resp=$(curl -c ~/.local/share/mcmodcn-cookies --connect-timeout 20 'https://modfile-dl.mcmod.cn/action/upload/' -X POST \
		-H 'Origin: https://modfile-dl.mcmod.cn' -H "Cookie: $MCMODCN_COOKIE" \
		-F "classID=$1" -F "mcverList=$2" -F "platformList=1" -F "apiList=$3" -F "tagList=$4" \
		-F "0=@$5;type=application/x-java-archive")
	if [ "$?" != "0" ]; then
		sleep 2
		continue
	fi
	state=$(echo "$resp" | jq .state)
	if [ "$state" == "101" ]; then
		echo 'Session invalid. :('
		exit 3
	fi
	if [ "$state" == "0" ]; then
		echo 'Successfully uploaded!'
		exit 0
	else
		echo Failure $resp - retrying
		sleep 2
	fi
done
