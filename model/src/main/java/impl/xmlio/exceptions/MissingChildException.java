package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import java.io.Serial;

/**
 * A custom exception for when a tag requires a child but the child is not present.
 */
public class MissingChildException extends SPFormatException {
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * The current tag.
	 */
	private final QName tag;

	public MissingChildException(final StartElement context) {
		super(String.format("Tag %s missing a child", context.getName().getLocalPart()),
				context.getLocation());
		tag = context.getName();
	}

	public QName getTag() {
		return tag;
	}
}
