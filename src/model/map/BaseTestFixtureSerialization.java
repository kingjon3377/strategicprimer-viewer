package model.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;

import util.FatalWarning;
import util.Warning;
import controller.map.DeprecatedPropertyException;
import controller.map.ISPReader;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.TestReaderFactory;
import controller.map.UnsupportedTagException;
import controller.map.UnwantedChildException;
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
	private static final String FAKE_FILENAME = "string";
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
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param warning
	 *            whether this is supposed to be a warning only
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	public void assertUnwantedChild(final String xml,
			final Class<?> desideratum, final boolean warning)
			throws XMLStreamException, SPFormatException {
		assertUnwantedChild(oldReader, xml, desideratum, warning);
		assertUnwantedChild(newReader, xml, desideratum, warning);
	}
	/**
	 * Assert that reading the given XML will produce an UnwantedChildException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but fail with warnings made fatal. This version runs
	 * against both the reflection and non-reflection versions.
	 * 
	 * @param reader
	 *            the reader to do the reading
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param warning
	 *            whether this is supposed to be a warning only
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	private static void assertUnwantedChild(final ISPReader reader, final String xml,
			final Class<?> desideratum, final boolean warning) throws XMLStreamException,
			SPFormatException {
		assertUnwantedChild(reader, xml, desideratum, true, warning);
		assertUnwantedChild(reader, xml, desideratum, false, warning);
	}
	/**
	 * Assert that reading the given XML will produce an UnsupportedTagException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but fail with warnings made fatal. This version uses both old and new readers.
	 * 
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param tag the unsupported tag
	 * @param warning
	 *            whether this is supposed to be a warning only
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	public void assertUnsupportedTag(final String xml,
			final Class<?> desideratum, final String tag, final boolean warning)
			throws XMLStreamException, SPFormatException {
		assertUnsupportedTag(oldReader, xml, desideratum, tag, warning);
		assertUnsupportedTag(newReader, xml, desideratum, tag, warning);
	}
	/**
	 * Assert that reading the given XML will produce an UnsupportedTagException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but fail with warnings made fatal. This version uses both reflection and non-reflection readers.
	 * 
	 * @param reader
	 *            the reader to do the reading
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param tag the unsupported tag
	 * @param warning
	 *            whether this is supposed to be a warning only
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	private static void assertUnsupportedTag(final ISPReader reader, final String xml,
			final Class<?> desideratum, final String tag, final boolean warning) throws XMLStreamException,
			SPFormatException {
		assertUnsupportedTag(reader, xml, desideratum, tag, true, warning);
		assertUnsupportedTag(reader, xml, desideratum, tag, false, warning);
	}
	/**
	 * Assert that reading the given XML will produce an UnsupportedTagException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but fail with warnings made fatal.
	 * 
	 * @param reader
	 *            the reader to do the reading
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param tag the unsupported tag
	 * @param reflection
	 *            whether to use the reflection version or not
	 * @param warning
	 *            whether this is supposed to be a warning only
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	private static void assertUnsupportedTag(final ISPReader reader,
			final String xml, final Class<?> desideratum, final String tag,
			final boolean reflection, final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum, reflection, new Warning(Warning.Action.Ignore));
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum, reflection, new Warning(Warning.Action.Die));
			} catch (FatalWarning except) {
				assertTrue("Unsupported tag", except.getCause() instanceof UnsupportedTagException);
				assertEquals("The tag we expected", tag, ((UnsupportedTagException) except.getCause()).getTag());
			}
		} else {
			try {
				reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum, reflection, new Warning(Warning.Action.Ignore));
				fail("Expected an UnsupportedTagException");
			} catch (UnsupportedTagException except) {
				assertEquals("The tag we expected", tag, except.getTag());
			}
		}
	}

	/**
	 * Assert that reading the given XML will produce an UnwantedChildException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but fail with warnings made fatal.
	 * 
	 * @param reader
	 *            the reader to do the reading
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param reflection
	 *            whether to use the reflection version or not
	 * @param warning
	 *            whether this is supposed to be a warning only
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	private static void assertUnwantedChild(final ISPReader reader, final String xml,
			final Class<?> desideratum, final boolean reflection, final boolean warning) throws XMLStreamException,
			SPFormatException {
				if (warning) {
					reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum, reflection,
							new Warning(Warning.Action.Ignore));
					try {
						reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum, reflection,
								new Warning(Warning.Action.Die));
						fail("We were expecting an UnwantedChildException");
					} catch (FatalWarning except) {
						assertTrue("Unwanted child",
								except.getCause() instanceof UnwantedChildException);
					}
				} else {
					try {
						reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum, reflection,
								new Warning(Warning.Action.Ignore));
						fail("We were expecting an UnwantedChildException");
					} catch (UnwantedChildException except) {
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
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param property
	 *            the missing property
	 * @param warning
	 *            whether this is supposed to be only a warning
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	public void assertMissingProperty(final String xml,
			final Class<?> desideratum, final String property,
			final boolean warning) throws XMLStreamException, SPFormatException {
		assertMissingProperty(oldReader, xml, desideratum, property, warning);
		assertMissingProperty(newReader, xml, desideratum, property, warning);
	}
	/**
	 * Assert that reading the given XML will give a MissingPropertyException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but object with them made fatal. This version runs both
	 * with and without reflection.
	 * 
	 * @param reader
	 *            the reader to do the reading
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param property
	 *            the missing property
	 * @param warning
	 *            whether this is supposed to be only a warning
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	private static void assertMissingProperty(final ISPReader reader, final String xml,
			final Class<?> desideratum, final String property, final boolean warning)
			throws XMLStreamException, SPFormatException {
		assertMissingProperty(reader, xml, desideratum, property, true, warning);
		assertMissingProperty(reader, xml, desideratum, property, false, warning);
	}
	/**
	 * Assert that reading the given XML will give a MissingPropertyException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but object with them made fatal.
	 * 
	 * @param reader
	 *            the reader to do the reading
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param property
	 *            the missing property
	 * @param reflection
	 *            whether to use the reflection version of the reader or not
	 * @param warning
	 *            whether this is supposed to be only a warning
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	private static void assertMissingProperty(final ISPReader reader, final String xml,
			final Class<?> desideratum, final String property, final boolean reflection, final boolean warning)
			throws XMLStreamException, SPFormatException {
				if (warning) {
					reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum, reflection,
							new Warning(Warning.Action.Ignore));
					try {
						reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum, reflection,
								new Warning(Warning.Action.Die));
						fail("We were expecting a MissingParameterException");
					} catch (FatalWarning except) {
						assertTrue("Missing property",
								except.getCause() instanceof MissingParameterException);
						assertEquals(
								"The missing property should be the one we're expecting",
								property, ((MissingParameterException) except
										.getCause()).getParam());
					}
				} else {
					try {
						reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum, reflection,
								new Warning(Warning.Action.Ignore));
					} catch (MissingParameterException except) {
						assertEquals(
								"Missing property should be the one we're expecting",
								property, except.getParam());
					}
				}
			}
	/**
	 * Assert that reading the given XML will give a DeprecatedPropertyException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but object with them made fatal. This version tests both old and new readers.
	 * 
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param deprecated
	 *            the deprecated property
	 * @param warning
	 *            whether this is supposed to be only a warning
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	public void assertDeprecatedProperty(final String xml,
			final Class<?> desideratum, final String deprecated, final boolean warning)
			throws XMLStreamException, SPFormatException {
		assertDeprecatedProperty(oldReader, xml, desideratum, deprecated, warning);
		assertDeprecatedProperty(newReader, xml, desideratum, deprecated, warning);
	}
	/**
	 * Assert that reading the given XML will give a DeprecatedPropertyException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but object with them made fatal. This version tests both reflection and non-reflection versions.
	 * 
	 * @param reader
	 *            the reader to do the reading
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param deprecated
	 *            the deprecated property
	 * @param warning
	 *            whether this is supposed to be only a warning
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	private static void assertDeprecatedProperty(final ISPReader reader, final String xml,
			final Class<?> desideratum, final String deprecated, final boolean warning)
			throws XMLStreamException, SPFormatException {
		assertDeprecatedProperty(reader, xml, desideratum, deprecated, true, warning);
		assertDeprecatedProperty(reader, xml, desideratum, deprecated, false, warning);
	}
	/**
	 * Assert that reading the given XML will give a DeprecatedPropertyException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but object with them made fatal.
	 * 
	 * @param reader
	 *            the reader to do the reading
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param deprecated
	 *            the deprecated property
	 * @param reflection
	 *            whether to use the reflection version of the reader or not
	 * @param warning
	 *            whether this is supposed to be only a warning
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	private static void assertDeprecatedProperty(final ISPReader reader, final String xml,
			final Class<?> desideratum, final String deprecated, final boolean reflection, final boolean warning)
			throws XMLStreamException, SPFormatException {
				if (warning) {
					reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum, reflection,
							new Warning(Warning.Action.Ignore));
					try {
						reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum, reflection,
								new Warning(Warning.Action.Die));
						fail("We were expecting a MissingParameterException");
					} catch (FatalWarning except) {
						assertTrue("Missing property",
								except.getCause() instanceof DeprecatedPropertyException);
						assertEquals(
								"The missing property should be the one we're expecting",
								deprecated, ((DeprecatedPropertyException) except
										.getCause()).getOld());
					}
				} else {
					try {
						reader.readXML(FAKE_FILENAME, new StringReader(xml), desideratum, reflection,
								new Warning(Warning.Action.Ignore));
					} catch (DeprecatedPropertyException except) {
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
	 * @param <T>
	 *            the type of the object
	 * @param message
	 *            the message to use
	 * @param obj
	 *            the object to serialize
	 * @param type
	 *            its type
	 * @throws SPFormatException
	 *             on SP XML problem
	 * @throws XMLStreamException
	 *             on XML reading problem
	 * @throws IOException on I/O error creating serialized form
	 */
	public <T extends XMLWritable> void assertSerialization(final String message,
			final T obj, final Class<T> type)
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization(message, obj, type, new Warning(Warning.Action.Die));
	}
	/**
	 * Assert that the serialized form of the given object will deserialize
	 * without error using both the old and new readers.
	 * 
	 * @param <T>
	 *            the type of the object
	 * @param message
	 *            the message to use
	 * @param obj
	 *            the object to serialize
	 * @param type
	 *            its type
	 * @param warning the warning instance to use
	 * @throws SPFormatException
	 *             on SP XML problem
	 * @throws XMLStreamException
	 *             on XML reading problem
	 * @throws IOException on I/O error creating serialized form
	 */
	public <T extends XMLWritable> void assertSerialization(final String message,
			final T obj, final Class<T> type, final Warning warning)
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization(message, oldReader, obj, type, warning);
		assertSerialization(message, newReader, obj, type, warning);
	}
	/**
	 * Assert that the serialized form of the given object will deserialize
	 * without error using both the reflection and non-reflection methods.
	 * 
	 * @param <T>
	 *            the type of the object
	 * @param message
	 *            the message to use
	 * @param reader
	 *            the reader to parse the serialized form
	 * @param obj
	 *            the object to serialize
	 * @param type
	 *            its type
	 * @param warner the warning instance to use
	 * @throws SPFormatException
	 *             on SP XML problem
	 * @throws XMLStreamException
	 *             on XML reading problem
	 * @throws IOException on I/O error creating serialized form
	 */
	@SuppressWarnings("deprecation")
	private static <T extends XMLWritable> void assertSerialization(final String message,
			final ISPReader reader, final T obj, final Class<T> type, final Warning warner)
			throws XMLStreamException, SPFormatException, IOException {
		assertEquals(message, obj, reader.readXML(FAKE_FILENAME, new StringReader(obj.toXML()), type, false, warner));
		assertEquals(message, obj, reader.readXML(FAKE_FILENAME, new StringReader(obj.toXML()), type, true, warner));
		assertEquals(message, obj, reader.readXML(FAKE_FILENAME, new StringReader(createSerializedForm(obj)), type, false, warner));
		assertEquals(message, obj, reader.readXML(FAKE_FILENAME, new StringReader(createSerializedForm(obj)), type, true, warner));
	}
	/**
	 * Assert that a deprecated idiom deserializes properly if warnings are ignored, but is warned about.
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
			final Class<T> type, final String property) throws XMLStreamException, SPFormatException {
		assertEquals(message, expected, oldReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, true, new Warning(
						Warning.Action.Ignore)));
		assertEquals(message, expected, oldReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, false, new Warning(
						Warning.Action.Ignore)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, true, new Warning(
						Warning.Action.Ignore)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, false, new Warning(
						Warning.Action.Ignore)));
		assertDeprecatedProperty(xml, type, property, true);
	}
	
	/**
	 * Assert that a serialized form with a recommended but not required
	 * property missing deserializes properly if warnings are ignored, but is
	 * warned about.
	 * 
	 * @param <T>
	 *            the type
	 * @param message
	 *            the message to pass to JUnit
	 * @param expected
	 *            the object we expect the deserialized form to equal
	 * @param xml
	 *            the serialized form
	 * @param type
	 *            the type of object
	 * @param property
	 *            the missing property
	 * @throws SPFormatException
	 *             on SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	public <T extends XMLWritable> void assertMissingPropertyDeserialization(
			final String message, final T expected, final String xml,
			final Class<T> type, final String property) throws XMLStreamException, SPFormatException {
		assertEquals(message, expected, oldReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, true, new Warning(
						Warning.Action.Ignore)));
		assertEquals(message, expected, oldReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, false, new Warning(
						Warning.Action.Ignore)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, true, new Warning(
						Warning.Action.Ignore)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, false, new Warning(
						Warning.Action.Ignore)));
		assertMissingProperty(xml, type, property, true);
	}
	
	/**
	 * Assert that a "forward idiom"---an idiom that we do not yet produce, but
	 * want to accept---will be deserialized properly by both readers, both with
	 * and without reflection.
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
				new StringReader(xml), type, false, new Warning(
						Warning.Action.Die)));
		assertEquals(message, expected, oldReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, true, new Warning(
						Warning.Action.Die)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, false, new Warning(
						Warning.Action.Die)));
		assertEquals(message, expected, newReader.readXML(FAKE_FILENAME,
				new StringReader(xml), type, true, new Warning(
						Warning.Action.Die)));
	}
	/**
	 * Assert that two deserialzed forms are equivalent, using both readers, both with and without reflection.
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
		assertEquals(message, oldReader.readXML(FAKE_FILENAME, new StringReader(one), type,
				true, new Warning(warningLevel)), oldReader.readXML(FAKE_FILENAME,
				new StringReader(two), type, true, new Warning(warningLevel)));
		assertEquals(message, oldReader.readXML(FAKE_FILENAME, new StringReader(one), type,
				false, new Warning(warningLevel)), oldReader.readXML(FAKE_FILENAME,
				new StringReader(two), type, false, new Warning(warningLevel)));
		assertEquals(message, newReader.readXML(FAKE_FILENAME, new StringReader(one), type,
				true, new Warning(warningLevel)), newReader.readXML(FAKE_FILENAME,
				new StringReader(two), type, true, new Warning(warningLevel)));
		assertEquals(message, newReader.readXML(FAKE_FILENAME, new StringReader(one), type,
				false, new Warning(warningLevel)), newReader.readXML(FAKE_FILENAME,
				new StringReader(two), type, false, new Warning(warningLevel)));
	}
	/**
	 * @param obj an object
	 * @return its serialized form
	 * @throws IOException on I/O error creating it
	 */
	public static String createSerializedForm(final XMLWritable obj) throws IOException {
		final StringWriter writer = new StringWriter();
		new ReaderAdapter().write(obj, writer, true);
		return writer.toString();
	}
	/**
	 * @param <T> the type of the object
	 * @param obj an object
	 * @return it, with its file set to "string"
	 */
	public static <T extends XMLWritable> T setFileOnObject(final T obj) {
		obj.setFile("string");
		return obj;
	}
}
