#!/bin/sh
# This is for use by Travis CI, to reduce too-long lines in .travis.yml
ant \
    -Dlaunch4j.dir=./launch4j \
    -Dnullness.jar.path=./org.eclipse.jdt.annotation-2.0.0.jar \
    -Djunit.jar.path=/usr/share/java/junit4.jar \
    -Dwindowmenu.jar.path=./WindowMenu.jar \
    -Dhamcrest.jar.path=/usr/share/java/hamcrest-core-1.3.jar \
    -Dapple.extensions.path=./orange-extensions-1.3.0.jar \
    -Dceylon.home=./ceylon-1.3.2 \
    test-ceylon
