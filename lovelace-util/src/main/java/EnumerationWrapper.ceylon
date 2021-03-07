import java.util {
    Enumeration
}

"A wrapper around [[Enumeration]] where callers want a Ceylon [[Iterable]]. In practice
 the APIs that use Enumeration rather than [[java.lang::Iterable]] don't parameterize it,
 so we assert that each item returned is of the desired type instead of requiring callers
 to coerce the type of the enumeration to be parameterized properly."
see(`class ConvertingIterable`)
shared class EnumerationWrapper<Element>(Enumeration<out Object> enumeration)
        satisfies Iterator<Element> {
    shared actual Element|Finished next() {
        if (enumeration.hasMoreElements()) {
            assert (is Element item = enumeration.nextElement());
            return item;
        } else {
            return finished;
        }
    }
}
