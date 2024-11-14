package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import java.io.Serial;
import java.nio.file.Path;

/**
 * A custom exception for cases where one property is deprecated in favor of another.
 */
public final class DeprecatedPropertyException extends SPFormatException {
	@Serial
	private static final long serialVersionUID = 1L;
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

	public DeprecatedPropertyException(final StartElement context, final @Nullable Path path, final String old,
	                                   final String preferred) {
		super("Use of the property %s in tag %s is deprecated, use %s instead".formatted(old,
				context.getName().getLocalPart(), preferred), Pair.with(path, context.getLocation()));
		tag = context.getName();
		this.old = old;
		this.preferred = preferred;
	}

}
