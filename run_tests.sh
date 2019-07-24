#!/bin/sh
# This is for use by Travis CI, to reduce too-long lines in .travis.yml
if test -f "${TRAVIS_BUILD_DIR}/dependencies.sh"; then
	# shellcheck source=./dependencies.sh
	. "${TRAVIS_BUILD_DIR}/dependencies.sh"
elif test -f dependencies.sh; then
	. dependencies.sh
else
	echo "Can't find dependencies.sh" 1>&2
	exit 1
fi
ant \
	-Dlaunch4j.dir="$(pwd)/launch4j" \
	-Dpumpernickel.path="$(pwd)/Pumpernickel.jar" \
	-Dapple.extensions.path="$(pwd)/orange-extensions-${ORANGE_VERSION}.jar" \
	-Dceylon.home="$(pwd)/ceylon-${CEYLON_VERSION}" \
	test
