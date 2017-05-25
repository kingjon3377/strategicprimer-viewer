#!/bin/sh
# This is for use by Travis CI, to reduce too-long lines in .travis.yml
ant \
    -lib /usr/share/java/ant-contrib.jar \
    -lib ./launch4j \
    -lib ./launch4j/lib \
    -Dlaunch4j.dir=./launch4j \
    -Dnullness.jar.path=./org.eclipse.jdt.annotation-2.0.0.jar \
    -Djunit.jar.path=/usr/share/java/junit4.jar \
    -Dwindowmenu.jar.path=./pump-swing-1.0.00.jar \
    -Dstub-script-path=./universalJavaApplicationStub-2.0.1/src/universalJavaApplicationStub \
    -Dapple.extensions.path=./orange-extensions-1.3.0.jar \
    release
