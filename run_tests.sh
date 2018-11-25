#!/bin/sh
# This is for use by Travis CI, to reduce too-long lines in .travis.yml
if test -f "${TRAVIS_BUILD_DIR}/dependencies.sh"; then
	. "${TRAVIS_BUILD_DIR}/dependencies.sh"
elif test -f dependencies.sh; then
	. dependencies.sh
else
	echo "Can't find dependencies.sh" 1>&2
	exit 1
fi
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
