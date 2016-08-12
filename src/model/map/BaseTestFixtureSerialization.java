package model.map;

import controller.map.cxml.CompactXMLWriter;
import controller.map.fluidxml.SPFluidWriter;
import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingChildException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.IMapReader;
import controller.map.iointerfaces.ISPReader;
import controller.map.iointerfaces.TestReaderFactory;
import controller.map.misc.DuplicateIDException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;
import javax.xml.stream.XMLStreamException;
import util.FatalWarningException;
import util.NullCleaner;
import util.TypesafeLogger;
import util.Warning;

import static jdk.internal.dynalink.support.Guards.isNull;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * An abstract base class for this helper method.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
@SuppressWarnings({"ElementOnlyUsedFromTestCode", "ClassHasNoToStringMethod"})
public abstract class BaseTestFixtureSerialization {
	/**
	 * The "filename" to pass to the readers.
	 */
	private static final Path FAKE_FILENAME = Paths.get("");
	/**
	 * An instance of the previous-generation reader to test against.
	 */
	private final ISPReader oldReader = TestReaderFactory.createOldReader();
	/**
	 * An instance of the current-generation reader to test against.
	 */
	private final ISPReader newReader = TestReaderFactory.createNewReader();
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			TypesafeLogger.getLogger(BaseTestFixtureSerialization.class);

