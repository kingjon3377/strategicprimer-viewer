package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

/**
 * An exception for cases where a parameter is required (or, if this is merely logged, recommended) but missing.
 */
public class MissingPropertyException extends SPFormatException {
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
	public MissingPropertyException(StartElement context, String param) {
		super(String.format("Missing parameter %s in tag %s", param,
			context.getName().getLocalPart()), context.getLocation().getLineNumber(),
			context.getLocation().getColumnNumber());
		tag = context.getName();
		this.param = param;
	}

	/**
	 * @param context The current tag
	 * @param param The missing parameter.
	 * @param cause the underlying cause
	 */
	public MissingPropertyException(StartElement context, String param, Throwable cause) {
		super(String.format("Missing parameter %s in tag %s", param,
			context.getName().getLocalPart()), context.getLocation().getLineNumber(),
			context.getLocation().getColumnNumber(), cause);
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
