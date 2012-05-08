package controller.map.readerng;

import java.io.FileNotFoundException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import controller.map.SPFormatException;
import controller.map.misc.FileOpener;

/**
 * An extension to the IteratorWrapper we previously used in MapReaderNG that
 * automatically handles "include" tags. TODO: We need something to map tags to
 * the files they came from, so we can write them back properly. (For tile
 * submaps at <em>least</em>.)
 * 
 * @author Jonathan Lovelace
 * 
 */
public class IncludingIterator implements Iterator<XMLEvent> {
	/**
	 * Constructor.
	 * 
	 * @param iter
	 *            the iterator we'll start with.
	 */
	public IncludingIterator(final Iterator<XMLEvent> iter) {
		stack.addFirst(iter);
	}

	/**
	 * The stack of iterators we're working with.
	 */
	private final Deque<Iterator<XMLEvent>> stack = new LinkedList<Iterator<XMLEvent>>();

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
		while (!stack.isEmpty() && !stack.peekFirst().hasNext()) {
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
		XMLEvent retval = stack.peekFirst().next();
		while (retval.isStartElement()
				&& "include".equals(retval.asStartElement().getName()
						.getLocalPart())) {
			handleInclude(retval);
			removeEmptyIterators();
			if (stack.isEmpty()) {
				throw new NoSuchElementException();
			}
			retval = stack.peekFirst().next();
		}
		return retval;
	}

	/**
	 * Handle an "include" tag by adding an iterator for the contents of the
	 * file it references to the top of the stack.
	 * 
	 * @param tag
	 *            the tag.
	 */
	private void handleInclude(final XMLEvent tag) {
		try {
			stack.addFirst(XMLInputFactory.newInstance().createXMLEventReader(
					new FileOpener().createReader(XMLHelper.getAttribute(
							tag.asStartElement(), "file"))));
		} catch (final FileNotFoundException e) {
			throw new NoSuchElementException(// NOPMD
					"File referenced by <include> not found") {
				@Override
				public Throwable getCause() {
					return e;
				}
			};
		} catch (final XMLStreamException e) {
			throw new NoSuchElementException(// NOPMD
					"XML stream error parsing <include> tag or opening file") {
				@Override
				public Throwable getCause() {
					return e;
				}
			};
		} catch (final SPFormatException e) {
			throw new NoSuchElementException(// NOPMD
					"SP format problem in <include>") {
				@Override
				public Throwable getCause() {
					return e;
				}
			};
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
		stack.peekFirst().remove();
		removeEmptyIterators();
	}

}
