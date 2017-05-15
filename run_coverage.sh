#!/bin/sh
# This is for use by Travis CI, to reduce too-long lines in .travis.yml
ant \
    -Dlaunch4j.dir=$(pwd)/launch4j \
    -Dwindowmenu.jar.path=$(pwd)/pump-swing-1.0.00.jar \
    -Dapple.extensions.path=$(pwd)/orange-extensions-1.3.0.jar \
    -Dceylon.home=$(pwd)/ceylon-1.3.2 \
    coverage
