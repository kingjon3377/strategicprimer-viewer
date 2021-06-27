#!/bin/sh
# This is for use by Travis CI, to reduce too-long lines in .travis.yml
if test -f "${GITHUB_WORKSPACE}/dependencies.sh"; then
	# shellcheck source=./dependencies.sh
	. "${GITHUB_WORKSPACE}/dependencies.sh"
elif test -f ./dependencies.sh; then
	. ./dependencies.sh
else
	echo "Can't find dependencies.sh" 1>&2
	exit 1
fi
ant \
	-lib /usr/share/java/ant-contrib.jar \
	-lib "$(pwd)/launch4j" \
	-lib "$(pwd)/launch4j/lib" \
	-Dlaunch4j.dir="$(pwd)/launch4j" \
	-Dpumpernickel.path="$(pwd)/Pumpernickel.jar" \
	-Dstub-script-path="$(pwd)/universalJavaApplicationStub-${APP_STUB_VERSION}/src/universalJavaApplicationStub" \
	-Dapple.extensions.path="$(pwd)/orange-extensions-${ORANGE_VERSION}.jar" \
	-Dceylon.home="$(pwd)/ceylon-${CEYLON_VERSION}" \
	release
