package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

/**
 * A custom exception for cases where one property is deprecated in favor of another.
 */
public class DeprecatedPropertyException extends SPFormatException {
	/**
	 * The current tag.
	 */
	private final QName tag;
	/**
	 * The old form of the property.
	 */
	private final String old;
	/**
	 * The preferred form of the property.
	 */
	private final String preferred;

	public QName getTag() {
		return tag;
	}

	public String getOld() {
		return old;
	}

	public String getPreferred() {
		return preferred;
	}

	public DeprecatedPropertyException(StartElement context, String old, String preferred) {
		super(String.format("Use of the property %s in tag %s is deprecated, use %s instead", old,
				context.getName().getLocalPart(), preferred), context.getLocation().getLineNumber(),
			context.getLocation().getColumnNumber());
		this.tag = context.getName();
		this.old = old;
		this.preferred = preferred;
	}
}
