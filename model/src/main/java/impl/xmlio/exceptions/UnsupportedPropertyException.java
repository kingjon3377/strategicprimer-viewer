package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import java.io.Serial;

/**
 * A custom exception for cases where a tag has a property it doesn't support.
 */
public final class UnsupportedPropertyException extends SPFormatException {
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * The unsupported property.
	 */
	private final String param;

	/**
	 * The current tag.
	 */
	private final QName tag;

	public UnsupportedPropertyException(final StartElement context, final String param) {
		super("Unsupported property %s in tag %s".formatted(param,
				context.getName().getLocalPart()), context.getLocation());
		this.param = param;
		tag = context.getName();
	}

	private UnsupportedPropertyException(final StartElement tag, final String param, final String context) {
		super("Unsupported property %s in tag %s %s".formatted(param,
				tag.getName().getLocalPart(), context), tag.getLocation());
		this.tag = tag.getName();
		this.param = param;
	}

	/**
	 * A variation for when a property is *conditionally* supported.
	 */
	public static UnsupportedPropertyException inContext(final StartElement tag, final String param,
	                                                     final String context) {
		return new UnsupportedPropertyException(tag, param, context);
	}

	public String getParam() {
		return param;
	}

	public QName getTag() {
		return tag;
	}
}
