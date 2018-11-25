#!/bin/sh
# This is for use by Travis CI, to reduce too-long lines in .travis.yml
. dependencies.sh
ant \
	-lib /usr/share/java/ant-contrib.jar \
	-lib "$(pwd)/launch4j" \
	-lib "$(pwd)/launch4j/lib" \
	-Dlaunch4j.dir="$(pwd)/launch4j" \
	-Dwindowmenu.jar.path="$(pwd)/pump-swing-${pumpernickel_version}.jar" \
	-Dpump.common.path="$(pwd)/pump-common-${pumpernickel_version}.jar" \
	-Dpump.awt.path="$(pwd)/pump-awt-${pumpernickel_version}.jar" \
	-Dpump.image.path="$(pwd)/pump-image-${pumpernickel_version}.jar" \
	-Dpump.button.path="$(pwd)/pump-button-${pumpernickel_version}.jar" \
	-Dstub-script-path="$(pwd)/universalJavaApplicationStub-${app_stub_version}/src/universalJavaApplicationStub" \
	-Dapple.extensions.path="$(pwd)/orange-extensions-${orange_version}.jar" \
	-Dceylon.home="$(pwd)/ceylon-${ceylon_version}" \
	release
