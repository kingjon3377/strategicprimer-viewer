package controller.map.misc;

import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.ISPReader;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.EqualsAny;
import util.Pair;

import static util.NullCleaner.assertNotNull;

/**
 * An extension to the IteratorWrapper we previously used in MapReaderNG that
 * automatically handles "include" tags.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class IncludingIterator implements Iterator<@NonNull XMLEvent> {
	/**
	 * The attribute on include tags that tells what file to include.
	 */
	public static final String FILE_ATTR_NAME = "file";
	/**
	 * The stack of iterators we're working with.
	 */
	private final Deque<Pair<String, ComparableIterator<XMLEvent>>> stack;

	/**
	 * FIXME: We should use Paths "all the way down" if we can. But we can't yet because
	 * we rely on the magic string: file.
	 *
	 * @param file the name of the file we're reading
	 * @param iter the iterator we'll start with.
	 */
	public IncludingIterator(final Path file, final Iterator<XMLEvent> iter) {
		this(assertNotNull(file.toString()), iter);
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
	 * @param element an element
	 * @param names   names an attribute of it might be known by
	 * @return the first matching attribute, or null if none found
	 */
	@Nullable
	private static Attribute getAttribute(final StartElement element, final QName...
																			  names) {
		for (final QName name : names) {
			final Attribute retval = element.getAttributeByName(name);
			if ((retval != null) && (retval.getValue() != null)) {
				return retval;
			}
		}
		return null;
	}

	/**
	 * @param startElement a tag
	 * @return the value of the 'file' attribute.
	 * @throws SPFormatException if the element doesn't have that attribute
	 */
	private static String getFileAttribute(final StartElement startElement)
			throws SPFormatException {
		final Attribute attr =
				getAttribute(startElement, new QName(ISPReader.NAMESPACE,
															FILE_ATTR_NAME),
						new QName(FILE_ATTR_NAME));
		if (attr == null) {
			throw new MissingPropertyException(startElement, FILE_ATTR_NAME);
		} else {
			return assertNotNull(attr.getValue());
		}
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
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void removeEmptyIterators() {
		while (!stack.isEmpty() && !stack.getFirst().second().hasNext()) {
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
	@SuppressWarnings("NewExceptionWithoutArguments")
	@Override
	public XMLEvent next() {
		removeEmptyIterators();
		if (stack.isEmpty()) {
			throw new NoSuchElementException();
		}
		XMLEvent retval = stack.getFirst().second().next();
		while (retval.isStartElement()
					   && EqualsAny.equalsAny(
				assertNotNull(retval.asStartElement()
									  .getName().getNamespaceURI()),
				ISPReader.NAMESPACE, XMLConstants.NULL_NS_URI)
					   && "include".equals(
				retval.asStartElement().getName().getLocalPart())) {
			handleInclude(assertNotNull(retval.asStartElement()));
			removeEmptyIterators();
			if (stack.isEmpty()) {
				throw new NoSuchElementException();
			}
			retval = stack.getFirst().second().next();
		}
		return retval;
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
			// FIXME: The MagicReader here (and thus the file it opens!) get leaked!
			stack.addFirst(Pair.of(file,
					new ComparableIterator<>(new TypesafeXMLEventReader(new MagicReader(
																							   file)))));
		} catch (final FileNotFoundException e) {
			throw new NoSuchElementBecauseException("File referenced by <include> not " +
															"found",
														   e);
		} catch (final XMLStreamException e) {
			// TODO: Tests should handle include-non-XML case
			throw new NoSuchElementBecauseException("XML stream error parsing <include>" +
															" tag or opening file",
														   e);
		} catch (final SPFormatException e) {
			throw new NoSuchElementBecauseException("SP format problem in <include>", e);
		}
	}

	/**
	 * Remove the next item from the topmost iterator on the stack; this method makes
	 * sure
	 * that no empty iterator is on the top of the stack both before and after doing so.
	 */
	@SuppressWarnings("NewExceptionWithoutArguments")
	@Override
	public void remove() {
		removeEmptyIterators();
		if (stack.isEmpty()) {
			throw new NoSuchElementException();
		}
		stack.getFirst().second().remove();
		removeEmptyIterators();
	}

	/**
	 * TODO: Tests
	 *
	 * @return the file we're *currently* reading from.
	 */
	public String getFile() {
		removeEmptyIterators();
		if (stack.isEmpty()) {
			throw new NoSuchElementException("We're not reading at all");
		}
		return stack.getFirst().first();
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final Pair<String, ComparableIterator<XMLEvent>> top = stack.peekFirst();
		if (!stack.isEmpty() && (top != null)) {
			return "IncludingIterator, currently on " + top.first();
		} else {
			return "Empty IncludingIterator";
		}
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
}
