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
			assertEquals("chooseFromList prompted the user",
					"test desc\n0: one\n1: two\nprompt", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("chooseFromList chooses the one specified by user", 1,
					new CLIHelper(new StringReader("1\n"), out).chooseFromList(
							Arrays.asList(new Player(1, "one"), new Player(2, "two")),
							"test desc", "none present", "prompt", false));
			assertEquals("chooseFromList prompted the user",
					"test desc\n0: one\n1: two\nprompt", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("chooseFromList chooses only choice when this is specified", 0,
					new CLIHelper(new StringReader(""), out)
							.chooseFromList(Arrays.asList(new Player(1, "one")),
									"test desc", "none present", "prompt", true));
			assertEquals("chooseFromList automatically chose only choice",
					"test desc\nAutomatically choosing only item, one\n", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("chooseFromList doesn't always automatically choose only choice", 0,
					new CLIHelper(new StringReader("0\n"), out)
							.chooseFromList(Arrays.asList(new Player(1, "one")),
									"test desc", "none present", "prompt", false));
			assertEquals("chooseFromList didn't automatically chose only choice",
					"test desc\n0: one\nprompt", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("chooseFromList prompts again when negative index given", 0,
					new CLIHelper(new StringReader("-1\n0\n"), out).chooseFromList(
							Arrays.asList(new Player(1, "one"), new Player(2, "two")),
							"test desc", "none present", "prompt", false));
			assertEquals("chooseFromList prompts again when negative index given",
					"test desc\n0: one\n1: two\npromptprompt", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("chooseFromList allows too-large choice", 3,
					new CLIHelper(new StringReader("3\n"), out).chooseFromList(
							Arrays.asList(new Player(1, "one"), new Player(2, "two")),
							"test desc", "none present", "prompt", false));
			assertEquals("chooseFromList allows too-large choice",
					"test desc\n0: one\n1: two\nprompt", out.toString());
		}
	}
	/**
	 * Test inputNumber().
	 */
	@Test
	public void testInputNumber() throws IOException {
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputNumber works", 2,
					new CLIHelper(new StringReader("2\n"), out)
							.inputNumber("test prompt"));
			assertEquals("inputNumber uses given prompt", "test prompt", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputNumber works", 8,
					new CLIHelper(new StringReader("8\n"), out)
							.inputNumber("test prompt two"));
			assertEquals("inputNumber uses given prompt", "test prompt two",
					out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputNumber asks again on negative input", 0,
					new CLIHelper(new StringReader("-1\n0\n"), out)
							.inputNumber("test prompt three"));
			assertEquals("inputNumber asks again on negative input",
					"test prompt threetest prompt three", out.toString());
		}
	}
	/**
	 * Test for inputString().
	 */
	@Test
	public void testInputString() throws IOException {
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputString returns the inputted string", "first",
					new CLIHelper(new StringReader("first\n"), out)
							.inputString("string prompt"));
			assertEquals("inputString displays prompt", "string prompt", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputString returns the inputted string", "second",
					new CLIHelper(new StringReader("second\n"), out)
							.inputString("second prompt"));
			assertEquals("inputString displays prompt", "second prompt", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputString returns empty on EOF", "",
					new CLIHelper(new StringReader(""), out)
							.inputString("third prompt"));
			assertEquals("inputString displays prompt", "third prompt", out.toString());
		}
	}
	/**
	 * Test for inputBoolean().
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testInputBoolean() throws IOException {
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputBoolean returns true on 'yes'", true,
					new CLIHelper(new StringReader("yes\n"), out)
							.inputBoolean("bool prompt"));
			assertEquals("inputBoolean displays prompt", "bool prompt", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputBoolean returns true on 'true'", true,
					new CLIHelper(new StringReader("true\n"), out)
							.inputBoolean("prompt two"));
			assertEquals("inputBoolean displays prompt", "prompt two", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputBoolean returns true on 'y'", true,
					new CLIHelper(new StringReader("y\n"), out)
							.inputBoolean("prompt three"));
			assertEquals("inputBoolean displays prompt", "prompt three", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputBoolean returns true on 't'", true,
					new CLIHelper(new StringReader("t\n"), out)
							.inputBoolean("prompt four"));
			assertEquals("inputBoolean displays prompt", "prompt four", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputBoolean returns false on 'no'", false,
					new CLIHelper(new StringReader("no\n"), out)
							.inputBoolean("prompt five"));
			assertEquals("inputBoolean displays prompt", "prompt five", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputBoolean returns false on 'false'", false,
					new CLIHelper(new StringReader("false\n"), out)
							.inputBoolean("prompt six"));
			assertEquals("inputBoolean displays prompt", "prompt six", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputBoolean returns false on 'n'", false,
					new CLIHelper(new StringReader("n\n"), out)
							.inputBoolean("prompt seven"));
			assertEquals("inputBoolean displays prompt", "prompt seven", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputBoolean returns false on 'f'", false,
					new CLIHelper(new StringReader("f\n"), out)
							.inputBoolean("prompt eight"));
			assertEquals("inputBoolean displays prompt", "prompt eight", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("inputBoolean rejects other input", true,
					new CLIHelper(new StringReader("xyzzy\nyes\n"), out)
							.inputBoolean("prompt nine"));
			assertEquals("inputBoolean gives message on invalid input",
					"prompt ninePlease enter 'yes', 'no', 'true', or 'false',\nor the " +
							"first character of any of those.\nprompt nine",
					out.toString());
		}
	}
	/**
	 * Test chooseStringFromList().
	 */
	@Test
	public void testStringChooseFromList() throws IOException {
		try (StringWriter out = new StringWriter()) {
			assertEquals("chooseStringFromList chooses the one specified by user", 0,
					new CLIHelper(new StringReader("0\n"), out)
							.chooseStringFromList(Arrays.asList("one", "two"),
									"test desc", "none present", "prompt", false));
			assertEquals("chooseStringFromList prompted the user",
					"test desc\n0: one\n1: two\nprompt", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("chooseStringFromList chooses the one specified by user", 1,
					new CLIHelper(new StringReader("1\n"), out)
							.chooseStringFromList(Arrays.asList("one", "two"),
									"test desc", "none present", "prompt", false));
			assertEquals("chooseStringFromList prompted the user",
					"test desc\n0: one\n1: two\nprompt", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals(
					"chooseStringFromList chooses only choice when this is specified", 0,
					new CLIHelper(new StringReader(""), out)
							.chooseStringFromList(Arrays.asList("one"), "test desc",
									"none present", "prompt", true));
			assertEquals("chooseStringFromList automatically chose only choice",
					"test desc\nAutomatically choosing only item, one\n", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals(
					"chooseStringFromList doesn't always automatically choose only " +
							"choice",
					0, new CLIHelper(new StringReader("0\n"), out)
							   .chooseStringFromList(Arrays.asList("one"), "test desc",
									   "none present", "prompt", false));
			assertEquals("chooseStringFromList didn't automatically chose only choice",
					"test desc\n0: one\nprompt", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("chooseStringFromList prompts again when negative index given",
					0, new CLIHelper(new StringReader("-1\n0\n"), out)
							   .chooseStringFromList(Arrays.asList("one", "two"),
									   "test desc", "none present", "prompt", false));
			assertEquals("chooseStringFromList prompts again when negative index given",
					"test desc\n0: one\n1: two\npromptprompt", out.toString());
		}
		try (StringWriter out = new StringWriter()) {
			assertEquals("chooseStringFromList allows too-large choice", 3,
					new CLIHelper(new StringReader("3\n"), out)
							.chooseStringFromList(Arrays.asList("one", "two"),
									"test desc", "none present", "prompt", false));
			assertEquals("chooseStringFromList allows too-large choice",
					"test desc\n0: one\n1: two\nprompt", out.toString());
		}
	}
}
