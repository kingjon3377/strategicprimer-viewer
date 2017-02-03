package model.map.fixtures.resources;

import controller.map.formatexceptions.SPFormatException;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import model.map.BaseTestFixtureSerialization;
import model.map.HasMutableImage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * A class to test the serialization of StoneFixtures.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@RunWith(Parameterized.class)
public class TestStoneSerialization extends BaseTestFixtureSerialization {
	/**
	 * Extracted constant.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Compiled pattern of it.
	 */
	private static final Pattern KIND_PATTERN =
			Pattern.compile(KIND_PROPERTY, Pattern.LITERAL);
	/**
	 * The stone kind to use for this test.
	 */
	private final StoneKind kind;

	/**
	 * Constructor for parametrized test.
	 * @param stone the stone kind to use for this test
	 */
	public TestStoneSerialization(final StoneKind stone) {
		kind = stone;
	}

	/**
	 * Generate the test parameters.
	 * @return a collection of values to use for tests
	 */
	@Parameterized.Parameters
	public static Collection<Object[]> generateData() {
		return Stream.of(StoneKind.values()).map(stoneKind -> new Object[]{stoneKind})
					   .collect(Collectors.toList());
	}

	/**
	 * Test serialization of StoneDeposits.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testStoneSerialization() throws XMLStreamException,
														SPFormatException, IOException {
		assert kind != null;
		assertSerialization("First StoneDeposit test, kind: " + kind,
				new StoneDeposit(kind, 8, 1));
		assertSerialization("Second StoneDeposit test, kind: " + kind,
				new StoneDeposit(kind, 15, 2));
		assertImageSerialization("Stone image property is preserved",
				new StoneDeposit(kind, 10, 3));
	}

	/**
	 * Test that the old XML idiom ("stone" as the property for the kind of stone) is
	 * deprecated.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testOldIdiomDeprecated()
			throws XMLStreamException, SPFormatException, IOException {
		final HasMutableImage thirdDeposit = new StoneDeposit(kind, 10, 3);
		final String oldKindProperty = "stone";
		assertDeprecatedDeserialization(
				"Deserialization of deprecated stone idiom", thirdDeposit,
				KIND_PATTERN.matcher(createSerializedForm(thirdDeposit, true))
						.replaceAll(Matcher.quoteReplacement(oldKindProperty)),
				oldKindProperty, KIND_PROPERTY, "stone");
		assertDeprecatedDeserialization(
				"Deserialization of deprecated stone idiom", thirdDeposit,
				KIND_PATTERN.matcher(createSerializedForm(thirdDeposit, false))
						.replaceAll(Matcher.quoteReplacement(oldKindProperty)),
				oldKindProperty, KIND_PROPERTY, "stone");
	}

	/**
	 * Test that the required properties are actually required.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testStoneSerializationErrors()
			throws SPFormatException, IOException, XMLStreamException {
		assertUnwantedChild(
				"<stone kind=\"" + kind + "\" dc=\"10\"><troll /></stone>",
				StoneDeposit.class, false);
		assertMissingProperty("<stone kind=\"" + kind + "\" />", StoneDeposit.class,
				"dc", false);
		assertMissingProperty("<stone dc=\"10\" />", StoneDeposit.class,
				KIND_PROPERTY, false);
		assertMissingProperty("<stone kind=\"" + kind + "\" dc=\"0\" />",
				StoneDeposit.class, "id", true);
	}

}
