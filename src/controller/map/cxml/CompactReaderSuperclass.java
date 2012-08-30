package controller.map.cxml;

import javax.xml.stream.events.StartElement;

import util.EqualsAny;

/**
 * A superclass to provide helper methods.
 * @author Jonathan Lovelace
 *
 */
public class CompactReaderSuperclass {
	/**
	 * Do not instantiate directly.
	 */
	protected CompactReaderSuperclass() {
		// Nothing to do
	}
	/**
	 * Require that an element be one of the specified tags.
	 * @param element the element to check
	 * @param tags the tags we accept here
	 */
	protected void requireTag(final StartElement element, final String... tags) {
		if (!(EqualsAny.equalsAny(element.getName().getLocalPart(), tags))) {
			final StringBuilder sbuild = new StringBuilder("Unexpected tag ");
			sbuild.append(element.getName().getLocalPart());
			sbuild.append(" on line ");
			sbuild.append(element.getLocation().getLineNumber());
			sbuild.append(", expected one of the following: ");
			for (String tag : tags) {
				sbuild.append(tag);
				sbuild.append(", ");
			}
			throw new IllegalArgumentException(sbuild.toString());
		}
	}
}
