package legacy.map;

import java.util.function.Consumer;

/**
 * An interface to let us check converted player maps against the main map.
 */
public interface Subsettable<Element> {
	/**
	 * Test whether an object is a "strict" subset of this one.
	 *
	 * @param obj    The thing that might be a subset.
	 * @param report How to report why we return false. The outermost
	 *               caller will probably pass in <pre>System.out.println</pre>, but each
	 *               recursive call will wrap this in a statement of its own context.
	 */
	boolean isSubset(Element obj, Consumer<String> report);
}
