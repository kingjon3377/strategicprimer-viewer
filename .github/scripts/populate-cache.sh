#!/bin/sh
if curl -s --head --request GET \
		'https://modules.ceylon-lang.org/repo/1/ceylon/collection/1.3.3/ceylon.collection-1.3.3.car' | \
		grep -v -q "200 OK"; then
	# Herd is down
	echo "Cache URL we have is '${CEYLON_DEPS_CACHE}'"
	wget "${CEYLON_DEPS_CACHE}" -O ceylon-deps.tar.gz || exit 1
	tar xzf ceylon-deps.tar.gz
fi
