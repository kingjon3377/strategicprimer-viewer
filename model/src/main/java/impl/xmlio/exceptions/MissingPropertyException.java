package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;
import org.javatuples.Pair;
import org.jspecify.annotations.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import java.io.Serial;
import java.nio.file.Path;

/**
 * An exception for cases where a parameter is required (or, if this is merely logged, recommended) but missing.
 */
public final class MissingPropertyException extends SPFormatException {
	@Serial
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
	 * @param file    The file containing this tagg
	 * @param param   The missing parameter.
	 */
	public MissingPropertyException(final StartElement context, final @Nullable Path file, final String param) {
		super("Missing parameter %s in tag %s".formatted(param,
				context.getName().getLocalPart()), Pair.with(file, context.getLocation()));
		tag = context.getName();
		this.param = param;
	}

	/**
	 * @param context The current tag
	 * @param file    The file containing the tag.
	 * @param param   The missing parameter.
	 * @param cause   the underlying cause
	 */
	public MissingPropertyException(final StartElement context, final @Nullable Path file, final String param,
	                                final Throwable cause) {
		super("Missing parameter %s in tag %s".formatted(param,
				context.getName().getLocalPart()), file, context.getLocation(), cause);
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
