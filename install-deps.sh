#!/bin/bash
set -ex
if test -f "${GITHUB_WORKSPACE}/dependencies.sh"; then
	# shellcheck source=./dependencies.sh
	. "${GITHUB_WORKSPACE}/dependencies.sh"
elif test -f ./dependencies.sh; then
	. ./dependencies.sh
else
	echo "Can't find dependencies.sh" 1>&2
	exit 1
fi
mkdir -p "${HOME}/.ant/lib"
wget -nv "https://download.sourceforge.net/launch4j/launch4j-${LAUNCH4J_MAJOR}/${LAUNCH4J_VERSION}/launch4j-${LAUNCH4J_VERSION}-linux-x64.tgz" \
		-O "launch4j-${LAUNCH4J_VERSION}-linux-x64.tgz"
tar xzf "launch4j-${LAUNCH4J_VERSION}-linux-x64.tgz"
wget -nv "https://github.com/UltraMixer/JarBundler/releases/download/${JARBUNDLER_VERSION}/jarbundler-core-${JARBUNDLER_VERSION}.jar" \
		-O "${HOME}/.ant/lib/jarbundler-core-${JARBUNDLER_VERSION}.jar"
wget -nv "https://github.com/mickleness/pumpernickel/raw/master/release/jars/Pumpernickel.jar"
wget -nv "https://repo.maven.apache.org/maven2/com/yuvimasory/orange-extensions/${ORANGE_VERSION}/orange-extensions-${ORANGE_VERSION}.jar"
wget -nv "https://github.com/tofi86/universalJavaApplicationStub/archive/v${APP_STUB_VERSION}.tar.gz" -O \
		"universalJavaApplicationStub-${APP_STUB_VERSION}.tar.gz"
tar xzf "universalJavaApplicationStub-${APP_STUB_VERSION}.tar.gz"
case "${GITHUB_REF:-none}" in
refs/tags/v*) sed -i -e "s@SNAPSHOT@${GITHUB_REF#refs/tags/v}@" version.properties ;;
esac
wget -nv "https://ceylon-lang.org/download/dist/$(echo "${CEYLON_VERSION}"|sed 's@\.@_@g')" \
	--output-document=ceylon.zip
unzip -q ceylon.zip
# Work around eclipse/ceylon#7462
find source/ -name module.ceylon -exec grep -h maven: {} + | grep -v '^/' | \
		sed -e 's@native("jvm")@@' \
			-e 's@^[         ]*import maven:"\([^"]*\)" "\([^"]*\)";$@\1:\2@' | \
		sort -u | while read -r dependency; do
	mvn -B --no-transfer-progress dependency:get -Dartifact="${dependency}" || exit 2
done
# Takes dependencies that earlier command doesn't pull in
import_jar() {
	if ! test -f "${HOME}/.m2/repository/$(echo "${1}" | tr . /)/${2}/${3}/${2}-${3}.jar"; then
		mvn -B --no-transfer-progress dependency:get -Dartifact="${1}:${2}:${3}" || exit 2
	fi
	if test -f "lib/${2}.properties" && test "${4}" != "--force";then
		descriptor=("--descriptor=lib/${2}.properties")
	else
		descriptor=()
	fi
	case "${2}" in
	orange-extensions|RoaringBitmap|takes|sqlite-jdbc) import_as="${1}:${2}" ;;
	*) import_as="${2}" ;;
	esac
	if ! "ceylon-${CEYLON_VERSION}/bin/ceylon" import-jar "${descriptor[@]}" "${import_as}/${3}" "${HOME}/.m2/repository/$(echo "${1}" | tr . /)/${2}/${3}/${2}-${3}.jar" ${4}; then
		echo "Failed in ${import_as}"
		return 2
	fi
}
import_jar javax.jms javax.jms-api 2.0.1
import_jar com.sun.mail javax.mail 1.6.2
import_jar javax.mail javax.mail-api 1.6.2
import_jar log4j log4j 1.2.17
import_jar com.fasterxml.jackson.core jackson-core 2.7.6
import_jar com.fasterxml.jackson.core jackson-annotations 2.7.6
import_jar com.fasterxml.jackson.core jackson-databind 2.7.6
import_jar org.aspectj aspectjrt 1.8.7
import_jar com.jcabi jcabi-log 0.17.2 --force
import_jar javax.validation validation-api 1.1.0.Final
import_jar com.jcabi jcabi-aspects 0.23.1
import_jar org.slf4j slf4j-api 1.7.25 --force
import_jar org.slf4j slf4j-jdk14 1.7.25
import_jar ch.qos.cal10n cal10n-api 0.8.1
import_jar org.slf4j slf4j-api 1.7.25
import_jar com.jcabi jcabi-log 0.17.2
import_jar com.jcabi jcabi-immutable 1.5
import_jar commons-io commons-io 2.5
import_jar xml-apis xml-apis 1.4.01
import_jar com.jcabi jcabi-xml 0.17.2
import_jar com.google.errorprone error_prone_annotations 2.7.1
import_jar com.google.code.findbugs jsr305 3.0.2
import_jar com.google.guava failureaccess 1.0.1
import_jar com.google.guava guava 30.1.1-jre
import_jar javax.servlet javax.servlet-api 4.0.0
import_jar com.jcabi jcabi-manifests 1.1
import_jar com.jcabi jcabi-http 1.17.1 --force
import_jar javax.ws.rs jsr311-api 1.1.1
import_jar org.apache.httpcomponents httpcore 4.4.4
import_jar org.apache.httpcomponents httpmime 4.3
import_jar com.jcabi jcabi-w3c 1.3
import_jar com.jcabi jcabi-matchers 1.5.3 --force
import_jar com.sun.grizzly grizzly-servlet-webserver 1.9.64
import_jar javax.xml.bind jaxb-api 2.2.12
import_jar javax.xml.bind jsr173_api 1.0
import_jar commons-codec commons-codec 1.15
import_jar avalon-framework avalon-framework 4.1.3 --force
import_jar logkit logkit 1.0.1
import_jar avalon-framework avalon-framework 4.1.3
import_jar commons-logging commons-logging 1.0.4
import_jar org.apache.httpcomponents httpclient 4.5.1 --force
import_jar org.jsoup jsoup 1.8.3
import_jar javax.json javax.json-api 1.0
import_jar org.hamcrest hamcrest-core 1.3
import_jar org.hamcrest hamcrest-library 1.3
import_jar com.jcabi jcabi-matchers 1.5.3
import_jar com.jcabi jcabi-http 1.17.1
import_jar antlr antlr 2.7.7
import_jar org.antlr stringtemplate 3.2.1
import_jar org.antlr antlr-runtime 3.5.2
import_jar com.jcabi.incubator xembly 0.22
import_jar com.restfb restfb 2.0.0-rc.3
import_jar org.apache.commons commons-lang3 3.5
import_jar org.apache.velocity velocity-engine-core 2.0
import_jar org.cactoos cactoos 0.42
import_jar org.takes takes 1.19
