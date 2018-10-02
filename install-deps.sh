#!/bin/sh
set -ex
mkdir -p "${HOME}/.ant/lib"
wget https://download.sourceforge.net/launch4j/launch4j-3/3.12/launch4j-3.12-linux.tgz \
		-O launch4j-3.12-linux.tgz
tar xzf launch4j-3.12-linux.tgz
wget https://github.com/UltraMixer/JarBundler/releases/download/3.3.0/jarbundler-core-3.3.0.jar \
		-O "${HOME}/.ant/lib/jarbundler-core-3.3.0.jar"
for jar in pump-swing pump-common pump-awt pump-image pump-button;do
    wget https://github.com/mickleness/pumpernickel/raw/master/pump-release/com/pump/${jar}/1.0.00/${jar}-1.0.00.jar
done
wget http://central.maven.org/maven2/com/yuvimasory/orange-extensions/1.3.0/orange-extensions-1.3.0.jar
sudo apt-get update -qq
sudo apt-get install genisoimage
wget https://github.com/tofi86/universalJavaApplicationStub/archive/v3.0.3.tar.gz -O \
		universalJavaApplicationStub-3.0.4.tar.gz
tar xzf universalJavaApplicationStub-3.0.4.tar.gz
if test -n "${TRAVIS_TAG}"; then
	echo "${TRAVIS_TAG}" | sed 's@^v[0-9]\.[0-9]\.\([0-9]*\|[0-9]*\.[0-9]*\|[0-9]*[-_]rc[0-9]*\)$@s:SNAPSHOT:\1:@' | \
		sed -f - -i version.properties
fi
wget https://ceylon-lang.org/download/dist/1_3_3 --output-document=ceylon.zip
unzip ceylon.zip
