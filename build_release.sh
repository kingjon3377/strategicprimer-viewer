#!/bin/bash
# This is for use by CI, to build the artifacts and give them the names we want.
set -ex
case "${GITHUB_REF}" in
refs/tags/v*) localrevision=( -Drevision="${GITHUB_REF##refs/tags/v}" ) ;;
refs/tags/*) localrevision=( -Drevision="${GITHUB_REF##refs/tags/}" ) ;;
*) localrevision=( ) ;;
esac
mvn --batch-mode package "${localrevision[@]}"
mkdir -p release
cd main/target
appnames=( *.app )
if "${#appnames}" -ne 1;then
	echo "Unexpected number of Mac apps" 1>&2
	exit 1
fi
appname="${appnames[0]}"
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
