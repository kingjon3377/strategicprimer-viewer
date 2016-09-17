#!/bin/sh
# This is for use by Travis CI, to reduce too-long lines in .travis.yml
updatedb
locate hamcrest-core-1.3.jar
ant \
    -Dlaunch4j.dir=./launch4j \
    -Dnullness.jar.path=./org.eclipse.jdt.annotation-2.0.0.jar \
    -Djunit.jar.path=/usr/share/java/junit4.jar \
    -Dwindowmenu.jar.path=./WindowMenu.jar \
    -Dhamcrest.jar.path=/usr/share/java/hamcrest-core-1.3.jar \
    test
