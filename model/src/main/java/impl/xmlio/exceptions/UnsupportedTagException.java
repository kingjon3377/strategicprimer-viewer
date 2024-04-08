package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import java.io.Serial;

/**
 * A custom exception for not-yet-supported tags.
 */
public final class UnsupportedTagException extends SPFormatException {
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * The unsupported tag.
	 */
	private final QName tag;

	private UnsupportedTagException(final String format, final StartElement tag) {
		super(format.formatted(tag.getName().getLocalPart()),
				tag.getLocation());
		this.tag = tag.getName();
	}

	public static UnsupportedTagException future(final StartElement unexpectedTag) {
		return new UnsupportedTagException("Unexpected tag %s; probably a more recent map format than we support",
				unexpectedTag);
	}

	public static UnsupportedTagException obsolete(final StartElement unexpectedTag) {
		return new UnsupportedTagException("No-longer-supported tag %s", unexpectedTag);
	}

	public QName getTag() {
		return tag;
	}
}
