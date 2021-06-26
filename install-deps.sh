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
if test -n "${GITHUB_REF}"; then
	echo "${GITHUB_REF}" | sed 's@^refs/tags/v\([0-9]\.[0-9]\.[0-9]*\|[0-9]*\.[0-9]*\|[0-9]*[-_]rc[0-9]*\)$@s:SNAPSHOT:\1:@' | \
		sed -f - -i version.properties
fi
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
for dependency in com.jcabi:jcabi-http:1.17.1 com.jcabi:jcabi-xml:0.17.2 \
		com.jcabi.incubator:xembly:0.22 com.sun.mail:javax.mail:jar:1.6.2 \
		com.restfb:restfb:2.0.0-rc.3 log4j:log4j:1.2.17 \
		org.apache.velocity:velocity-engine-core:2.0 \
		org.hamcrest:hamcrest-library:1.3 org.slf4j:slf4j-api:1.8.0-alpha2 ; do
	mvn -B --no-transfer-progress dependency:get -Dartifact="${dependency}" || exit 2
done
import_jar() {
	if test -f "lib/${2}.properties";then
		descriptor=("--descriptor=lib/${2}.properties")
	else
		descriptor=()
	fi
	"ceylon-${CEYLON_VERSION}/bin/ceylon" import-jar "${descriptor[@]}" "${1}:${2}/${3}" "${HOME}/.m2/repository/$(echo "${1}" | tr . /)/${2}/${3}/${2}-${3}.jar"
}
import_jar javax.jms javax.jms-api 2.0.1
import_jar com.sun.mail javax.mail 1.6.2
import_jar javax.mail javax.mail-api 1.6.2
import_jar log4j log4j 1.2.17
import_jar com.jcabi jcabi-http 1.17.1
import_jar org.takes takes 1.19
