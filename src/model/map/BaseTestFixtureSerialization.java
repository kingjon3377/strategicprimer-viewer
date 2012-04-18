package model.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import util.FatalWarning;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.simplexml.SimpleXMLReader;

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
	public static void assertUnwantedChild(final SimpleXMLReader reader, final String xml,
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
	public static void assertMissingProperty(final SimpleXMLReader reader, final String xml,
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
			final SimpleXMLReader reader, final T orig, final Class<T> type,
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
