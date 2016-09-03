package controller.map.misc;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * TODO: explain this class
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class TestFileHandling {
	/**
	 * Test that MapReaderAdapter's filename-handling code works.
	 */
	@Test
	public void testNamesToFiles() {
		final Path[] expectedOne = new Path[]{Paths.get("two"), Paths.get("three"), Paths.get("four")};
		assertThat("Returns all names when dropFirst is false",
				MapReaderAdapter.namesToFiles(false, "two", "three", "four"),
				equalTo(expectedOne));
		assertThat("Drops first name when dropFirst is true",
				MapReaderAdapter.namesToFiles(true, "one", "two", "three", "four"),
				equalTo(expectedOne));
	}
}
