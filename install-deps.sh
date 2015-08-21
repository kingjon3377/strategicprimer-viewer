#!/bin/sh
set -ex
wget http://sourceforge.net/projects/launch4j/files/launch4j-3/3.8/launch4j-3.8-linux.tgz/download -O launch4j-3.8-linux.tgz
tar xzf launch4j-3.8-linux.tgz
wget https://github.com/tofi86/Jarbundler/releases/download/v2.4.0/jarbundler-2.4.0.tar.gz
tar xzf jarbundler-2.4.0.tar.gz
mkdir -p "${HOME}/.ant/lib"
mv -i jarbundler-2.4.0/jarbundler-2.4.0.jar "${HOME}/.ant/lib/"
