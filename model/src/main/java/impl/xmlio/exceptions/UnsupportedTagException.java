package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

/**
 * A custom exception for not-yet-supported tags.
 */
public class UnsupportedTagException extends SPFormatException {
	/**
	 * The unsupported tag.
	 */
	private final QName tag;

	private UnsupportedTagException(String format, StartElement tag) {
		super(String.format(format, tag.getName().getLocalPart()),
			tag.getLocation().getLineNumber(), tag.getLocation().getColumnNumber());
		this.tag = tag.getName();
	}

	public static UnsupportedTagException future(StartElement unexpectedTag) {
		return new UnsupportedTagException("Unexpected tag %s; probably a more recent map format than we support",
			unexpectedTag);
	}

	public static UnsupportedTagException obsolete(StartElement unexpectedTag) {
		return new UnsupportedTagException("No-longer-supported tag %s", unexpectedTag);
	}

	public QName getTag() {
		return tag;
	}
}
