package controller.map.misc;

import static controller.map.readerng.XMLHelper.getAttribute;

import java.io.FileNotFoundException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import util.Pair;
import controller.map.SPFormatException;

/**
 * An extension to the IteratorWrapper we previously used in MapReaderNG that
 * automatically handles "include" tags. TODO: We need something to map tags to
 * the files they came from, so we can write them back properly.
 *
 * @author Jonathan Lovelace
 *
 */
public class IncludingIterator implements Iterator<XMLEvent> {
	/**
	 * Constructor.
	 *
	 * @param file the name of the file we're reading
	 * @param iter the iterator we'll start with.
	 */
	public IncludingIterator(final String file, final Iterator<XMLEvent> iter) {
		stack = new LinkedList<Pair<String, ComparableIterator<XMLEvent>>>();
		stack.addFirst(Pair.of(file, new ComparableIterator<XMLEvent>(iter)));
	}

	/**
	 * The stack of iterators we're working with.
	 */
	private final Deque<Pair<String, ComparableIterator<XMLEvent>>> stack;

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
	@Override
	public XMLEvent next() {
		removeEmptyIterators();
		if (stack.isEmpty()) {
			throw new NoSuchElementException();
		}
		XMLEvent retval = stack.peekFirst().second().next();
		while (retval.isStartElement()
				&& "include".equals(retval.asStartElement().getName()
						.getLocalPart())) {
			handleInclude(retval);
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
	private void handleInclude(final XMLEvent tag) {
		try {
			final String file = getAttribute(tag.asStartElement(), "file");
			stack.addFirst(Pair.of(
					file,
					new ComparableIterator<XMLEvent>(XMLInputFactory
							.newInstance().createXMLEventReader(
									new FileOpener().createReader(file)))));
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
}
