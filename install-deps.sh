#!/bin/sh
set -ex
mkdir -p "${HOME}/.ant/lib"
wget http://sourceforge.net/projects/launch4j/files/launch4j-3/3.8/launch4j-3.8-linux.tgz/download -O launch4j-3.8-linux.tgz
tar xzf launch4j-3.8-linux.tgz
wget https://github.com/tofi86/Jarbundler/releases/download/v2.4.0/jarbundler-2.4.0.tar.gz
tar xzf jarbundler-2.4.0.tar.gz
mv jarbundler-2.4.0/jarbundler-2.4.0.jar "${HOME}/.ant/lib/"
wget http://central.maven.org/maven2/org/eclipse/jdt/org.eclipse.jdt.annotation/2.0.0/org.eclipse.jdt.annotation-2.0.0.jar
wget http://javagraphics.java.net/jars/WindowMenu.jar
sudo apt-get update -qq
sudo apt-get install genisoimage
wget https://github.com/tofi86/universalJavaApplicationStub/archive/v0.9.0.tar.gz -O universalJavaApplicationStub-0.9.0.tar.gz
tar xzf universalJavaApplicationStub-0.9.0.tar.gz
if test -n "${TRAVIS_TAG}"; then
    echo "${TRAVIS_TAG}" | sed 's@^v[0-9]\.[0-9]\.\([0-9]*\)$@s:SNAPSHOT:\1:@' | sed -f - -i version.properties
fi
