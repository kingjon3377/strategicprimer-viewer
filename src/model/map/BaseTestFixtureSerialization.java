package model.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import util.FatalWarningException;
import util.Warning;
import controller.map.DeprecatedPropertyException;
import controller.map.IMapReader;
import controller.map.ISPReader;
import controller.map.MapVersionException;
import controller.map.MissingChildException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.TestReaderFactory;
import controller.map.UnsupportedTagException;
import controller.map.UnwantedChildException;
import controller.map.cxml.CompactXMLWriter;
import controller.map.readerng.ReaderAdapter;

// ESCA-JAVA0011:
/**
 * An abstract base class for this helper method.
 *
 * @author Jonathan Lovelace
 *
 */
public abstract class BaseTestFixtureSerialization { // NOPMD
	/**
	 * The "filename" to pass to the readers.
	 */
	protected static final String FAKE_FILENAME = "string";
	/**
	 * An instance of the previous-generation reader to test against.
	 */
	private final ISPReader oldReader = TestReaderFactory.createOldReader();
	/**
	 * An instance of the current-generation reader to test against.
	 */
	private final ISPReader newReader = TestReaderFactory.createNewReader();

	/**
	 * Assert that reading the given XML will produce an UnwantedChildException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but fail with warnings made fatal. This version runs
	 * against both the old and the new reader.
	 *
	 * @param xml the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param warning whether this is supposed to be a warning only
	 * @throws SPFormatException on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	public void assertUnwantedChild(final String xml,
			final Class<? extends XMLWritable> desideratum, final boolean warning)
			throws XMLStreamException, SPFormatException {
		assertUnwantedChild(oldReader, xml, desideratum, warning);
		assertUnwantedChild(newReader, xml, desideratum, warning);
	}

	/**
	 * Assert that reading the given XML will produce an
	 * UnsupportedTagException. If it's only supposed to be a warning, assert
	 * that it'll pass with warnings disabled but fail with warnings made fatal.
	 * This version uses both old and new readers.
	 *
	 * @param xml the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param tag the unsupported tag
	 * @param warning whether this is supposed to be a warning only
	 * @throws SPFormatException on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	public void assertUnsupportedTag(final String xml,
			final Class<? extends XMLWritable> desideratum, final String tag, final boolean warning)
			throws XMLStreamException, SPFormatException {
		assertUnsupportedTag(oldReader, xml, desideratum, tag, warning);
		assertUnsupportedTag(newReader, xml, desideratum, tag, warning);
	}

	/**
	 * Assert that reading the given XML will produce an
	 * UnsupportedTagException. If it's only supposed to be a warning, assert
	 * that it'll pass with warnings disabled but fail with warnings made fatal.
	 *
	 * @param reader the reader to do the reading
	 * @param xml the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param tag the unsupported tag
	 * @param warning whether this is supposed to be a warning only
	 * @throws SPFormatException on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	private static void assertUnsupportedTag(final ISPReader reader,
			final String xml, final Class<? extends XMLWritable> desideratum, final String tag,
			final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum,
					new Warning(Warning.Action.Ignore));
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Warning.Action.Die));
			} catch (final FatalWarningException except) {
				final Throwable cause = except.getCause();
				assertTrue("Unsupported tag",
						cause instanceof UnsupportedTagException);
				if (cause instanceof UnsupportedTagException) {
					assertEquals("The tag we expected", tag,
							((UnsupportedTagException) cause).getTag());
				} else {
					throw new IllegalStateException("Can't get here");
				}
			}
		} else {
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(
								Warning.Action.Ignore));
				fail("Expected an UnsupportedTagException");
			} catch (final UnsupportedTagException except) {
				assertEquals("The tag we expected", tag, except.getTag());
			}
		}
	}

	/**
	 * Assert that reading the given XML will produce an UnwantedChildException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but fail with warnings made fatal.
	 *
	 * @param reader the reader to do the reading
	 * @param xml the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param warning whether this is supposed to be a warning only
	 * @throws SPFormatException on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	private static void assertUnwantedChild(final ISPReader reader,
			final String xml, final Class<? extends XMLWritable> desideratum,
			final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum,
					new Warning(Warning.Action.Ignore));
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Warning.Action.Die));
				fail("We were expecting an UnwantedChildException");
			} catch (final FatalWarningException except) {
				assertTrue("Unwanted child",
						except.getCause() instanceof UnwantedChildException);
			}
		} else {
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(
								Warning.Action.Ignore));
				fail("We were expecting an UnwantedChildException");
			} catch (final UnwantedChildException except) {
				assertNotNull("Dummy check", except);
			}
		}
	}

	/**
	 * Assert that reading the given XML will give a MissingPropertyException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but object with them made fatal. This version tests
	 * both old and new readers.
	 *
	 * @param xml the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param property the missing property
	 * @param warning whether this is supposed to be only a warning
	 * @throws SPFormatException on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	public void assertMissingProperty(final String xml,
			final Class<? extends XMLWritable> desideratum, final String property,
			final boolean warning) throws XMLStreamException, SPFormatException {
		assertMissingProperty(oldReader, xml, desideratum, property, warning);
		assertMissingProperty(newReader, xml, desideratum, property, warning);
	}

	/**
	 * Assert that reading the given XML will give a MissingPropertyException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but object with them made fatal.
	 *
	 * @param reader the reader to do the reading
	 * @param xml the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param property the missing property
	 * @param warning whether this is supposed to be only a warning
	 * @throws SPFormatException on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	private static void assertMissingProperty(final ISPReader reader,
			final String xml, final Class<? extends XMLWritable> desideratum,
			final String property, final boolean warning) throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum,
					new Warning(Warning.Action.Ignore));
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Warning.Action.Die));
				fail("We were expecting a MissingParameterException");
			} catch (final FatalWarningException except) {
				final Throwable cause = except.getCause();
				assertTrue("Missing property",
						cause instanceof MissingParameterException);
				if (cause instanceof MissingParameterException) {
					assertEquals(
							"The missing property should be the one we're expecting",
							property,
							((MissingParameterException) cause).getParam());
				} else {
					throw new IllegalStateException("Can't get here");
				}
			}
		} else {
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(
								Warning.Action.Ignore));
			} catch (final MissingParameterException except) {
				assertEquals(
						"Missing property should be the one we're expecting",
						property, except.getParam());
			}
		}
	}

	/**
	 * Assert that reading the given XML will give a
	 * DeprecatedPropertyException. If it's only supposed to be a warning,
	 * assert that it'll pass with warnings disabled but object with them made
	 * fatal. This version tests both old and new readers.
	 *
	 * @param xml the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param deprecated the deprecated property
	 * @param warning whether this is supposed to be only a warning
	 * @throws SPFormatException on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	public void assertDeprecatedProperty(final String xml,
			final Class<? extends XMLWritable> desideratum, final String deprecated,
			final boolean warning) throws XMLStreamException, SPFormatException {
		assertDeprecatedProperty(oldReader, xml, desideratum, deprecated,
				warning);
		assertDeprecatedProperty(newReader, xml, desideratum, deprecated,
				warning);
	}

	/**
	 * Assert that reading the given XML will give a
	 * DeprecatedPropertyException. If it's only supposed to be a warning,
	 * assert that it'll pass with warnings disabled but object with them made
	 * fatal.
	 *
	 * @param reader the reader to do the reading
	 * @param xml the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param deprecated the deprecated property
	 * @param warning whether this is supposed to be only a warning
	 * @throws SPFormatException on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	private static void assertDeprecatedProperty(final ISPReader reader,
			final String xml, final Class<? extends XMLWritable> desideratum,
			final String deprecated, final boolean warning) throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum,
					new Warning(Warning.Action.Ignore));
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Warning.Action.Die));
				fail("We were expecting a MissingParameterException");
			} catch (final FatalWarningException except) {
				final Throwable cause = except.getCause();
				assertTrue(
						"Missing property",
						cause instanceof DeprecatedPropertyException);
				if (cause instanceof DeprecatedPropertyException) {
					assertEquals(
							"The missing property should be the one we're expecting",
							deprecated,
							((DeprecatedPropertyException) cause).getOld());
				} else {
					throw new IllegalStateException("Can't get here");
				}
			}
		} else {
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(
								Warning.Action.Ignore));
			} catch (final DeprecatedPropertyException except) {
				assertEquals(
						"Missing property should be the one we're expecting",
						deprecated, except.getOld());
			}
		}
	}

	/**
	 * Assert that the serialized form of the given object will deserialize
	 * without error using both old and new readers.
	 *
	 * @param <T> the type of the object
	 * @param message the message to use
	 * @param obj the object to serialize
	 * @param type its type
	 * @throws SPFormatException on SP XML problem
	 * @throws XMLStreamException on XML reading problem
	 * @throws IOException on I/O error creating serialized form
	 */
	public <T extends XMLWritable> void assertSerialization(
			final String message, final T obj, final Class<T> type)
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization(message, obj, type, new Warning(Warning.Action.Die));
	}

	/**
	 * Assert that the serialized form of the given object will deserialize
	 * without error using both the old and new readers.
	 *
	 * @param <T> the type of the object
	 * @param message the message to use
	 * @param obj the object to serialize
	 * @param type its type
	 * @param warning the warning instance to use
	 * @throws SPFormatException on SP XML problem
	 * @throws XMLStreamException on XML reading problem
	 * @throws IOException on I/O error creating serialized form
	 */
	public <T extends XMLWritable> void assertSerialization(
			final String message, final T obj, final Class<T> type,
			final Warning warning) throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization(message, oldReader, obj, type, warning);
		assertSerialization(message, newReader, obj, type, warning);
	}

	/**
	 * Assert that the serialized form of the given object will deserialize
	 * without error using both the reflection and non-reflection methods.
	 *
	 * @param <T> the type of the object
	 * @param message the message to use
	 * @param reader the reader to parse the serialized form
	 * @param obj the object to serialize
	 * @param type its type
	 * @param warner the warning instance to use
	 * @throws SPFormatException on SP XML problem
	 * @throws XMLStreamException on XML reading problem
	 * @throws IOException on I/O error creating serialized form
	 */
	private static <T extends XMLWritable> void assertSerialization(
			final String message, final ISPReader reader, final T obj,
			final Class<T> type, final Warning warner)
			throws XMLStreamException, SPFormatException, IOException {
		assertEquals(message, obj, reader.readXML(FAKE_FILENAME,
				new StringReader(createSerializedForm(obj, true)), type, warner));
		assertEquals(message, obj, reader.readXML(FAKE_FILENAME,
				new StringReader(createSerializedForm(obj, true)), type, warner));
		assertEquals(message, obj, reader.readXML(FAKE_FILENAME,
				new StringReader(createSerializedForm(obj, false)), type,
				warner));
		assertEquals(message, obj, reader.readXML(FAKE_FILENAME,
				new StringReader(createSerializedForm(obj, false)), type, warner));
	}

	/**
	 * Assert that a deprecated idiom deserializes properly if warnings are
	 * ignored, but is warned about.
	 *
	 * @param <T> the type
	 * @param message the message to pass to JUnit
	 * @param expected the object we expect the deserialized form to equal
	 * @param xml the serialized form
	 * @param type the type of object
	 * @param property the deprecated property
	 * @throws SPFormatException on SP format error
	 * @throws XMLStreamException on XML format error
	 */
	public <T extends XMLWritable> void assertDeprecatedDeserialization(
			final String message, final T expected, final String xml,
			final Class<T> type, final String property)
			throws XMLStreamException, SPFormatException {
		assertEquals(message, expected, oldReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(
						Warning.Action.Ignore)));
		assertEquals(message, expected, oldReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(
						Warning.Action.Ignore)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(
						Warning.Action.Ignore)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(
						Warning.Action.Ignore)));
		assertDeprecatedProperty(xml, type, property, true);
	}

	/**
	 * Assert that a serialized form with a recommended but not required
	 * property missing deserializes properly if warnings are ignored, but is
	 * warned about.
	 *
	 * @param <T> the type
	 * @param message the message to pass to JUnit
	 * @param expected the object we expect the deserialized form to equal
	 * @param xml the serialized form
	 * @param type the type of object
	 * @param property the missing property
	 * @throws SPFormatException on SP format error
	 * @throws XMLStreamException on XML format error
	 */
	public <T extends XMLWritable> void assertMissingPropertyDeserialization(
			final String message, final T expected, final String xml,
			final Class<T> type, final String property)
			throws XMLStreamException, SPFormatException {
		assertEquals(message, expected, oldReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(
						Warning.Action.Ignore)));
		assertEquals(message, expected, oldReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(
						Warning.Action.Ignore)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(
						Warning.Action.Ignore)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(
						Warning.Action.Ignore)));
		assertMissingProperty(xml, type, property, true);
	}

	/**
	 * Assert that a "forward idiom"---an idiom that we do not yet produce, but
	 * want to accept---will be deserialized properly by both readers, both with
	 * and without reflection.
	 *
	 * @param <T> the type
	 * @param message the message to pass to JUnit
	 * @param expected the object we expect the deserialized form to equal
	 * @param xml the serialized form
	 * @param type the type of object
	 * @throws SPFormatException on SP format error
	 * @throws XMLStreamException on XML format error
	 */
	public <T extends XMLWritable> void assertForwardDeserialization(
			final String message, final T expected, final String xml,
			final Class<T> type) throws XMLStreamException, SPFormatException {
		assertEquals(message, expected, oldReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(
						Warning.Action.Die)));
		assertEquals(message, expected, oldReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(
						Warning.Action.Die)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(
						Warning.Action.Die)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(
						Warning.Action.Die)));
	}

	/**
	 * Assert that two deserialzed forms are equivalent, using both readers,
	 * both with and without reflection.
	 *
	 * @param <T> the type they'll deserialize to.
	 * @param message the message to pass to JUnit
	 * @param one the first form
	 * @param two the second form
	 * @param type the type
	 * @param warningLevel the warning level to set.
	 * @throws SPFormatException on SP format error
	 * @throws XMLStreamException on XML format error
	 */
	public <T extends XMLWritable> void assertEquivalentForms(
			final String message, final String one, final String two,
			final Class<T> type, final Warning.Action warningLevel)
			throws SPFormatException, XMLStreamException {
		assertEquals(message, oldReader.readXML(FAKE_FILENAME,
				new StringReader(one), type, new Warning(warningLevel)),
				oldReader.readXML(FAKE_FILENAME, new StringReader(two), type,
						new Warning(warningLevel)));
		assertEquals(message, oldReader.readXML(FAKE_FILENAME,
				new StringReader(one), type, new Warning(warningLevel)),
				oldReader.readXML(FAKE_FILENAME, new StringReader(two), type,
						new Warning(warningLevel)));
		assertEquals(message, newReader.readXML(FAKE_FILENAME,
				new StringReader(one), type, new Warning(warningLevel)),
				newReader.readXML(FAKE_FILENAME, new StringReader(two), type,
						new Warning(warningLevel)));
		assertEquals(message, newReader.readXML(FAKE_FILENAME,
				new StringReader(one), type, new Warning(warningLevel)),
				newReader.readXML(FAKE_FILENAME, new StringReader(two), type,
						new Warning(warningLevel)));
	}

	/**
	 * @param obj an object
	 * @param deprecated whether to use the deprecated XML-serialization idiom
	 * @return its serialized form
	 * @throws IOException on I/O error creating it
	 */
	@SuppressWarnings("deprecation")
	public static String createSerializedForm(final XMLWritable obj,
			final boolean deprecated) throws IOException {
		final StringWriter writer = new StringWriter();
		if (deprecated) {
			ReaderAdapter.ADAPTER.write(obj).write(writer, true, 0);
		} else {
			new CompactXMLWriter().writeObject(writer, obj, true);
		}
		return writer.toString();
	}

	/**
	 * @param <T> the type of the object
	 * @param obj an object
	 * @return it, with its file set to "string"
	 */
	public static <T extends XMLWritable> T setFileOnObject(final T obj) {
		obj.setFile(FAKE_FILENAME);
		return obj;
	}

	/**
	 * Assert that reading the given XML will give a MissingChildException. If
	 * it's only supposed to be a warning, assert that it'll pass with warnings
	 * disabled but object with them made fatal. This version tests both old and
	 * new readers.
	 *
	 * @param xml the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param warning whether this is supposed to be only a warning
	 * @throws SPFormatException on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	public void assertMissingChild(final String xml,
			final Class<? extends XMLWritable> desideratum, final boolean warning)
			throws XMLStreamException, SPFormatException {
		assertMissingChild(oldReader, xml, desideratum, warning);
		assertMissingChild(newReader, xml, desideratum, warning);
	}

	/**
	 * Assert that reading the given XML will give a MissingChildException. If
	 * it's only supposed to be a warning, assert that it'll pass with warnings
	 * disabled but object with them made fatal.
	 *
	 * @param reader the reader to do the reading
	 * @param xml the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param warning whether this is supposed to be only a warning
	 * @throws SPFormatException on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	private static void assertMissingChild(final ISPReader reader,
			final String xml, final Class<? extends XMLWritable> desideratum,
			final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum,
					new Warning(Warning.Action.Ignore));
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Warning.Action.Die));
				fail("We were expecting a MissingChildException");
			} catch (final FatalWarningException except) {
				assertTrue("Missing child",
						except.getCause() instanceof MissingChildException);
			}
		} else {
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(
								Warning.Action.Ignore));
				fail("We were expecting a MissingChildException");
			} catch (final MissingChildException except) { // $codepro.audit.disable logExceptions
				assertTrue("Got the expected MissingChildException", true);
			}
		}
	}

	/**
	 * Assert that a map is properly deserialized (by the main
	 * map-deserialization methods) into a view.
	 *
	 * @param message the message to use in JUnit calls
	 * @param expected the object to test against
	 * @param xml the XML to deserialize it into.
	 * @throws SPFormatException on SP format error
	 * @throws XMLStreamException on XML format error
	 * @throws MapVersionException if map format too old or new for a reader
	 */
	public void assertMapDeserialization(final String message,
			final MapView expected, final String xml)
			throws MapVersionException, XMLStreamException, SPFormatException {
		assertEquals(message, expected, ((IMapReader) oldReader).readMap(
				FAKE_FILENAME, new StringReader(xml), new Warning(
						Warning.Action.Die)));
		assertEquals(message, expected, ((IMapReader) newReader).readMap(
				FAKE_FILENAME, new StringReader(xml), new Warning(
						Warning.Action.Die)));
	}

	/**
	 * Test whether an item is in an iterable. Note that the iterable's iterator
	 * will have advanced either to the item searched for or to the end.
	 *
	 * @param iter an iterable
	 * @param item an item
	 * @param <T> the type of the items in the iterator
	 * @param <U> the type of the item
	 * @return whether the iterable contains the item
	 */
	public <T, U extends T> boolean doesIterableContain(final Iterable<T> iter, final U item) {
		for (final T each : iter) {
			if (each.equals(item)) {
				return true; // NOPMD
			}
		}
		return false;
	}
	/**
	 * Determine the size of an iterable. Note that its iterator will have been advanced to the end.
	 * @param iter an iterable
	 * @param <T> the type of thing it contains
	 * @return the number of items in the iterable
	 */
	public <T> int iteratorSize(final Iterable<T> iter) {
		int size = 0;
		final Iterator<T> iterator = iter.iterator();
		// ESCA-JAVA0254:
		while (iterator.hasNext()) { // $codepro.audit.disable
			size++;
			iterator.next();
		}
		return size;
	}
}
