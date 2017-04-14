#!/bin/sh
# This is for use by Travis CI, to reduce too-long lines in .travis.yml
ant \
    -lib /usr/share/java/ant-contrib.jar \
    -lib $(pwd)/launch4j \
    -lib $(pwd)/launch4j/lib \
    -Dlaunch4j.dir=$(pwd)/launch4j \
    -Dnullness.jar.path=$(pwd)/org.eclipse.jdt.annotation-2.0.0.jar \
    -Dwindowmenu.jar.path=$(pwd)/WindowMenu.jar \
    -Dstub-script-path=$(pwd)/universalJavaApplicationStub-2.0.1/src/universalJavaApplicationStub \
    -Dapple.extensions.path=$(pwd)/orange-extensions-1.3.0.jar \
    -Dceylon.home=$(pwd)/ceylon-1.3.2 \
    release
