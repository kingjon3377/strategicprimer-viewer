#!/bin/sh
set -ex
mkdir -p "${HOME}/.ant/lib"
wget http://download.sourceforge.net/launch4j/launch4j-3/3.9/launch4j-3.9-linux.tgz \
        -O launch4j-3.9-linux.tgz
tar xzf launch4j-3.9-linux.tgz
wget https://github.com/UltraMixer/JarBundler/releases/download/3.3.0/jarbundler-core-3.3.0.jar \
        -O "${HOME}/.ant/lib/jarbundler-core-3.3.0.jar"
wget http://javagraphics.java.net/jars/WindowMenu.jar
wget http://central.maven.org/maven2/com/yuvimasory/orange-extensions/1.3.0/orange-extensions-1.3.0.jar
sudo apt-get update -qq
sudo apt-get install genisoimage
wget https://github.com/tofi86/universalJavaApplicationStub/archive/v2.0.2.tar.gz -O \
        universalJavaApplicationStub-2.0.2.tar.gz
tar xzf universalJavaApplicationStub-2.0.2.tar.gz
if test -n "${TRAVIS_TAG}"; then
    echo "${TRAVIS_TAG}" | sed 's@^v[0-9]\.[0-9]\.\([0-9]*\|[0-9]*[-_]rc[0-9]*\)$@s:SNAPSHOT:\1:@' | \
        sed -f - -i version.properties
fi
get_maven_url() {
    curl -L "https://search.maven.org/remotecontent?filepath=${1}/${2}/${3}/${2}-${3}.jar" \
        -o "${HOME}/.ant/lib/${4}.jar"
}
jacoco_ver=0.7.4.201502262128
get_maven_url org/jacoco org.jacoco.core ${jacoco_ver} jacoco
get_maven_url org/jacoco org.jacoco.ant ${jacoco_ver} jacocoant
get_maven_url org/jacoco org.jacoco.agent ${jacoco_ver} jacocoagent
get_maven_url org/jacoco org.jacoco.report ${jacoco_ver} jacocoreport
get_maven_url org/ow2/asm asm-debug-all 5.0.3 asm-debug-all
wget https://ceylon-lang.org/download/dist/1_3_2 --output-document=ceylon.zip
unzip ceylon.zip
