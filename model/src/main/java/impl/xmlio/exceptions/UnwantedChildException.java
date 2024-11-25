package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;

import java.io.Serial;
import java.nio.file.Path;
import java.util.Collection;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.StartElement;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

/**
 * A custom exception for when a tag has a child tag it can't handle.
 */
public final class UnwantedChildException extends SPFormatException {
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * The current tag.
	 */
	private final QName tag;

	/**
	 * The unwanted child.
	 */
	private final QName child;

	/**
	 * The file in which this happens, if any.
	 */
	private final @Nullable Path file;

	/**
	 * The location in the XML where this happens, if any.
	 */
	private final @Nullable Location location;

	private UnwantedChildException(final QName parent, final QName child, final @Nullable Path path,
	                               final Location location, final Throwable cause) {
		super("Unexpected child %s in tag %s".formatted(child.getLocalPart(),
				parent.getLocalPart()), Pair.with(path, location));
		tag = parent;
		this.child = child;
		file = path;
		this.location = location;
	}

	/**
	 * For when the unwanted child isn't an unwanted *tag* at all. (The one current use of this is for arbitrary
	 * text outside a tile.)
	 *
	 * @param parent   the current tag
	 * @param child    the unwanted child
	 * @param location where this occurred
	 * @param cause    why this occurred
	 */
	public static UnwantedChildException childInTag(final QName parent, final @Nullable Path path, final QName child,
	                                                final Location location, final Throwable cause) {
		return new UnwantedChildException(parent, child, path, location, cause);
	}

	/**
	 * @param parent The current tag
	 * @param child  The unwanted child
	 * @param cause  Another exception that caused this one
	 */
	public UnwantedChildException(final QName parent, final StartElement child, final @Nullable Path path,
	                              final Throwable cause) {
		super("Unexpected child %s in tag %s".formatted(child.getName().getLocalPart(),
				parent.getLocalPart()), path, child.getLocation(), cause);
		tag = parent;
		this.child = child.getName();
		file = path;
		location = child.getLocation();
	}

	/**
	 * @param parent The current tag
	 * @param child  The unwanted child
	 */
	public UnwantedChildException(final QName parent, final StartElement child, final @Nullable Path path) {
		super("Unexpected child %s in tag %s".formatted(child.getName().getLocalPart(),
				parent.getLocalPart()), Pair.with(path, child.getLocation()));
		tag = parent;
		this.child = child.getName();
		file = path;
		location = child.getLocation();
	}

	/**
	 * Where the caller asserted that a tag was one of a specified list.
	 */
	private UnwantedChildException(final QName parent, final StartElement child, final @Nullable Path path,
	                               final String[] expected) {
		super("Unexpected child %s in tag %s, expecting one of the following: %s".formatted(
						child.getName().getLocalPart(), parent.getLocalPart(),
						String.join(", ", expected)),
				Pair.with(path, child.getLocation()));
		tag = parent;
		this.child = child.getName();
		file = path;
		location = child.getLocation();
	}

	/**
	 * Where the caller asserted that a tag was one of a specified list.
	 */
	private UnwantedChildException(final QName parent, final StartElement child, final @Nullable Path path,
	                               final Iterable<String> expected) {
		super("Unexpected child %s in tag %s, expecting one of the following: %s".formatted(
						child.getName().getLocalPart(), parent.getLocalPart(),
						String.join(", ", expected)),
				Pair.with(path, child.getLocation()));
		tag = parent;
		this.child = child.getName();
		file = path;
		location = child.getLocation();
	}

	/**
	 * Where the caller asserted that a tag was one of a specified list.
	 *
	 * @param parent   the current tag
	 * @param child    the unwanted child
	 * @param expected what could have appeared here without triggering the error
	 */
	public static UnwantedChildException listingExpectedTags(final QName parent, final StartElement child,
	                                                         final @Nullable Path path, final String... expected) {
		return new UnwantedChildException(parent, child, path, expected);
	}

	/**
	 * Where the caller asserted that a tag was one of a specified list.
	 *
	 * @param parent   the current tag
	 * @param child    the unwanted child
	 * @param expected what could have appeared here without triggering the error
	 */
	public static UnwantedChildException listingExpectedTags(final QName parent, final StartElement child,
	                                                         final @Nullable Path path,
	                                                         final Collection<String> expected) {
		return new UnwantedChildException(parent, child, path, expected);
	}

	/**
	 * When the problem is that the child is not of a recognized namespace.
	 *
	 * Note that to avoid illegal overloading of the same signature, since
	 * Java doesn't have named constructors, this has its signature backwards
	 *
	 * @param parent the current tag
	 * @param child  the unwanted child
	 */
	private UnwantedChildException(final StartElement child, final @Nullable Path path, final QName parent) {
		super("Unexpected child, from unknown namespace, %s:%s in tag %s".formatted(
						child.getName().getPrefix(), child.getName().getLocalPart(),
						parent.getLocalPart()), Pair.with(path, child.getLocation()));
		tag = parent;
		this.child = child.getName();
		file = path;
		location = child.getLocation();
	}

	/**
	 * When the problem is that the child is not of a recognized namespace.
	 *
	 * @param parent the current tag
	 * @param child  the unwanted child
	 */
	public static UnwantedChildException unexpectedNamespace(final QName parent, final @Nullable Path path,
	                                                         final StartElement child) {
		return new UnwantedChildException(child, path, parent);
	}

	/**
	 * When a child is unwanted for a reason that needs further explanation.
	 *
	 * @param parent  the current tag
	 * @param child   the unwanted child
	 * @param message the additional message
	 */
	public UnwantedChildException(final QName parent, final @Nullable Path path, final StartElement child,
	                              final String message) {
		super("Unexpected child %s in tag %s: %s".formatted(
				child.getName().getLocalPart(), parent.getLocalPart(), message), Pair.with(path, child.getLocation()));
		tag = parent;
		this.child = child.getName();
		file = path;
		location = child.getLocation();
	}


	public QName getChild() {
		return child;
	}

	public QName getTag() {
		return tag;
	}

	public @Nullable Location getLocation() {
		return location;
	}
}
