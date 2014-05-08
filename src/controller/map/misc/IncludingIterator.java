package controller.map.misc;

import static controller.map.misc.FileOpener.createReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;
import util.Pair;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;

/**
 * An extension to the IteratorWrapper we previously used in MapReaderNG that
 * automatically handles "include" tags.
 *
 * @author Jonathan Lovelace
 *
 */
public class IncludingIterator implements Iterator<XMLEvent> {
	/**
	 * The stack of iterators we're working with.
	 */
	private final Deque<Pair<String, ComparableIterator<XMLEvent>>> stack;

	/**
	 * FIXME: We should use Files "all the way down" if we can. But we can't yet
	 * because we rely on the magic string: file.
	 *
	 * @param file
	 *            the name of the file we're reading
	 * @param iter
	 *            the iterator we'll start with.
	 */
	public IncludingIterator(final File file, final Iterator<XMLEvent> iter) {
		this(NullCleaner.assertNotNull(file.getPath()), iter);
	}
	/**
	 * Constructor.
	 *
	 * @param file the name of the file we're reading
	 * @param iter the iterator we'll start with.
	 */
	public IncludingIterator(final String file, final Iterator<XMLEvent> iter) {
		stack = new LinkedList<>();
		stack.addFirst(Pair.of(file, new ComparableIterator<>(iter)));
	}

	/**
	 * Note that this method removes any empty iterators from the top of the
	 * stack before returning.
	 *
	 * @return whether there are any events left.
	 */
	@Override
	public boolean hasNext() {
		removeEmptyIterators();
		return !stack.isEmpty();
	}

	/**
	 * Remove any empty iterators from the top of the stack.
	 */
	private void removeEmptyIterators() {
		while (!stack.isEmpty() && !stack.peekFirst().second().hasNext()) {
			stack.removeFirst();
		}
	}

	/**
	 * Return the next item in the topmost iterator. We always make sure that
	 * there *is* a next item in the topmost iterator. If the next item would be
	 * an "include" tag, we open the file it specifies and push an iterator of
	 * its elements onto the stack. On error in that process, we throw a
	 * NoSuchElementException, as that's the only thing we *can* throw other
	 * than unchecked exceptions.
	 *
	 * @return the next item in the topmost iterator.
	 */
	@Nullable
	@Override
	public XMLEvent next() {
		removeEmptyIterators();
		if (stack.isEmpty()) {
			throw new NoSuchElementException();
		}
		XMLEvent retval = stack.peekFirst().second().next();
		while (retval != null && retval.isStartElement()
				&& "include".equals(retval.asStartElement().getName()
						.getLocalPart())) {
			handleInclude(NullCleaner.assertNotNull(retval.asStartElement()));
			removeEmptyIterators();
			if (stack.isEmpty()) {
				throw new NoSuchElementException();
			}
			retval = stack.peekFirst().second().next();
		}
		return retval;
	}

	/**
	 * A NoSuchElementException that takes a custom cause, unlike its
	 * superclass.
	 *
	 * @author Jonathan Lovelace
	 */
	// ESCA-JAVA0051:
	public static class NoSuchElementBecauseException extends
			NoSuchElementException {
		/**
		 * Constructor.
		 *
		 * @param message the message
		 * @param cause the cause
		 */
		public NoSuchElementBecauseException(final String message,
				final Throwable cause) {
			super(message);
			super.initCause(cause);
		}

		/**
		 * Constructor.
		 *
		 * @param cause the cause
		 */
		public NoSuchElementBecauseException(final Throwable cause) {
			super();
			super.initCause(cause);
		}
	}

	/**
	 * Handle an "include" tag by adding an iterator for the contents of the
	 * file it references to the top of the stack.
	 *
	 * @param tag the tag.
	 */
	private void handleInclude(final StartElement tag) {
		try {
			final String file = getAttribute(tag, "file");
			stack.addFirst(Pair.of(file, new ComparableIterator<>(
					new TypesafeXMLEventReader(createReader(file)))));
		} catch (final FileNotFoundException e) {
			throw new NoSuchElementBecauseException(
					"File referenced by <include> not found", e);
		} catch (final XMLStreamException e) {
			throw new NoSuchElementBecauseException(
					"XML stream error parsing <include> tag or opening file", e);
		} catch (final SPFormatException e) {
			throw new NoSuchElementBecauseException(
					"SP format problem in <include>", e);
		}
	}

	/**
	 * Remove the next item from the topmost iterator on the stack; this method
	 * makes sure that no empty iterator is on the top of the stack both before
	 * and after doing so.
	 */
	@Override
	public void remove() {
		removeEmptyIterators();
		if (stack.isEmpty()) {
			throw new NoSuchElementException();
		}
		stack.peekFirst().second().remove();
		removeEmptyIterators();
	}

	/**
	 * @return the file we're *currently* reading from.
	 */
	public String getFile() {
		removeEmptyIterators();
		if (stack.isEmpty()) {
			throw new NoSuchElementException("We're not reading at all");
		}
		return stack.peekFirst().first();
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "IncludingIterator";
	}

	/**
	 * @param startElement a tag
	 * @param attribute the attribute we want
	 *
	 * @return the value of that attribute.
	 * @throws SPFormatException if the element doesn't have that attribute
	 */
	private static String getAttribute(final StartElement startElement,
			final String attribute) throws SPFormatException {
		final Attribute attr = startElement.getAttributeByName(new QName(
				attribute));
		if (attr == null) {
			throw new MissingPropertyException(NullCleaner.valueOrDefault(
					startElement.getName().getLocalPart(), "a null tag"),
					attribute, startElement.getLocation().getLineNumber());
		}
		final String value = attr.getValue();
		if (value == null) {
			throw new MissingPropertyException(NullCleaner.valueOrDefault(
					startElement.getName().getLocalPart(), "a null tag"),
					attribute, startElement.getLocation().getLineNumber());
		} else {
			return value;
		}
	}
}