	/**
	 * Assert that reading the given XML will produce an UnwantedChildException. If it's
	 * only supposed to be a warning, assert that it'll pass with warnings disabled but
	 * fail with warnings made fatal. This version runs against both the old and the new
	 * reader.
	 *
	 * @param xml         the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param warning     whether this is supposed to be a warning only
	 * @throws SPFormatException  on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	protected final void assertUnwantedChild(final String xml,
									final Class<?> desideratum, final boolean warning)
			throws XMLStreamException, SPFormatException {
		assertUnwantedChild(oldReader, xml, desideratum, warning);
		assertUnwantedChild(newReader, xml, desideratum, warning);
	}

	/**
	 * Assert that reading the given XML will produce an UnsupportedTagException. If it's
	 * only supposed to be a warning, assert that it'll pass with warnings disabled but
	 * fail with warnings made fatal. This version uses both old and new readers.
	 *
	 * @param xml         the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param tag         the unsupported tag
	 * @param warning     whether this is supposed to be a warning only
	 * @throws SPFormatException  on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	protected final void assertUnsupportedTag(final String xml,
										final Class<?> desideratum, final String tag,
										final boolean warning)
			throws XMLStreamException, SPFormatException {
		assertUnsupportedTag(oldReader, xml, desideratum, tag, warning);
		assertUnsupportedTag(newReader, xml, desideratum, tag, warning);
	}

	/**
	 * Assert that reading the given XML will produce an UnsupportedTagException. If it's
	 * only supposed to be a warning, assert that it'll pass with warnings disabled but
	 * fail with warnings made fatal.
	 *
	 * @param reader      the reader to do the reading
	 * @param xml         the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param tag         the unsupported tag
	 * @param warning     whether this is supposed to be a warning only
	 * @throws SPFormatException  on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	private static void assertUnsupportedTag(final ISPReader reader,
											final String xml,
											final Class<?> desideratum,
											final String tag,
											final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			try (StringReader stringReader = new StringReader(xml)) {
				reader.readXML(FAKE_FILENAME, stringReader, desideratum,
						Warning.Ignore);
			}
			try (StringReader stringReader = new StringReader(xml)) {
				reader.readXML(FAKE_FILENAME, stringReader,
						desideratum, Warning.Die);
			} catch (final FatalWarningException except) {
				final Throwable cause = except.getCause();
				assertThat("Unsupported tag", cause,
						instanceOf(UnsupportedTagException.class));
				assertThat("The tag we expected",
						((UnsupportedTagException) cause).getTag().getLocalPart(),
						equalTo(tag));
			}
		} else {
			try (StringReader stringReader = new StringReader(xml)) {
				reader.readXML(FAKE_FILENAME, stringReader, desideratum,
						Warning.Ignore);
				fail("Expected an UnsupportedTagException");
			} catch (final UnsupportedTagException except) {
				assertThat("The tag we expected", except.getTag().getLocalPart(),
						equalTo(tag));
			}
		}
	}

	/**
	 * Assert that reading the given XML will produce an UnwantedChildException. If it's
	 * only supposed to be a warning, assert that it'll pass with warnings disabled but
	 * fail with warnings made fatal.
	 *
	 * @param reader      the reader to do the reading
	 * @param xml         the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param warning     whether this is supposed to be a warning only
	 * @throws SPFormatException  on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	private static void assertUnwantedChild(final ISPReader reader,
											final String xml, final Class<?> desideratum,
											final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			try (StringReader stringReader = new StringReader(xml)) {
				reader.readXML(FAKE_FILENAME, stringReader, desideratum,
						Warning.Ignore);
			}
			try (StringReader stringReader = new StringReader(xml)) {
				reader.readXML(FAKE_FILENAME, stringReader, desideratum,
						Warning.Die);
				fail("We were expecting an UnwantedChildException");
			} catch (final FatalWarningException except) {
				assertThat("Unwanted child", except.getCause(),
						instanceOf(UnwantedChildException.class));
			}
		} else {
			try (StringReader stringReader = new StringReader(xml)) {
				reader.readXML(FAKE_FILENAME, stringReader, desideratum,
						Warning.Ignore);
				fail("We were expecting an UnwantedChildException");
			} catch (final UnwantedChildException except) {
				assertThat("Dummy check", except, not(isNull()));
			}
		}
	}

	/**
	 * Assert that reading the given XML will give a MissingPropertyException. If it's
	 * only supposed to be a warning, assert that it'll pass with warnings disabled but
	 * object with them made fatal. This version tests both old and new readers.
	 *
	 * @param xml         the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param property    the missing property
	 * @param warning     whether this is supposed to be only a warning
	 * @throws SPFormatException  on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	protected final void assertMissingProperty(final String xml,
										final Class<?> desideratum,
										final String property,
										final boolean warning)
			throws XMLStreamException, SPFormatException {
		assertMissingProperty(oldReader, xml, desideratum, property, warning);
		assertMissingProperty(newReader, xml, desideratum, property, warning);
	}

	/**
	 * Assert that reading the given XML will give a MissingPropertyException. If it's
	 * only supposed to be a warning, assert that it'll pass with warnings disabled but
	 * object with them made fatal.
	 *
	 * @param reader      the reader to do the reading
	 * @param xml         the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param property    the missing property
	 * @param warning     whether this is supposed to be only a warning
	 * @throws SPFormatException  on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	private static void assertMissingProperty(final ISPReader reader,
											final String xml,
											final Class<?> desideratum,
											final String property,
											final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			try (StringReader stringReader = new StringReader(xml)) {
				reader.readXML(FAKE_FILENAME, stringReader, desideratum,
						Warning.Ignore);
			}
			try (StringReader stringReader = new StringReader(xml)) {
				reader.readXML(FAKE_FILENAME, stringReader, desideratum,
						Warning.Die);
				fail("We were expecting a MissingParameterException");
			} catch (final FatalWarningException except) {
				final Throwable cause = except.getCause();
				assertThat("Missing property", cause,
						instanceOf(MissingPropertyException.class));
				assertThat("Missing property should be the one we're expecting",
						((MissingPropertyException) cause).getParam(), equalTo(property));
			}
		} else {
			try (StringReader stringReader = new StringReader(xml)) {
				reader.readXML(FAKE_FILENAME, stringReader, desideratum,
						Warning.Ignore);
			} catch (final MissingPropertyException except) {
				assertThat("Missing property should be the one we're expecting",
						except.getParam(), equalTo(property));
			}
		}
	}

	/**
	 * Assert that reading the given XML will give a DeprecatedPropertyException. If it's
	 * only supposed to be a warning, assert that it'll pass with warnings disabled but
	 * object with them made fatal. This version tests both old and new readers.
	 *
	 * @param xml         the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param deprecated  the deprecated property
	 * @param warning     whether this is supposed to be only a warning
	 * @throws SPFormatException  on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	protected final void assertDeprecatedProperty(final String xml,
											final Class<?> desideratum,
											final String deprecated,
											final boolean warning)
			throws XMLStreamException, SPFormatException {
		assertDeprecatedProperty(oldReader, xml, desideratum, deprecated,
				warning);
		assertDeprecatedProperty(newReader, xml, desideratum, deprecated,
				warning);
	}

	/**
	 * Assert that reading the given XML will give a DeprecatedPropertyException. If it's
	 * only supposed to be a warning, assert that it'll pass with warnings disabled but
	 * object with them made fatal.
	 *
	 * @param reader      the reader to do the reading
	 * @param xml         the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @param deprecated  the deprecated property
	 * @param warning     whether this is supposed to be only a warning
	 * @throws SPFormatException  on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	private static void assertDeprecatedProperty(final ISPReader reader,
												final String xml,
												final Class<?> desideratum,
												final String deprecated,
												final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			try (StringReader stringReader = new StringReader(xml)) {
				reader.readXML(FAKE_FILENAME, stringReader, desideratum,
						Warning.Ignore);
			}
			try (StringReader stringReader = new StringReader(xml)) {
				reader.readXML(FAKE_FILENAME, stringReader, desideratum,
						Warning.Die);
				fail("We were expecting a MissingParameterException");
			} catch (final FatalWarningException except) {
				final Throwable cause = except.getCause();
				assertThat("Missing property", cause,
						instanceOf(DeprecatedPropertyException.class));
				assertThat("Missing property should be one we're expecting",
						((DeprecatedPropertyException) cause).getOld(),
						equalTo(deprecated));
			}
		} else {
			try (StringReader stringReader = new StringReader(xml)) {
				reader.readXML(FAKE_FILENAME, stringReader, desideratum,
						Warning.Ignore);
			} catch (final DeprecatedPropertyException except) {
				assertThat("Missing property should be one we're expecting",
						except.getOld(), equalTo(deprecated));
			}
		}
	}

	/**
	 * Assert that the serialized form of the given object will deserialize without error
	 * using both old and new readers.
	 *
	 * @param message the message to use
	 * @param obj     the object to serialize
	 * @throws SPFormatException  on SP XML problem
	 * @throws XMLStreamException on XML reading problem
	 * @throws IOException        on I/O error creating serialized form
	 */
	protected final void assertSerialization(final String message, final Object obj)
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization(message, obj, Warning.Die);
	}

	/**
	 * Assert that the serialized form of the given object will deserialize without error
	 * using both the old and new readers.
	 *
	 * @param message the message to use
	 * @param obj     the object to serialize
	 * @param warning the warning instance to use
	 * @throws SPFormatException  on SP XML problem
	 * @throws XMLStreamException on XML reading problem
	 * @throws IOException        on I/O error creating serialized form
	 */
	protected final void assertSerialization(final String message, final Object obj,
											final Warning warning)
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization(message, oldReader, obj, warning);
		assertSerialization(message, newReader, obj, warning);
	}

	/**
	 * Assert that the serialized form of the given object will deserialize without error
	 * using both the reflection and non-reflection methods.
	 *
	 * @param message the message to use
	 * @param reader  the reader to parse the serialized form
	 * @param obj     the object to serialize
	 * @param warner  the warning instance to use
	 * @throws SPFormatException  on SP XML problem
	 * @throws XMLStreamException on XML reading problem
	 * @throws IOException        on I/O error creating serialized form
	 */
	private static void assertSerialization(final String message, final ISPReader reader,
											final Object obj, final Warning warner)
			throws XMLStreamException, SPFormatException, IOException {
		try (StringReader stringReader = new StringReader(createSerializedForm(obj, true))) {
			assertThat(message,
					reader.readXML(FAKE_FILENAME, stringReader, obj.getClass(), warner),
					equalTo(obj));
		}
		try (StringReader stringReader = new StringReader(createSerializedForm(obj, false))) {
			assertThat(message,
					reader.readXML(FAKE_FILENAME, stringReader, obj.getClass(), warner),
					equalTo(obj));
		}
	}

	/**
	 * Assert that a deprecated idiom deserializes properly if warnings are ignored, but
	 * is warned about.
	 *
	 * @param message  the message to pass to JUnit
	 * @param expected the object we expect the deserialized form to equal
	 * @param xml      the serialized form
	 * @param property the deprecated property
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML format error
	 */
	protected final void assertDeprecatedDeserialization(final String message,
														final Object expected,
														final String xml,
														final String property)
			throws XMLStreamException, SPFormatException {
		try (StringReader stringReader = new StringReader(xml)) {
			assertThat(message, oldReader.readXML(FAKE_FILENAME, stringReader,
					expected.getClass(), Warning.Ignore), equalTo(expected));
		}
		try (StringReader stringReader = new StringReader(xml)) {
			assertThat(message, newReader.readXML(FAKE_FILENAME, stringReader,
					expected.getClass(), Warning.Ignore), equalTo(expected));
		}
		assertDeprecatedProperty(xml, expected.getClass(), property, true);
	}

	/**
	 * Assert that a serialized form with a recommended but not required property missing
	 * deserializes properly if warnings are ignored, but is warned about.
	 *
	 * @param message  the message to pass to JUnit
	 * @param expected the object we expect the deserialized form to equal
	 * @param xml      the serialized form
	 * @param property the missing property
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML format error
	 */
	protected final void assertMissingPropertyDeserialization(final String message,
															final Object expected,
															final String xml,
															final String property)
			throws XMLStreamException, SPFormatException {
		try (StringReader stringReader = new StringReader(xml)) {
			assertThat(message, oldReader.readXML(FAKE_FILENAME, stringReader,
					expected.getClass(), Warning.Ignore), equalTo(expected));
		}
		try (StringReader stringReader = new StringReader(xml)) {
			assertThat(message, newReader.readXML(FAKE_FILENAME, stringReader,
					expected.getClass(), Warning.Ignore), equalTo(expected));
		}
		assertMissingProperty(xml, expected.getClass(), property, true);
	}

	/**
	 * Assert that a "forward idiom"---an idiom that we do not yet produce, but want to
	 * accept---will be deserialized properly by both readers, both with and without
	 * reflection.
	 *
	 * @param message  the message to pass to JUnit
	 * @param expected the object we expect the deserialized form to equal
	 * @param xml      the serialized form
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML format error
	 */
	protected final void assertForwardDeserialization(final String message,
													final Object expected, final String xml)
			throws XMLStreamException, SPFormatException {
		try (StringReader stringReader = new StringReader(xml)) {
			assertThat(message, oldReader.readXML(FAKE_FILENAME, stringReader,
					expected.getClass(), Warning.Die), equalTo(expected));
		}
		try (StringReader stringReader = new StringReader(xml)) {
			assertThat(message, newReader.readXML(FAKE_FILENAME, stringReader,
					expected.getClass(), Warning.Die), equalTo(expected));
		}
	}

	/**
	 * Assert that two deserialized forms are equivalent, using both readers, both with
	 * and
	 * without reflection.
	 *
	 * @param <T>          the type they'll deserialize to.
	 * @param message      the message to pass to JUnit
	 * @param firstForm    the first form
	 * @param secondForm   the second form
	 * @param type         the type
	 * @param warningLevel the warning level to set.
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML format error
	 */
	protected final <T> void assertEquivalentForms(final String message,
											final String firstForm,
											final String secondForm,
											final Class<T> type,
											final Warning warningLevel)
			throws SPFormatException, XMLStreamException {
		try (final StringReader firstReader = new StringReader(firstForm);
			 final StringReader secondReader = new StringReader(secondForm)) {
			assertThat(message,
					oldReader.readXML(FAKE_FILENAME, secondReader, type, warningLevel),
					equalTo(oldReader.readXML(FAKE_FILENAME, firstReader, type,
							warningLevel)));
		}
		try (final StringReader firstReader = new StringReader(firstForm);
			 final StringReader secondReader = new StringReader(secondForm)) {
			assertThat(message,
					newReader.readXML(FAKE_FILENAME, secondReader, type, warningLevel),
					equalTo(newReader.readXML(FAKE_FILENAME, firstReader, type,
							warningLevel)));
		}
	}

	/**
	 * @param obj        an object
	 * @param deprecated whether to use the deprecated XML-serialization idiom
	 * @return its serialized form
	 * @throws IOException on I/O error creating it
	 */
	@SuppressWarnings("deprecation")
	protected static String createSerializedForm(final Object obj,
												final boolean deprecated)
			throws IOException {
		final StringWriter writer = new StringWriter();
		if (deprecated) {
			CompactXMLWriter.writeSPObject(writer, obj);
		} else {
			new SPFluidWriter().writeSPObject(writer, obj);
		}
		return NullCleaner.assertNotNull(writer.toString());
	}

	/**
	 * Assert that reading the given XML will give a MissingChildException. If it's only
	 * supposed to be a warning, assert that it'll pass with warnings disabled but object
	 * with them made fatal. This version tests both old and new readers.
	 *
	 * @param xml         the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @throws SPFormatException  on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	protected final void assertMissingChild(final String xml,
									final Class<?> desideratum)
			throws XMLStreamException, SPFormatException {
		assertMissingChild(oldReader, xml, desideratum);
		assertMissingChild(newReader, xml, desideratum);
	}

	/**
	 * Assert that reading the given XML will give a MissingChildException. If it's only
	 * supposed to be a warning, assert that it'll pass with warnings disabled but object
	 * with them made fatal.
	 *
	 * @param reader      the reader to do the reading
	 * @param xml         the XML to read
	 * @param desideratum the class it would produce if it weren't erroneous
	 * @throws SPFormatException  on unexpected SP format error
	 * @throws XMLStreamException on XML format error
	 */
	private static void assertMissingChild(final ISPReader reader,
										final String xml, final Class<?> desideratum)
			throws XMLStreamException, SPFormatException {
		try {
			reader.readXML(FAKE_FILENAME, new StringReader(xml),
					desideratum, Warning.Ignore);
			fail("We were expecting a MissingChildException");
		} catch (final MissingChildException except) {
			LOGGER.log(Level.FINEST, "Got the expected MissingChildException",
					except);
		}
	}

	/**
	 * Assert that a map is properly deserialized (by the main map-deserialization
	 * methods) into a view.
	 *
	 * @param message  the message to use in JUnit calls
	 * @param expected the object to test against
	 * @param xml      the XML to deserialize it into.
	 * @throws SPFormatException  if map format too old or new for a reader, or on other
	 *                            SP format error
	 * @throws XMLStreamException on XML format error
	 */
	protected final void assertMapDeserialization(final String message,
											final IMapNG expected, final String xml)
			throws XMLStreamException, SPFormatException {
		try (final StringReader stringReader = new StringReader(xml)) {
			assertThat(message, ((IMapReader) oldReader)
										.readMap(FAKE_FILENAME, stringReader,
												Warning.Die), equalTo(expected));
		}
		try (final StringReader stringReader = new StringReader(xml)) {
			assertThat(message, ((IMapReader) newReader)
										.readMap(FAKE_FILENAME, stringReader,
												Warning.Die), equalTo(expected));
		}
	}

	/**
	 * Determine the size of an iterable. Note that its iterator will have been advanced
	 * to the end.
	 *
	 * @param iter an iterable
	 * @param <T>  the type of thing it contains
	 * @return the number of items in the iterable
	 */
	protected static <T> long iteratorSize(final Iterable<T> iter) {
		return StreamSupport.stream(iter.spliterator(), false).count();
	}

	/**
	 * Assert that the given object, if serialized and deserialized, will have its image
	 * property preserved. We modify its image property, but set it back to the original
	 * value before exiting the method.
	 *
	 * @param message the message to use for assertions
	 * @param obj     the object to serialize
	 * @throws SPFormatException  on SP XML problem
	 * @throws XMLStreamException on XML reading problem
	 * @throws IOException        on I/O error creating serialized form
	 */
	protected final void assertImageSerialization(final String message,
												final HasMutableImage obj)
			throws XMLStreamException, SPFormatException, IOException {
		final String origImage = obj.getImage();
		obj.setImage("imageForSerialization");
		assertImageSerialization(message, obj, oldReader);
		assertImageSerialization(message, obj, newReader);
		obj.setImage(obj.getDefaultImage());
		assertThat("Default image is not written", createSerializedForm(obj, true),
				not(containsString("image=")));
		assertThat("Default image is not written", createSerializedForm(obj, false),
				not(containsString("image=")));
		obj.setImage("");
		assertThat("Empty image is not written", createSerializedForm(obj, true),
				not(containsString("image=")));
		assertThat("Empty image is not written", createSerializedForm(obj, false),
				not(containsString("image=")));
		obj.setImage(origImage);
	}

	/**
	 * Assert that the given object, if serialized and deserialized, will have its portrait
	 * property preserved. We modify its image property, but set it back to the original
	 * value before exiting the method.
	 *
	 * @param message the message to use for assertions
	 * @param obj     the object to serialize
	 * @throws SPFormatException  on SP XML problem
	 * @throws XMLStreamException on XML reading problem
	 * @throws IOException        on I/O error creating serialized form
	 */
	protected final void assertPortraitSerialization(final String message,
												  final HasPortrait obj)
			throws XMLStreamException, SPFormatException, IOException {
		final String origImage = obj.getPortrait();
		obj.setPortrait("portraitForSerialization");
		assertPortraitSerialization(message, obj, oldReader);
		assertPortraitSerialization(message, obj, newReader);
		obj.setPortrait("");
		assertThat("Empty portrait is not written", createSerializedForm(obj, true),
				not(containsString("image=")));
		assertThat("Empty portrait is not written", createSerializedForm(obj, false),
				not(containsString("image=")));
		obj.setPortrait(origImage);
	}
	/**
	 * Assert that the given object, if serialized and deserialized, will have its image
	 * property preserved. We modify its image property, but set it back to the original
	 * value before exiting the method.
	 *
	 * @param message the message to use for assertions
	 * @param obj     the object to serialize
	 * @param reader  the reader to use
	 * @throws SPFormatException  on SP XML problem
	 * @throws XMLStreamException on XML reading problem
	 * @throws IOException        on I/O error creating serialized form
	 */
	private static void assertImageSerialization(final String message, final HasImage obj,
												final ISPReader reader)
			throws XMLStreamException, SPFormatException, IOException {
		try (final StringReader stringReader = new StringReader(createSerializedForm(obj,
				true))) {
			assertThat(message,
					reader.readXML(FAKE_FILENAME, stringReader, obj.getClass(),
							Warning.Ignore).getImage(), equalTo(obj.getImage()));
		}
		try (final StringReader stringReader = new StringReader(createSerializedForm(obj,
				false))) {
			assertThat(message,
					reader.readXML(FAKE_FILENAME, stringReader, obj.getClass(),
							Warning.Ignore).getImage(), equalTo(obj.getImage()));
		}
	}
	/**
	 * Assert that the given object, if serialized and deserialized, will have its portrait
	 * property preserved. We modify its image property, but set it back to the original
	 * value before exiting the method.
	 *
	 * @param message the message to use for assertions
	 * @param obj     the object to serialize
	 * @param reader  the reader to use
	 * @throws SPFormatException  on SP XML problem
	 * @throws XMLStreamException on XML reading problem
	 * @throws IOException        on I/O error creating serialized form
	 */
	private static void assertPortraitSerialization(final String message,
													final HasPortrait obj,
													final ISPReader reader)
			throws XMLStreamException, SPFormatException, IOException {
		try (final StringReader stringReader = new StringReader(createSerializedForm(obj,
				true))) {
			assertThat(message,
					reader.readXML(FAKE_FILENAME, stringReader, obj.getClass(),
							Warning.Ignore).getPortrait(), equalTo(obj.getPortrait()));
		}
		try (final StringReader stringReader = new StringReader(createSerializedForm(obj,
				false))) {
			assertThat(message,
					reader.readXML(FAKE_FILENAME, stringReader, obj.getClass(),
							Warning.Ignore).getPortrait(), equalTo(obj.getPortrait()));
		}
	}
	/**
	 * Assert that a "forward idiom"---an idiom that we do not yet produce, but want to
	 * accept---will be deserialized properly by both readers, both with and without
	 * reflection.
	 *
	 * @param xml      the serialized form
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML format error
	 */
	protected final void assertDuplicateID(final String xml)
			throws XMLStreamException, SPFormatException {
		try (StringReader stringReader = new StringReader(xml)) {
			oldReader.readXML(FAKE_FILENAME, stringReader, Object.class, Warning.Ignore);
		}
		try (StringReader stringReader = new StringReader(xml)) {
			newReader.readXML(FAKE_FILENAME, stringReader, Object.class, Warning.Ignore);
		}
		try (StringReader stringReader = new StringReader(xml)) {
			oldReader.readXML(FAKE_FILENAME, stringReader, Object.class, Warning.Die);
			fail("Old reader didn't warn about duplicate ID");
		} catch (final FatalWarningException except) {
			assertThat("Warning was about duplicate ID", except.getCause(), instanceOf(
					DuplicateIDException.class));
		}
		try (StringReader stringReader = new StringReader(xml)) {
			newReader.readXML(FAKE_FILENAME, stringReader, Object.class, Warning.Die);
			fail("New reader didn't warn about duplicate ID");
		} catch (final FatalWarningException except) {
			assertThat("Warning was about duplicate ID", except.getCause(), instanceOf(
					DuplicateIDException.class));
		}
	}
	/**
	 * Assert that a given piece of XML will fail with NoSuchElementException.
	 * @param xml the XML to check
	 * @throws XMLStreamException on unexpected reader failure
	 * @throws SPFormatException on unexpected reader objection
	 */
	protected final void assertInvalid(final String xml)
			throws XMLStreamException, SPFormatException {
		try (StringReader stringReader = new StringReader(xml)) {
			oldReader.readXML(FAKE_FILENAME, stringReader, Object.class, Warning.Ignore);
			fail("Old reader didn't object to invalid XML");
		} catch (final NoSuchElementException ignored) {
			// pass()
		}
		try (StringReader stringReader = new StringReader(xml)) {
			newReader.readXML(FAKE_FILENAME, stringReader, Object.class, Warning.Ignore);
			fail("Old reader didn't object to invalid XML");
		} catch (final NoSuchElementException ignored) {
			// pass()
		}
	}
}
