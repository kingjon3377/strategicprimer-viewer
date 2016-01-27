package controller.map.misc;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import model.map.Player;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for CLIHelper
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
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
public class TestCLIHelper {
	/**
	 * Test chooseFromList().
	 */
	@Test
	public void testChooseFromList() throws IOException {
		try (StringWriter out = new StringWriter()) {
			assertEquals("chooseFromList chooses the one specified by user", 0,
					new CLIHelper(new StringReader("0\n"), out).chooseFromList(
							Arrays.asList(new Player(1, "one"), new Player(2, "two")),
							"test desc", "none present", "prompt", false));
			assertEquals("chooseFromList prompted the user", "0: one\n1: two\nprompt",
					out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("chooseFromList chooses the one specified by user", 1,
					new CLIHelper(new StringReader("1\n"), out).chooseFromList(
							Arrays.asList(new Player(1, "one"), new Player(2, "two")),
							"test desc", "none present", "prompt", false));
			assertEquals("chooseFromList prompted the user", "0: one\n1: two\nprompt",
					out.toString());
		}
	}
}
