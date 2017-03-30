import lovelace.util.common {
    todo
}
import java.util {
    Formatter
}
"An interface to let us check converted player maps against the main map."
shared interface Subsettable<Element> given Element satisfies Object {
	"""Test whether an object is a "strict" subset of this one."""
	todo("Take a successively-wrapped Anything(String) instead of a Formatter and `context")
	shared formal Boolean isSubset(Element obj, Formatter ostream, String context);
}