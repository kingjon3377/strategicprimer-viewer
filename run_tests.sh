#!/bin/sh
# This is for use by Travis CI, to reduce too-long lines in .travis.yml
ant \
    -Dlaunch4j.dir=$(pwd)/launch4j \
    -Dnullness.jar.path=$(pwd)/org.eclipse.jdt.annotation-2.0.0.jar \
    -Djunit.jar.path=/usr/share/java/junit4.jar \
    -Dwindowmenu.jar.path=$(pwd)/WindowMenu.jar \
    -Dhamcrest.jar.path=/usr/share/java/hamcrest-core-1.3.jar \
    -Dapple.extensions.path=$(pwd)/orange-extensions-1.3.0.jar \
    -Dceylon.home=$(pwd)/ceylon-1.3.2 \
    test-ceylon
