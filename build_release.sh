#!/bin/sh
# This is for use by CI, to build the artifacts and give them the names we want.
set -ex
mvn --batch-mode package
mkdir -p release
cd main/target
appname=*.app
for file in main-*.app main-*.exe main-*.jar; do
	mv "${file}" ../../release/viewer-"${file##main-}"
done
cd ../../release
appname="viewer-${appname##main-}"
tar cjf "${appname}.tbz2" "${appname}"
mkdir dmgtmp
mv "${appname}" dmgtmp/"${appname}"
cd dmgtmp
mkisofs -V "${appname}" -no-pad -r -hfs -o ../"${appname%%.app}.dmg" .
cd ..
rm -r dmgtmp
