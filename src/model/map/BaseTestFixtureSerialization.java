package model.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import util.FatalWarningException;
import util.NullCleaner;
import util.Warning;
import util.Warning.Action;
import controller.map.cxml.CompactXMLWriter;
import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingChildException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.IMapReader;
import controller.map.iointerfaces.ISPReader;
import controller.map.iointerfaces.TestReaderFactory;

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
	protected static final File FAKE_FILENAME = new File("");
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
	protected void assertUnwantedChild(final String xml,
			final Class<?> desideratum, final boolean warning)
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
	protected void assertUnsupportedTag(final String xml,
			final Class<?> desideratum, final String tag, final boolean warning)
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
			final String xml, final Class<?> desideratum, final String tag,
			final boolean warning) throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum,
					new Warning(Action.Ignore));
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Action.Die));
			} catch (final FatalWarningException except) {
				final Throwable cause = except.getCause();
				assertTrue("Unsupported tag",
						cause instanceof UnsupportedTagException);
				assert cause instanceof UnsupportedTagException;
				assertEquals("The tag we expected", tag,
						((UnsupportedTagException) cause).getTag());
			}
		} else {
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Action.Ignore));
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
			final String xml, final Class<?> desideratum, final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum,
					new Warning(Action.Ignore));
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Action.Die));
				fail("We were expecting an UnwantedChildException");
			} catch (final FatalWarningException except) {
				assertTrue("Unwanted child",
						except.getCause() instanceof UnwantedChildException);
			}
		} else {
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Action.Ignore));
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
	protected void assertMissingProperty(final String xml,
			final Class<?> desideratum, final String property,
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
			final String xml, final Class<?> desideratum,
			final String property, final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum,
					new Warning(Action.Ignore));
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Action.Die));
				fail("We were expecting a MissingParameterException");
			} catch (final FatalWarningException except) {
				final Throwable cause = except.getCause();
				assertTrue("Missing property",
						cause instanceof MissingPropertyException);
				assert cause instanceof MissingPropertyException;
				assertEquals(
						"The missing property should be the one we're expecting",
						property, ((MissingPropertyException) cause).getParam());
			}
		} else {
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Action.Ignore));
			} catch (final MissingPropertyException except) {
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
	protected void assertDeprecatedProperty(final String xml,
			final Class<?> desideratum, final String deprecated,
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
			final String xml, final Class<?> desideratum,
			final String deprecated, final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum,
					new Warning(Action.Ignore));
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Action.Die));
				fail("We were expecting a MissingParameterException");
			} catch (final FatalWarningException except) {
				final Throwable cause = except.getCause();
				assertTrue("Missing property",
						cause instanceof DeprecatedPropertyException);
				assert cause instanceof DeprecatedPropertyException;
				assertEquals(
						"The missing property should be the one we're expecting",
						deprecated,
						((DeprecatedPropertyException) cause).getOld());
			}
		} else {
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Action.Ignore));
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
	protected <T> void assertSerialization(final String message, final T obj,
			final Class<T> type) throws XMLStreamException, SPFormatException,
			IOException {
		assertSerialization(message, obj, type, new Warning(Action.Die));
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
	protected <T> void assertSerialization(final String message, final T obj,
			final Class<T> type, final Warning warning)
			throws XMLStreamException, SPFormatException, IOException {
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
	private static <T> void assertSerialization(final String message,
			final ISPReader reader, final T obj, final Class<T> type,
			final Warning warner) throws XMLStreamException, SPFormatException,
			IOException {
		assertEquals(message, obj,
				reader.readXML(FAKE_FILENAME, new StringReader(
						createSerializedForm(obj, true)), type, warner));
		assertEquals(message, obj,
				reader.readXML(FAKE_FILENAME, new StringReader(
						createSerializedForm(obj, true)), type, warner));

		assertEquals(message, obj, reader.readXML(FAKE_FILENAME,
				new StringReader(createSerializedForm(obj, false)), type,
				warner));
		assertEquals(message, obj, reader.readXML(FAKE_FILENAME,
				new StringReader(createSerializedForm(obj, false)), type,
				warner));
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
	protected <T> void assertDeprecatedDeserialization(final String message,
			final T expected, final String xml, final Class<T> type,
			final String property) throws XMLStreamException, SPFormatException {
		assertEquals(message, expected,
				oldReader.readXML(FAKE_FILENAME, new StringReader(xml), type,
						new Warning(Action.Ignore)));
		assertEquals(message, expected,
				oldReader.readXML(FAKE_FILENAME, new StringReader(xml), type,
						new Warning(Action.Ignore)));
		assertEquals(message, expected,
				newReader.readXML(FAKE_FILENAME, new StringReader(xml), type,
						new Warning(Action.Ignore)));
		assertEquals(message, expected,
				newReader.readXML(FAKE_FILENAME, new StringReader(xml), type,
						new Warning(Action.Ignore)));
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
	protected <T> void assertMissingPropertyDeserialization(
			final String message, final T expected, final String xml,
			final Class<T> type, final String property)
			throws XMLStreamException, SPFormatException {
		assertEquals(message, expected,
				oldReader.readXML(FAKE_FILENAME, new StringReader(xml), type,
						new Warning(Action.Ignore)));
		assertEquals(message, expected,
				oldReader.readXML(FAKE_FILENAME, new StringReader(xml), type,
						new Warning(Action.Ignore)));
		assertEquals(message, expected,
				newReader.readXML(FAKE_FILENAME, new StringReader(xml), type,
						new Warning(Action.Ignore)));
		assertEquals(message, expected,
				newReader.readXML(FAKE_FILENAME, new StringReader(xml), type,
						new Warning(Action.Ignore)));
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
	protected <T> void assertForwardDeserialization(final String message,
			final T expected, final String xml, final Class<T> type)
			throws XMLStreamException, SPFormatException {
		assertEquals(message, expected, oldReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(Action.Die)));
		assertEquals(message, expected, oldReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(Action.Die)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(Action.Die)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, new Warning(Action.Die)));
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
	protected <T> void assertEquivalentForms(final String message,
			final String one, final String two, final Class<T> type,
			final Warning.Action warningLevel) throws SPFormatException,
			XMLStreamException {
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
	protected static String createSerializedForm(final Object obj,
			final boolean deprecated) throws IOException {
		final StringWriter writer = new StringWriter();
		if (deprecated) {
			controller.map.readerng.ReaderAdapter.ADAPTER.write(obj).write(
					writer, 0);
		} else {
			CompactXMLWriter.writeObject(writer, obj);
		}
		return NullCleaner.assertNotNull(writer.toString());
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
	protected void assertMissingChild(final String xml,
			final Class<?> desideratum, final boolean warning)
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
			final String xml, final Class<?> desideratum, final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum,
					new Warning(Action.Ignore));
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Action.Die));
				fail("We were expecting a MissingChildException");
			} catch (final FatalWarningException except) {
				assertTrue("Missing child",
						except.getCause() instanceof MissingChildException);
			}
		} else {
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml),
						desideratum, new Warning(Action.Ignore));
				fail("We were expecting a MissingChildException");
			} catch (final MissingChildException except) {
				assertTrue("Got the expected MissingChildException", true);
			}
		}
	}

	/**
	 * Assert that a map is properly deserialized (by the main
	 * map-deserialization methods) into a view.
	 *
	 * @param message
	 *            the message to use in JUnit calls
	 * @param expected
	 *            the object to test against
	 * @param xml
	 *            the XML to deserialize it into.
	 * @throws SPFormatException
	 *             if map format too old or new for a reader, or on other SP
	 *             format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	protected void assertMapDeserialization(final String message,
			final MapView expected, final String xml)
			throws XMLStreamException, SPFormatException {
		assertEquals(message, new MapNGAdapter(expected),
				((IMapReader) oldReader).readMap(FAKE_FILENAME,
						new StringReader(xml), new Warning(Action.Die)));
		assertEquals(message, new MapNGAdapter(expected),
				((IMapReader) newReader).readMap(FAKE_FILENAME,
						new StringReader(xml), new Warning(Action.Die)));
	}

	/**
	 * Determine the size of an iterable. Note that its iterator will have been
	 * advanced to the end.
	 *
	 * @param iter an iterable
	 * @param <T> the type of thing it contains
	 * @return the number of items in the iterable
	 */
	protected static <T> int iteratorSize(final Iterable<T> iter) {
		int size = 0; // NOPMD
		final Iterator<T> iterator = iter.iterator();
		// ESCA-JAVA0254:
		while (iterator.hasNext()) { // $codepro.audit.disable
			size++; // NOPMD
			iterator.next();
		}
		return size;
	}

	/**
	 * Assert that the given object, if serialized and deserialized, will have
	 * its image property preserved. We modify its image property, but set it
	 * back to the original value before exiting the method.
	 *
	 * @param <T> the type of the object
	 * @param message the message to use for assertions
	 * @param obj the object to serialize
	 * @param type its type,
	 * @throws SPFormatException on SP XML problem
	 * @throws XMLStreamException on XML reading problem
	 * @throws IOException on I/O error creating serialized form
	 */
	protected <T extends HasImage> void assertImageSerialization(
			final String message, final T obj, final Class<T> type)
			throws XMLStreamException, SPFormatException, IOException {
		final HasImage objInternal = obj;
		final String origImage = objInternal.getImage();
		objInternal.setImage("imageForSerialization");
		assertImageSerialization(message, obj, type, oldReader);
		assertImageSerialization(message, obj, type, newReader);
		objInternal.setImage(origImage);
	}

	/**
	 * Assert that the given object, if serialized and deserialized, will have
	 * its image property preserved. We modify its image property, but set it
	 * back to the original value before exiting the method.
	 *
	 * @param <T> the type of the object
	 * @param message the message to use for assertions
	 * @param obj the object to serialize
	 * @param type its type,
	 * @param reader the reader to use
	 * @throws SPFormatException on SP XML problem
	 * @throws XMLStreamException on XML reading problem
	 * @throws IOException on I/O error creating serialized form
	 */
	private static <T extends HasImage> void assertImageSerialization(
			final String message, final T obj, final Class<T> type,
			final ISPReader reader) throws XMLStreamException,
			SPFormatException, IOException {
		final HasImage objInternal = obj;
		assertEquals(
				message,
				objInternal.getImage(),
				reader.readXML(FAKE_FILENAME,
						new StringReader(createSerializedForm(obj, true)),
						type, new Warning(Action.Ignore)).getImage());
		assertEquals(
				message,
				objInternal.getImage(),
				reader.readXML(FAKE_FILENAME,
						new StringReader(createSerializedForm(obj, false)),
						type, new Warning(Action.Ignore)).getImage());
	}
}
