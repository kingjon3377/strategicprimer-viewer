#!/bin/sh
set -ex
if test -f "${TRAVIS_BUILD_DIR}/dependencies.sh"; then
	# shellcheck source=./dependencies.sh
	. "${TRAVIS_BUILD_DIR}/dependencies.sh"
elif test -f dependencies.sh; then
	. dependencies.sh
else
	echo "Can't find dependencies.sh" 1>&2
	exit 1
fi
mkdir -p "${HOME}/.ant/lib"
wget "https://download.sourceforge.net/launch4j/launch4j-${LAUNCH4J_MAJOR}/${LAUNCH4J_VERSION}/launch4j-${LAUNCH4J_VERSION}-linux.tgz" \
		-O "launch4j-${LAUNCH4J_VERSION}-linux.tgz"
tar xzf "launch4j-${LAUNCH4J_VERSION}-linux.tgz"
wget "https://github.com/UltraMixer/JarBundler/releases/download/${JARBUNDLER_VERSION}/jarbundler-core-${JARBUNDLER_VERSION}.jar" \
		-O "${HOME}/.ant/lib/jarbundler-core-${JARBUNDLER_VERSION}.jar"
wget "https://github.com/mickleness/pumpernickel/raw/master/release/jars/Pumpernickel.jar"
wget "https://repo.maven.apache.org/maven2/com/yuvimasory/orange-extensions/${ORANGE_VERSION}/orange-extensions-${ORANGE_VERSION}.jar"
sudo apt-get update -qq
sudo apt-get install genisoimage
wget "https://github.com/tofi86/universalJavaApplicationStub/archive/v${APP_STUB_VERSION}.tar.gz" -O \
		"universalJavaApplicationStub-${APP_STUB_VERSION}.tar.gz"
tar xzf "universalJavaApplicationStub-${APP_STUB_VERSION}.tar.gz"
if test -n "${TRAVIS_TAG}"; then
	echo "${TRAVIS_TAG}" | sed 's@^v[0-9]\.[0-9]\.\([0-9]*\|[0-9]*\.[0-9]*\|[0-9]*[-_]rc[0-9]*\)$@s:SNAPSHOT:\1:@' | \
		sed -f - -i version.properties
fi
wget "https://ceylon-lang.org/download/dist/$(echo "${CEYLON_VERSION}"|sed 's@\.@_@g')" \
	--output-document=ceylon.zip
unzip ceylon.zip
