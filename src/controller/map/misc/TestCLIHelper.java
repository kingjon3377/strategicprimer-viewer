package controller.map.misc;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import model.map.Player;
import org.junit.Test;
import util.NullCleaner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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
	 * @throws IOException on I/O error causing test failure
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testChooseFromList() throws IOException {
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("0\n"), out)) {
			assertThat("chooseFromList chooses the one specified by user",
					cli.chooseFromList(NullCleaner.assertNotNull(
							Arrays.asList(new Player(1, "one"), new Player(2, "two"))),
							"test desc", "none present", "prompt", false), equalTo(0));
			assertThat("chooseFromList prompted the user", out.toString(),
					equalTo("test desc\n0: one\n1: two\nprompt"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("1\n"), out)) {
			assertThat("chooseFromList chooses the one specified by user",
					cli.chooseFromList(NullCleaner.assertNotNull(
							Arrays.asList(new Player(1, "one"), new Player(2, "two"))),
							"test desc", "none present", "prompt", false), equalTo(1));
			assertThat("chooseFromList prompted the user", out.toString(),
					equalTo("test desc\n0: one\n1: two\nprompt"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader(""), out)) {
			assertThat(
					"chooseFromList chooses only choice when this is specified",
					cli.chooseFromList(
							NullCleaner.assertNotNull(Collections
									.singletonList(new Player(1, "one"))),
							"test desc", "none present", "prompt", true), equalTo(0));
			assertThat("chooseFromList automatically chose only choice",
					out.toString(),
					equalTo("test desc\nAutomatically choosing only item, one\n"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("0\n"), out)) {
			assertThat(
					"chooseFromList doesn't always auto-choose only choice",
					cli.chooseFromList(
							NullCleaner.assertNotNull(Collections
									.singletonList(new Player(1, "one"))),
							"test desc", "none present", "prompt", false), equalTo(0));
			assertThat(
					"chooseFromList didn't automatically chose only choice", out.toString(),
					equalTo("test desc\n0: one\nprompt"));
		}
		try (StringWriter out = new StringWriter(); ICLIHelper cli =
				new CLIHelper(new StringReader("-1\n0\n"), out)) {
			assertThat(
					"chooseFromList prompts again when negative index given",
					cli.chooseFromList(
							NullCleaner.assertNotNull(
									Arrays.asList(new Player(1, "one"),
											new Player(2, "two"))),
							"test desc", "none present", "prompt", false), equalTo(0));
			assertThat(
					"chooseFromList prompts again when negative index given", out.toString(),
					equalTo("test desc\n0: one\n1: two\npromptprompt"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("3\n"), out)) {
			assertThat("chooseFromList allows too-large choice", cli.chooseFromList(
					NullCleaner.assertNotNull(
							Arrays.asList(new Player(1, "one"),
									new Player(2, "two"))),
					"test desc", "none present", "prompt", false),
					equalTo(3));
			assertThat("chooseFromList allows too-large choice",
					out.toString(), equalTo("test desc\n0: one\n1: two\nprompt"));
		}
	}
	/**
	 * Test inputNumber().
	 * @throws IOException on I/O error causing test failure
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testInputNumber() throws IOException {
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("2\n"), out)) {
			assertThat("inputNumber works", cli.inputNumber("test prompt"), equalTo(2));
			assertThat("inputNumber uses given prompt", out.toString(),
					equalTo("test prompt"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("8\n"), out)) {
			assertThat("inputNumber works", cli.inputNumber("test prompt two"),
					equalTo(8));
			assertThat("inputNumber uses given prompt", out.toString(),
					equalTo("test prompt two"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("-1\n0\n"), out)) {
			assertThat("inputNumber asks again on negative input",
					cli.inputNumber("test prompt three"),
					equalTo(0));
			assertThat("inputNumber asks again on negative input",
					out.toString(), equalTo("test prompt threetest prompt three"));
		}
	}
	/**
	 * Test for inputString().
	 * @throws IOException on I/O error causing test failure
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testInputString() throws IOException {
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("first\n"), out)) {
			assertThat("inputString returns the inputted string",
					cli.inputString("string prompt"), equalTo("first"));
			assertThat("inputString displays prompt", out.toString(),
					equalTo("string prompt"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("second\n"), out)) {
			assertThat("inputString returns the inputted string",
					cli.inputString("second prompt"), equalTo("second"));
			assertThat("inputString displays prompt", out.toString(),
					equalTo("second prompt"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader(""), out)) {
			assertThat("inputString returns empty on EOF",
					cli.inputString("third prompt"), equalTo(""));
			assertThat("inputString displays prompt", out.toString(),
					equalTo("third prompt"));
		}
	}
	/**
	 * Test for inputBoolean().
	 * @throws IOException on I/O error causing test failure
	 */
	@SuppressWarnings({ "boxing", "static-method" })
	@Test
	public void testInputBoolean() throws IOException {
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("yes\n"), out)) {
			assertThat("inputBoolean returns true on 'yes'",
					cli.inputBoolean("bool prompt"), equalTo(true));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("bool prompt"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("true\n"), out)) {
			assertThat("inputBoolean returns true on 'true'",
					cli.inputBoolean("prompt two"), equalTo(true));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt two"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("y\n"), out)) {
			assertThat("inputBoolean returns true on 'y'",
					cli.inputBoolean("prompt three"), equalTo(true));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt three"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("t\n"), out)) {
			assertThat("inputBoolean returns true on 't'",
					cli.inputBoolean("prompt four"), equalTo(true));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt four"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("no\n"), out)) {
			assertThat("inputBoolean returns false on 'no'",
					cli.inputBoolean("prompt five"), equalTo(false));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt five"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("false\n"), out)) {
			assertThat("inputBoolean returns false on 'false'",
					cli.inputBoolean("prompt six"), equalTo(false));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt six"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("n\n"), out)) {
			assertThat("inputBoolean returns false on 'n'",
					cli.inputBoolean("prompt seven"), equalTo(false));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt seven"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("f\n"), out)) {
			assertThat("inputBoolean returns false on 'f'",
					cli.inputBoolean("prompt eight"), equalTo(false));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt eight"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("xyzzy\nyes\n"), out)) {
			assertThat("inputBoolean rejects other input",
					cli.inputBoolean("prompt nine"), equalTo(true));
			assertThat("inputBoolean gives message on invalid input", out.toString(),
					equalTo("prompt ninePlease enter 'yes', 'no', 'true', or 'false',\n" +
									"or the first character of any of those.\nprompt" +
									" nine"));
		}
	}
	/**
	 * Test chooseStringFromList().
	 * @throws IOException on I/O error causing test failure
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testStringChooseFromList() throws IOException {
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("0\n"), out)) {
			assertThat(
					"chooseStringFromList chooses the one specified by user", cli.chooseStringFromList(
							NullCleaner
									.assertNotNull(Arrays.asList("one", "two")),
							"test desc", "none present", "prompt", false),
					equalTo(0));
			assertThat("chooseStringFromList prompted the user",
					out.toString(), equalTo("test desc\n0: one\n1: two\nprompt"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("1\n"), out)) {
			assertThat("chooseStringFromList chooses the one specified by user",
					cli.chooseStringFromList(
							NullCleaner.assertNotNull(Arrays.asList("one", "two")),
							"test desc", "none present", "prompt", false), equalTo(1));
			assertThat("chooseStringFromList prompted the user",
					out.toString(), equalTo("test desc\n0: one\n1: two\nprompt"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader(""), out)) {
			assertThat("chooseStringFromList chooses only choice when told to",
					cli.chooseStringFromList(
							NullCleaner.assertNotNull(Collections.singletonList("one")),
							"test desc", "none present", "prompt", true), equalTo(0));
			assertThat("chooseStringFromList automatically chose only choice",
					out.toString(),
					equalTo("test desc\nAutomatically choosing only item, one\n"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("0\n"), out)) {
			assertThat("chooseStringFromList doesn't always auto-choose",
					cli.chooseStringFromList(
							NullCleaner.assertNotNull(Collections.singletonList("one")),
							"test desc", "none present", "prompt", false), equalTo(0));
			assertThat(
					"chooseStringFromList didn't automatically chose only choice",
					out.toString(), equalTo("test desc\n0: one\nprompt"));
		}
		try (StringWriter out = new StringWriter(); ICLIHelper cli =
				new CLIHelper(new StringReader("-1\n0\n"), out)) {
			assertThat(
					"chooseStringFromList prompts again when negative index given",
					cli.chooseStringFromList(
							NullCleaner
									.assertNotNull(Arrays.asList("one", "two")),
							"test desc", "none present", "prompt", false),
					equalTo(0));
			assertThat(
					"chooseStringFromList prompts again when negative index given",
					out.toString(), equalTo("test desc\n0: one\n1: two\npromptprompt"));
		}
		try (StringWriter out = new StringWriter();
				ICLIHelper cli = new CLIHelper(new StringReader("3\n"), out)) {
			assertThat("chooseStringFromList allows too-large choice",
					cli.chooseStringFromList(
							NullCleaner.assertNotNull(Arrays.asList("one", "two")),
							"test desc", "none present", "prompt", false), equalTo(3));
			assertThat("chooseStringFromList allows too-large choice",
					out.toString(), equalTo("test desc\n0: one\n1: two\nprompt"));
		}
	}
}
