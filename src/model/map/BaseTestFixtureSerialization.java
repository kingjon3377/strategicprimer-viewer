package model.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import util.FatalWarning;
import util.Warning;
import util.Warning.Action;
import controller.map.DeprecatedPropertyException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnsupportedTagException;
import controller.map.UnwantedChildException;
import controller.map.simplexml.ISPReader;

// ESCA-JAVA0011:
/**
 * An abstract base class for this helper method.
 * 
 * @author Jonathan Lovelace
 * 
 */
public abstract class BaseTestFixtureSerialization { // NOPMD
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
	public static void assertUnwantedChild(final ISPReader reader, final String xml,
			final Class<?> desideratum, final boolean warning) throws XMLStreamException,
			SPFormatException {
		assertUnwantedChild(reader, xml, desideratum, true, warning);
		assertUnwantedChild(reader, xml, desideratum, false, warning);
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
	public static void assertUnsupportedTag(final ISPReader reader, final String xml,
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
	public static void assertUnsupportedTag(final ISPReader reader,
			final String xml, final Class<?> desideratum, final String tag,
			final boolean reflection, final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(new StringReader(xml), desideratum, reflection, new Warning(Warning.Action.Ignore));
			try {
				reader.readXML(new StringReader(xml), desideratum, reflection, new Warning(Warning.Action.Die));
			} catch (FatalWarning except) {
				assertTrue("Unsupported tag", except.getCause() instanceof UnsupportedTagException);
				assertEquals("The tag we expected", tag, ((UnsupportedTagException) except.getCause()).getTag());
			}
		} else {
			try {
				reader.readXML(new StringReader(xml), desideratum, reflection, new Warning(Warning.Action.Ignore));
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
	public static void assertUnwantedChild(final ISPReader reader, final String xml,
			final Class<?> desideratum, final boolean reflection, final boolean warning) throws XMLStreamException,
			SPFormatException {
				if (warning) {
					reader.readXML(new StringReader(xml), desideratum, reflection,
							new Warning(Warning.Action.Ignore));
					try {
						reader.readXML(new StringReader(xml), desideratum, reflection,
								new Warning(Warning.Action.Die));
						fail("We were expecting an UnwantedChildException");
					} catch (FatalWarning except) {
						assertTrue("Unwanted child",
								except.getCause() instanceof UnwantedChildException);
					}
				} else {
					try {
						reader.readXML(new StringReader(xml), desideratum, reflection,
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
	public static void assertMissingProperty(final ISPReader reader, final String xml,
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
	public static void assertMissingProperty(final ISPReader reader, final String xml,
			final Class<?> desideratum, final String property, final boolean reflection, final boolean warning)
			throws XMLStreamException, SPFormatException {
				if (warning) {
					reader.readXML(new StringReader(xml), desideratum, reflection,
							new Warning(Warning.Action.Ignore));
					try {
						reader.readXML(new StringReader(xml), desideratum, reflection,
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
						reader.readXML(new StringReader(xml), desideratum, reflection,
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
	public static void assertDeprecatedProperty(final ISPReader reader, final String xml,
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
	public static void assertDeprecatedProperty(final ISPReader reader, final String xml,
			final Class<?> desideratum, final String deprecated, final boolean reflection, final boolean warning)
			throws XMLStreamException, SPFormatException {
				if (warning) {
					reader.readXML(new StringReader(xml), desideratum, reflection,
							new Warning(Warning.Action.Ignore));
					try {
						reader.readXML(new StringReader(xml), desideratum, reflection,
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
						reader.readXML(new StringReader(xml), desideratum, reflection,
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
	 * @throws SPFormatException
	 *             on SP XML problem
	 * @throws XMLStreamException
	 *             on XML reading problem
	 */
	public static <T extends XMLWritable> void assertSerialization(final String message,
			final ISPReader reader, final T obj, final Class<T> type)
			throws XMLStreamException, SPFormatException {
		assertEquals(message, obj, reader.readXML(new StringReader(obj.toXML()), type, false, new Warning(Action.Die)));
		assertEquals(message, obj, reader.readXML(new StringReader(obj.toXML()), type, true, new Warning(Action.Die)));
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
	 */
	public static <T extends XMLWritable> void assertSerialization(final String message,
			final ISPReader reader, final T obj, final Class<T> type, final Warning warner)
			throws XMLStreamException, SPFormatException {
		assertEquals(message, obj, reader.readXML(new StringReader(obj.toXML()), type, false, warner));
		assertEquals(message, obj, reader.readXML(new StringReader(obj.toXML()), type, true, warner));
	}
	/**
	 * A helper method to simplify test boiler plate code.
	 * 
	 * @param <T>
	 *            The type of the object
	 * @param reader
	 *            the reader to parse the serialized form.
	 * @param orig
	 *            the object to serialize
	 * @param type
	 *            the type of the object
	 * @param reflection
	 *            whether to use reflection
	 * @return the result of deserializing the serialized form
	 * @throws SPFormatException
	 *             on SP XML problem
	 * @throws XMLStreamException
	 *             on XML reading problem
	 */
	protected <T extends XMLWritable> T helpSerialization(
			final ISPReader reader, final T orig, final Class<T> type,
			final boolean reflection) throws XMLStreamException,
			SPFormatException {
		return reader.readXML(new StringReader(orig.toXML()), type, reflection, warner);
	}
	/**
	 * Warning instance that makes warnings fatal.
	 */
	private final Warning warner = new Warning(Warning.Action.Die);
	/**
	 * @return the warning instance
	 */
	protected Warning warner() {
		return warner;
	}
}
