#!/bin/sh
# This is for use by Travis CI, to reduce too-long lines in .travis.yml
ant \
	-lib /usr/share/java/ant-contrib.jar \
	-lib "$(pwd)/launch4j" \
	-lib "$(pwd)/launch4j/lib" \
	-Dlaunch4j.dir="$(pwd)/launch4j" \
	-Dwindowmenu.jar.path="$(pwd)/pump-swing-1.0.00.jar" \
	-Dpump.common.path="$(pwd)/pump-common-1.0.00.jar" \
	-Dpump.awt.path="$(pwd)/pump-awt-1.0.00.jar" \
	-Dpump.image.path="$(pwd)/pump-image-1.0.00.jar" \
	-Dpump.button.path="$(pwd)/pump-button-1.0.00.jar" \
	-Dstub-script-path="$(pwd)/universalJavaApplicationStub-3.0.2/src/universalJavaApplicationStub" \
	-Dapple.extensions.path="$(pwd)/orange-extensions-1.3.0.jar" \
	-Dceylon.home="$(pwd)/ceylon-1.3.3" \
	release
