package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

/**
 * An exception for cases where a parameter is required (or, if this is merely logged, recommended) but missing.
 */
public class MissingPropertyException extends SPFormatException {
	private static final long serialVersionUID = 1L;
	/**
	 * The current tag.
	 */
	private final QName tag;
	/**
	 * The missing parameter.
	 */
	private final String param;

	/**
	 * @param context The current tag
	 * @param param The missing parameter.
	 */
	public MissingPropertyException(final StartElement context, final String param) {
		super(String.format("Missing parameter %s in tag %s", param,
			context.getName().getLocalPart()), context.getLocation());
		tag = context.getName();
		this.param = param;
	}

	/**
	 * @param context The current tag
	 * @param param The missing parameter.
	 * @param cause the underlying cause
	 */
	public MissingPropertyException(final StartElement context, final String param, final Throwable cause) {
		super(String.format("Missing parameter %s in tag %s", param,
			context.getName().getLocalPart()), context.getLocation(), cause);
		tag = context.getName();
		this.param = param;
	}

	public QName getTag() {
		return tag;
	}

	public String getParam() {
		return param;
	}
}
