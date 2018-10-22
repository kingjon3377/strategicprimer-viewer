"""An interface for "proxy" implementations."""
shared interface ProxyFor<Type> /* satisfies T */ given Type satisfies Object {
    "Add another object to be proxied."
    shared formal void addProxied(Type item);

    "Get the proxied items. This should probably only ever be used in tests, or in proxies
     managing nexted proxies."
    shared formal {Type*} proxied;

    "If there is consensus among proxied items on the given property, return it;
     otherwise return null."
    shared default MemberType? getConsensus<MemberType>(MemberType(Type) accessor)
            given MemberType satisfies Object {
        variable MemberType? retval = null;
        for (item in proxied.map(accessor)) {
            if (exists temp = retval) {
                if (temp != item) {
                    return null;
                }
            } else {
                retval = item;
            }
        }
        return retval;
    }

    "Whether this should be considered (if true) a proxy for multiple representations of
     the same item (such as in different maps), or (if false) a proxy for multiple related
     items (such as all workers in a single unit)."
    shared formal Boolean parallel;
}
