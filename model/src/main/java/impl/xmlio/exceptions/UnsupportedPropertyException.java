package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

/**
 * A custom exception for cases where a tag has a property it doesn't support.
 */
public class UnsupportedPropertyException extends SPFormatException {
	/**
	 * The unsupported property.
	 */
	private final String param;

	/**
	 * The current tag.
	 */
	private final QName tag;

	public UnsupportedPropertyException(StartElement context, String param) {
		super(String.format("Unsupported property %s in tag %s", param,
			context.getName().getLocalPart()), context.getLocation().getLineNumber(),
			context.getLocation().getColumnNumber());
		this.param = param;
		tag = context.getName();
	}

	private UnsupportedPropertyException(StartElement tag, String param, String context) {
		super(String.format("Unsupported property %s in tag %s %s", param,
			tag.getName().getLocalPart(), context), tag.getLocation().getLineNumber(),
			tag.getLocation().getColumnNumber());
		this.tag = tag.getName();
		this.param = param;
	}

	/**
	 * A variation for when a property is *conditionally* supported.
	 */
	public static UnsupportedPropertyException inContext(StartElement tag, String param, String context) {
		return new UnsupportedPropertyException(tag, param, context);
	}

	public String getParam() {
		return param;
	}

	public QName getTag() {
		return tag;
	}
}