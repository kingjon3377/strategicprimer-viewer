package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import java.io.Serial;
import java.nio.file.Path;

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


	private UnsupportedTagException(final String format, final @Nullable Path file, final StartElement tag) {
		super(format.formatted(tag.getName().getLocalPart()),
				Pair.with(file, tag.getLocation()));
		this.tag = tag.getName();
	}

	public static UnsupportedTagException future(final StartElement unexpectedTag, final @Nullable Path file) {
		return new UnsupportedTagException("Unexpected tag %s; probably a more recent map format than we support",
				file, unexpectedTag);
	}

	public static UnsupportedTagException obsolete(final StartElement unexpectedTag, final @Nullable Path file) {
		return new UnsupportedTagException("No-longer-supported tag %s", file, unexpectedTag);
	}

	public QName getTag() {
		return tag;
	}
}
