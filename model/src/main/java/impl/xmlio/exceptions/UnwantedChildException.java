package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;

import java.io.Serial;
import java.util.Collection;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.StartElement;

import org.jetbrains.annotations.Nullable;

/**
 * A custom exception for when a tag has a child tag it can't handle.
 */
public class UnwantedChildException extends SPFormatException {
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
	 * The location in the XML where this happens, if any.
	 */
	private final @Nullable Location location;

	private UnwantedChildException(final QName parent, final QName child, final Location location,
	                               final Throwable cause) {
		super("Unexpected child %s in tag %s".formatted(child.getLocalPart(),
				parent.getLocalPart()), location);
		tag = parent;
		this.child = child;
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
	public static UnwantedChildException childInTag(final QName parent, final QName child, final Location location,
	                                                final Throwable cause) {
		return new UnwantedChildException(parent, child, location, cause);
	}

	/**
	 * @param parent The current tag
	 * @param child  The unwanted child
	 * @param cause  Another exception that caused this one
	 */
	public UnwantedChildException(final QName parent, final StartElement child, final Throwable cause) {
		super("Unexpected child %s in tag %s".formatted(child.getName().getLocalPart(),
				parent.getLocalPart()), child.getLocation(), cause);
		tag = parent;
		this.child = child.getName();
		location = child.getLocation();
	}

	/**
	 * @param parent The current tag
	 * @param child  The unwanted child
	 */
	public UnwantedChildException(final QName parent, final StartElement child) {
		super("Unexpected child %s in tag %s".formatted(child.getName().getLocalPart(),
				parent.getLocalPart()), child.getLocation());
		tag = parent;
		this.child = child.getName();
		location = child.getLocation();
	}

	/**
	 * Copy-constructor-with-replacement, for cases where the original thrower didn't know the parent tag.
	 */
	public UnwantedChildException(final QName parent, final UnwantedChildException except) {
		super("Unexpected child %s in tag %s".formatted(except.getChild().getLocalPart(),
				parent.getLocalPart()), except.location);
		tag = parent;
		child = except.getChild();
		location = except.location;
	}

	/**
	 * Where the caller asserted that a tag was one of a specified list.
	 */
	private UnwantedChildException(final QName parent, final StartElement child, final String[] expected) {
		super("Unexpected child %s in tag %s, expecting one of the following: %s".formatted(
						child.getName().getLocalPart(), parent.getLocalPart(),
						String.join(", ", expected)),
				child.getLocation());
		tag = parent;
		this.child = child.getName();
		location = child.getLocation();
	}

	/**
	 * Where the caller asserted that a tag was one of a specified list.
	 */
	private UnwantedChildException(final QName parent, final StartElement child, final Collection<String> expected) {
		super("Unexpected child %s in tag %s, expecting one of the following: %s".formatted(
						child.getName().getLocalPart(), parent.getLocalPart(),
						String.join(", ", expected)),
				child.getLocation());
		tag = parent;
		this.child = child.getName();
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
	                                                         final String... expected) {
		return new UnwantedChildException(parent, child, expected);
	}

	/**
	 * Where the caller asserted that a tag was one of a specified list.
	 *
	 * @param parent   the current tag
	 * @param child    the unwanted child
	 * @param expected what could have appeared here without triggering the error
	 */
	public static UnwantedChildException listingExpectedTags(final QName parent, final StartElement child,
	                                                         final Collection<String> expected) {
		return new UnwantedChildException(parent, child, expected);
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
	private UnwantedChildException(final StartElement child, final QName parent) {
		super("Unexpected child, from unknown namespace, %s:%s in tag %s".formatted(
						child.getName().getPrefix(), child.getName().getLocalPart(),
						parent.getLocalPart()), child.getLocation());
		tag = parent;
		this.child = child.getName();
		location = child.getLocation();
	}

	/**
	 * When the problem is that the child is not of a recognized namespace.
	 *
	 * @param parent the current tag
	 * @param child  the unwanted child
	 */
	public static UnwantedChildException unexpectedNamespace(final QName parent, final StartElement child) {
		return new UnwantedChildException(child, parent);
	}

	/**
	 * When a child is unwanted for a reason that needs further explanation.
	 *
	 * @param parent  the current tag
	 * @param child   the unwanted child
	 * @param message the additional message
	 */
	public UnwantedChildException(final QName parent, final StartElement child, final String message) {
		super("Unexpected child %s in tag %s: %s".formatted(
						child.getName().getLocalPart(), parent.getLocalPart(), message), child.getLocation());
		tag = parent;
		this.child = child.getName();
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
