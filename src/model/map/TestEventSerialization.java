package model.map;

import controller.map.formatexceptions.SPFormatException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import model.map.fixtures.explorable.Battlefield;
import model.map.fixtures.explorable.Cave;
import model.map.fixtures.resources.MineralVein;
import org.junit.Test;

/**
 * A class to test the serialization of Events.
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
public final class TestEventSerialization extends BaseTestFixtureSerialization {
	/**
	 * Extracted constant.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Compiled pattern of it.
	 */
	private static final Pattern KIND_PATTERN = Pattern.compile(
			KIND_PROPERTY, Pattern.LITERAL);

	/**
	 * Test serialization of CaveEvents.
	 *
	 * TODO: Randomly generate ID numbers, to avoid 'magic number' warnings.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testCaveSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First CaveEvent serialization test, reflection",
				new Cave(10, 0));
		assertSerialization("Second CaveEvent serialization test, reflection",
				new Cave(30, 1));
		assertUnwantedChild("<cave dc=\"10\"><troll /></cave>", Cave.class,
				false);
		assertMissingProperty("<cave />", Cave.class, "dc", false);
		assertMissingProperty("<cave dc=\"10\" />", Cave.class, "id", true);
		assertImageSerialization("Cave image property is preserved", new Cave(20, 2));
	}

	/**
	 * Test serialization of MineralVeins.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testMineralSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First MineralEvent serialization test",
				new MineralVein("one", true, 10, 1));
		final HasMutableImage secondVein = new MineralVein("two", false, 35, 2);
		assertSerialization("Second MineralEvent serialization test", secondVein);
		final String oldKindProperty = "mineral";
		assertDeprecatedDeserialization(
				"Deserialization of deprecated Mineral idiom", secondVein,
				KIND_PATTERN.matcher(createSerializedForm(secondVein, true))
						.replaceAll(Matcher.quoteReplacement(oldKindProperty)),
				oldKindProperty);
		assertDeprecatedDeserialization(
				"Deserialization of deprecated Mineral idiom", secondVein,
				KIND_PATTERN.matcher(createSerializedForm(secondVein, false))
						.replaceAll(Matcher.quoteReplacement(oldKindProperty)),
				oldKindProperty);
		assertUnwantedChild(
				"<mineral kind=\"gold\" exposed=\"false\" dc=\"0\">"
						+ "<troll /></mineral>", MineralVein.class, false);
		assertMissingProperty("<mineral dc=\"0\" exposed=\"false\" />",
				MineralVein.class, KIND_PROPERTY, false);
		assertMissingProperty("<mineral kind=\"gold\" exposed=\"false\" />",
				MineralVein.class, "dc", false);
		assertMissingProperty("<mineral dc=\"0\" kind=\"gold\" />",
				MineralVein.class, "exposed", false);
		assertMissingProperty(
				"<mineral kind=\"kind\" exposed=\"true\" dc=\"0\" />",
				MineralVein.class, "id", true);
		assertImageSerialization("Mineral image property is preserved", secondVein);
	}

	/**
	 * First test of serialization of BattlefieldEvents.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testBattlefieldSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First BattlefieldEvent serialization test",
				new Battlefield(10, 0));
		assertSerialization("Second BattlefieldEvent serialization test",
				new Battlefield(30, 1));
		assertUnwantedChild("<battlefield dc=\"10\"><troll /></battlefield>",
				Battlefield.class, false);
		assertMissingProperty("<battlefield />", Battlefield.class, "dc", false);
		assertMissingProperty("<battlefield dc=\"10\" />", Battlefield.class,
				"id", true);
		assertImageSerialization("Battlefield image property is preserved",
				new Battlefield(20, 2));
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestEventSerialization";
	}
}
