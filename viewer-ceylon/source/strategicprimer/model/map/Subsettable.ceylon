import lovelace.util.common {
    todo
}
"An interface to let us check converted player maps against the main map."
shared interface Subsettable<Element> given Element satisfies Object {
	"""Test whether an object is a "strict" subset of this one."""
	todo("Take a successively-wrapped Anything(String) instead of a Formatter and `context")
	shared formal Boolean isSubset(
			"The thing that might be a subset."
			Element obj,
			"How to report why we return false. The outermost caller will probably pass in
			  `process.writeLine`, but each recursive call will wrap this in a statement
			  of its own context."
			Anything(String) report);
}