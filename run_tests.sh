#!/bin/sh
# This is for use by Travis CI, to reduce too-long lines in .travis.yml
. dependencies.sh
ant \
	-Dlaunch4j.dir="$(pwd)/launch4j" \
	-Dwindowmenu.jar.path="$(pwd)/pump-swing-${pumpernickel_version}.jar" \
	-Dpump.common.path="$(pwd)/pump-common-${pumpernickel_version}.jar" \
	-Dpump.awt.path="$(pwd)/pump-awt-${pumpernickel_version}.jar" \
	-Dpump.image.path="$(pwd)/pump-image-${pumpernickel_version}.jar" \
	-Dpump.button.path="$(pwd)/pump-button-${pumpernickel_version}.jar" \
	-Dapple.extensions.path="$(pwd)/orange-extensions-${orange_version}.jar" \
	-Dceylon.home="$(pwd)/ceylon-${ceylon_version}" \
	test
