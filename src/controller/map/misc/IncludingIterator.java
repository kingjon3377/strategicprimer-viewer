package controller.map.misc;

import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
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
import org.eclipse.jdt.annotation.NonNull;
import util.NullCleaner;
import util.Pair;

import static controller.map.misc.FileOpener.createReader;

/**
 * An extension to the IteratorWrapper we previously used in MapReaderNG that
 * automatically handles "include" tags.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class IncludingIterator implements Iterator<@NonNull XMLEvent> {
	/**
	 * The stack of iterators we're working with.
	 */
	private final Deque<Pair<String, ComparableIterator<XMLEvent>>> stack;

	/**
	 * FIXME: We should use Files "all the way down" if we can. But we can't yet because
	 * we rely on the magic string: file.
	 *
	 * @param file the name of the file we're reading
	 * @param iter the iterator we'll start with.
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
	 * Note that this method removes any empty iterators from the top of the stack before
	 * returning.
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
	 * Return the next item in the topmost iterator. We always make sure that there
	 * *is* a
	 * next item in the topmost iterator. If the next item would be an "include" tag, we
	 * open the file it specifies and push an iterator of its elements onto the stack. On
	 * error in that process, we throw a NoSuchElementException, as that's the only thing
	 * we *can* throw other than unchecked exceptions.
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
	 * A NoSuchElementException that takes a custom cause, unlike its superclass.
	 *
	 * @author Jonathan Lovelace
	 */
	public static final class NoSuchElementBecauseException extends
			NoSuchElementException {
		/**
		 * Constructor.
		 *
		 * @param message the message
		 * @param cause   the cause
		 */
		public NoSuchElementBecauseException(final String message,
											 final Throwable cause) {
			super(message);
			initCause(cause);
		}

		/**
		 * Constructor.
		 *
		 * @param cause the cause
		 */
		public NoSuchElementBecauseException(final Throwable cause) {
			initCause(cause);
		}
	}

	/**
	 * Handle an "include" tag by adding an iterator for the contents of the file it
	 * references to the top of the stack.
	 *
	 * @param tag the tag.
	 */
	private void handleInclude(final StartElement tag) {
		try {
			final String file = getFileAttribute(tag);
			stack.addFirst(Pair.of(file,
					new ComparableIterator<>(new TypesafeXMLEventReader(createReader(
							file)))));
		} catch (final FileNotFoundException e) {
			throw new NoSuchElementBecauseException(
														   "File referenced by <include>" +
																   " not found",
														   e);
		} catch (final XMLStreamException e) {
			throw new NoSuchElementBecauseException(
														   "XML stream error parsing " +
																   "<include> tag or " +
																   "opening file",
														   e);
		} catch (final SPFormatException e) {
			throw new NoSuchElementBecauseException(
														   "SP format problem in " +
																   "<include>",
														   e);
		}
	}

	/**
	 * Remove the next item from the topmost iterator on the stack; this method makes
	 * sure
	 * that no empty iterator is on the top of the stack both before and after doing so.
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
	 * TODO: show diagnostics
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "IncludingIterator";
	}

	/**
	 * @param startElement a tag
	 * @return the value of that attribute.
	 * @throws SPFormatException if the element doesn't have that attribute
	 */
	private static String getFileAttribute(final StartElement startElement)
			throws SPFormatException {
		final Attribute attr = startElement.getAttributeByName(new QName("file"));
		if (attr == null) {
			throw new MissingPropertyException(startElement.getName(), "file",
					                                  startElement.getLocation());
		}
		final String value = attr.getValue();
		if (value == null) {
			throw new MissingPropertyException(startElement.getName(), "file",
					                                  startElement.getLocation());
		} else {
			return value;
		}
	}
}
