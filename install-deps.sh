#!/bin/sh
set -ex
if test -f "${TRAVIS_BUILD_DIR}/dependencies.sh"; then
	. "${TRAVIS_BUILD_DIR}/dependencies.sh"
elif test -f dependencies.sh; then
	. dependencies.sh
else
	echo "Can't find dependencies.sh" 1>&2
	exit 1
fi
mkdir -p "${HOME}/.ant/lib"
wget https://download.sourceforge.net/launch4j/launch4j-${launch4j_major}/${launch4j_version}/launch4j-${launch4j_version}-linux.tgz \
		-O "launch4j-${launch4j_version}-linux.tgz"
tar xzf "launch4j-${launch4j_version}-linux.tgz"
wget "https://github.com/UltraMixer/JarBundler/releases/download/${jarbundler_version}/jarbundler-core-${jarbundler_version}.jar" \
		-O "${HOME}/.ant/lib/jarbundler-core-${jarbundler_version}.jar"
for jar in pump-swing pump-common pump-awt pump-image pump-button;do
	wget "https://github.com/mickleness/pumpernickel/raw/master/pump-release/com/pump/${jar}/${pumpernickel_version}/${jar}-${pumpernickel_version}.jar"
done
# TODO: HTTPS !!!!
wget "http://central.maven.org/maven2/com/yuvimasory/orange-extensions/${orange_version}/orange-extensions-${orange_version}.jar"
sudo apt-get update -qq
sudo apt-get install genisoimage
wget "https://github.com/tofi86/universalJavaApplicationStub/archive/v${app_stub_version}.tar.gz" -O \
		"universalJavaApplicationStub-${app_stub_version}.tar.gz"
tar xzf "universalJavaApplicationStub-${app_stub_version}.tar.gz"
if test -n "${TRAVIS_TAG}"; then
	echo "${TRAVIS_TAG}" | sed 's@^v[0-9]\.[0-9]\.\([0-9]*\|[0-9]*\.[0-9]*\|[0-9]*[-_]rc[0-9]*\)$@s:SNAPSHOT:\1:@' | \
		sed -f - -i version.properties
fi
wget "https://ceylon-lang.org/download/dist/$(echo "${ceylon_version}" | sed 's@\.@_@g')" --output-document=ceylon.zip
unzip ceylon.zip
