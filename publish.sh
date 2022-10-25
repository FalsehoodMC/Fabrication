#!/bin/bash -e
if [ -z "$4" ]; then
	echo "Need a Curse API key, Modrinth API key, mcmod.cn cookie, and GitHub API key to publish."
	exit 2
fi

tasks=""

if [ "$1" != "-" ]; then
	export CURSE_TOKEN="$1"
	tasks="$tasks curseforge"
fi
if [ "$2" != "-" ]; then
	export MODRINTH_TOKEN="$2"
	tasks="$tasks modrinth"
fi
if [ "$4" != "-" ]; then
	export GITHUB_TOKEN="$4"
	tasks="$tasks githubRelease"
fi
if [ "$3" != "-" ]; then
	export MCMODCN_COOKIE="$3"
	tasks="$tasks mcmodcn"
fi

gw $tasks
PUBLISH_FORGERY=1 gw $tasks

