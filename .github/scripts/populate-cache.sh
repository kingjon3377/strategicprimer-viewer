#!/bin/sh
if curl -s --head --request GET \
		'https://modules.ceylon-lang.org/repo/1/ceylon/collection/1.3.3/ceylon.collection-1.3.3.car' | \
		grep -v -q "200 OK"; then
	# Herd is down
	pip install gdown
	echo "Cache URL we have is '${CEYLON_DEPS_CACHE_URL}'"
	if echo "${CEYLON_DEPS_CACHE_URL}" | grep -q '^https://drive.google.com/file/d/';then
		CEYLON_DEPS_CACHE_URL=$(echo "${CEYLON_DEPS_CACHE_URL}" | sed 's@file/d/\([^/]*\)/view$@uc?id=\1@')
	fi
	gdown "${CEYLON_DEPS_CACHE_URL}" || exit 1
	tar xzf ceylon-deps.tar.gz
	cp -r .ceylon/cache .ceylon/repo
fi
