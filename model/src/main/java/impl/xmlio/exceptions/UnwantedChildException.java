package impl.xmlio.exceptions;

import common.xmlio.SPFormatException;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.StartElement;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A custom exception for when a tag has a child tag it can't handle.
 */
public class UnwantedChildException extends SPFormatException {
	/**
	 * The current tag.
	 */
	private final QName tag;

	/**
	 * The unwanted child.
	 */
	private final QName child;

	private UnwantedChildException(QName parent, QName child, Location location, Throwable cause) {
		super(String.format("Unexpected child %s in tag %s", child.getLocalPart(),
			parent.getLocalPart()), location.getLineNumber(), location.getColumnNumber());
		tag = parent;
		this.child = child;
	}

	/**
	 * For when the unwanted child isn't an unwanted *tag* at all. (The one current use of this is for arbitrary text outside a tile.)
	 * @param parent the current tag
	 * @param child the unwanted child
	 * @param location where this occurred
	 * @param cause why this occurred
	 */
	public static UnwantedChildException childInTag(QName parent, QName child, Location location, Throwable cause) {
		return new UnwantedChildException(parent, child, location, cause);
	}

	/**
	 * @param parent The current tag
	 * @param child The unwanted child
	 * @param cause Another exception that caused this one
	 */
	public UnwantedChildException(QName parent, StartElement child, Throwable cause) {
		super(String.format("Unexpected child %s in tag %s", child.getName().getLocalPart(),
			parent.getLocalPart()), child.getLocation().getLineNumber(),
			child.getLocation().getColumnNumber(), cause);
		tag = parent;
		this.child = child.getName();
	}

	/**
	 * @param parent The current tag
	 * @param child The unwanted child
	 */
	public UnwantedChildException(QName parent, StartElement child) {
		super(String.format("Unexpected child %s in tag %s", child.getName().getLocalPart(),
			parent.getLocalPart()), child.getLocation().getLineNumber(),
			child.getLocation().getColumnNumber());
		tag = parent;
		this.child = child.getName();
	}

	/**
	 * Copy-constructor-with-replacement, for cases where the original thrower didn't know the parent tag.
	 */
	public UnwantedChildException(QName parent, UnwantedChildException except) {
		super(String.format("Unexpected child %s in tag %s", except.getChild().getLocalPart(),
			parent.getLocalPart()), except.getLine(), except.getColumn());
		tag = parent;
		this.child = except.getChild();
	}

	/**
	 * Where the caller asserted that a tag was one of a specified list.
	 */
	private UnwantedChildException(QName parent, StartElement child, String[] expected) {
		super(String.format("Unexpected child %s in tag %s, expecting one of the following: %s",
			child.getName().getLocalPart(), parent.getLocalPart(),
			Stream.of(expected).collect(Collectors.joining(", "))),
			child.getLocation().getLineNumber(), child.getLocation().getColumnNumber());
		tag = parent;
		this.child = child.getName();
	}

	/**
	 * Where the caller asserted that a tag was one of a specified list.
	 * @param parent the current tag
	 * @param child the unwanted child
	 * @param expected what could have appeared here without triggering the error
	 */
	public static UnwantedChildException listingExpectedTags(QName parent, StartElement child, String... expected) {
		return new UnwantedChildException(parent, child, expected);
	}

	/**
	 * When the problem is that the child is not of a recognized namespace.
	 *
	 * Note that to avoid illegal overloading of the same signature, since
	 * Java doesn't have named constructors, this has its signature backwards
	 *
	 * @param parent the current tag
	 * @param child the unwanted child
	 */
	private UnwantedChildException(StartElement child, QName parent) {
		super(String.format("Unexpected child, from unknown namespace, %s:%s in tag %s",
				child.getName().getPrefix(), child.getName().getLocalPart(),
				parent.getLocalPart()),
			child.getLocation().getLineNumber(), child.getLocation().getColumnNumber());
		tag = parent;
		this.child = child.getName();
	}

	/**
	 * When the problem is that the child is not of a recognized namespace.
	 *
	 * @param parent the current tag
	 * @param child the unwanted child
	 */
	public static UnwantedChildException unexpectedNamespace(QName parent, StartElement child) {
		return new UnwantedChildException(child, parent);
	}

	/**
	 * When a child is unwanted for a reason that needs further explanation.
	 * @param parent the current tag
	 * @param child the unwanted child
	 * @param message the additional message
	 */
	public UnwantedChildException(QName parent, StartElement child, String message) {
		super(String.format("Unexpected child %s in tag %s: %s",
				child.getName().getLocalPart(), parent.getLocalPart(), message),
			child.getLocation().getLineNumber(), child.getLocation().getColumnNumber());
		tag = parent;
		this.child = child.getName();
	}

	public QName getChild() {
		return child;
	}

	public QName getTag() {
		return tag;
	}
}
