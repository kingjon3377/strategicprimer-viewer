import ceylon.collection {
    MutableSet,
    HashSet
}
import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    ClassOrInterface
}
{ClassOrInterface<Anything>*} typeStream(Object obj) {
    MutableSet<ClassOrInterface<Anything>> classes =
            HashSet<ClassOrInterface<Anything>>();
    void impl(ClassOrInterface<Anything> current) {
        classes.add(current);
        if (exists superclass = current.extendedType) {
            impl(superclass);
        }
        for (superclass in current.satisfiedTypes) {
            impl(superclass);
        }
    }
    impl(type(obj));
    return {*classes};
}
