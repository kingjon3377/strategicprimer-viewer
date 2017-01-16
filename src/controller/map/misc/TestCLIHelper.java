package controller.map.misc;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import model.map.Player;
import model.map.PointFactory;
import org.junit.Test;
import util.NullStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for CLIHelper.
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
public class TestCLIHelper {
	/**
	 * Test chooseFromList().
	 *
	 * @throws IOException on I/O error causing test failure
	 */
	@Test
	public void testChooseFromList() throws IOException {
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("0%n")),
												   out)) {
			assertThat("chooseFromList chooses the one specified by user",
					Integer.valueOf(cli.chooseFromList(Arrays.asList(new Player(1, "one"),
							new Player(2, "two")),
							"test desc", "none present", " prompt", false)),
					equalTo(Integer.valueOf(0)));
			assertThat("chooseFromList prompted the user", out.toString(),
					equalTo(String.format("test desc%n0: one%n1: two%n prompt")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("1%n")),
												   out)) {
			assertThat("chooseFromList chooses the one specified by user",
					Integer.valueOf(
							cli.chooseFromList(Arrays.asList(new Player(1, "one"),
									new Player(2, "two")),
									"test desc", "none present", " prompt", false)),
					equalTo(Integer.valueOf(1)));
			assertThat("chooseFromList prompted the user", out.toString(),
					equalTo(String.format("test desc%n0: one%n1: two%n prompt")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(""), out)) {
			assertThat(
					"chooseFromList chooses only choice when this is specified",
					Integer.valueOf(cli.chooseFromList(
							Collections.singletonList(new Player(1, "one")),
							"test desc", "none present", "prompt", true)),
					equalTo(Integer.valueOf(0)));
			assertThat("chooseFromList automatically chose only choice",
					out.toString(),
					equalTo(String.format(
							"test desc%nAutomatically choosing only item, one%n")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("0%n")),
												   out)) {
			assertThat(
					"chooseFromList doesn't always auto-choose only choice",
					Integer.valueOf(cli.chooseFromList(
							Collections.singletonList(new Player(1, "one")),
							"test desc", "none present", " prompt", false)),
					equalTo(Integer.valueOf(0)));
			assertThat(
					"chooseFromList didn't automatically chose only choice",
					out.toString(),
					equalTo(String.format("test desc%n0: one%n prompt")));
		}
	}
	/**
	 * A second test of chooseFromList().
	 *
	 * @throws IOException on I/O error causing test failure
	 */
	@Test
	public void testChooseFromListMore() throws IOException {
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("-1%n0%n")),
												   out)) {
			assertThat(
					"chooseFromList prompts again when negative index given",
					Integer.valueOf(cli.chooseFromList(
							Arrays.asList(new Player(1, "one"),
									new Player(2, "two")),
							"test desc", "none present", " prompt", false)),
					equalTo(Integer.valueOf(0)));
			assertThat(
					"chooseFromList prompts again when negative index given",
					out.toString(),
					equalTo(String.format("test desc%n0: one%n1: two%n prompt prompt")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("3%n")),
												   out)) {
			assertThat("chooseFromList allows too-large choice",
					Integer.valueOf(cli.chooseFromList(
							Arrays.asList(new Player(1, "one"),
									new Player(2, "two")),
							"test desc", "none present", " prompt", false)),
					equalTo(Integer.valueOf(3)));
			assertThat("chooseFromList allows too-large choice",
					out.toString(),
					equalTo(String.format("test desc%n0: one%n1: two%n prompt")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("0%n")),
												   out)) {
			assertThat("chooseFromList asks even if 'auto' when multiple items",
					Integer.valueOf(
							cli.chooseFromList(Arrays.asList(new Player(1, "one"),
									new Player(2, "two")),
									"test desc", "none present", " prompt", true)),
					equalTo(Integer.valueOf(0)));
			assertThat("chooseFromList prompted the user", out.toString(),
					equalTo(String.format("test desc%n0: one%n1: two%n prompt")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(""), out)) {
			assertThat("chooseFromList handles no-item case",
					Integer.valueOf(
							cli.chooseFromList(Collections.emptyList(),
									"test desc", "none present", "prompt", false)),
					equalTo(Integer.valueOf(-1)));
			assertThat("chooseFromList didn't prompt the user", out.toString(),
					equalTo(String.format("none present%n")));
		}
	}

	/**
	 * Test inputNumber().
	 *
	 * @throws IOException on I/O error causing test failure
	 */
	@Test
	public void testInputNumber() throws IOException {
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("2%n")),
												   out)) {
			assertThat("inputNumber works",
					Integer.valueOf(cli.inputNumber("test prompt")),
					equalTo(Integer.valueOf(2)));
			assertThat("inputNumber uses given prompt", out.toString(),
					equalTo("test prompt"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("8%n")),
												   out)) {
			assertThat("inputNumber works",
					Integer.valueOf(cli.inputNumber("test prompt two")),
					equalTo(Integer.valueOf(8)));
			assertThat("inputNumber uses given prompt", out.toString(),
					equalTo("test prompt two"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("-1%n0%n")),
												   out)) {
			assertThat("inputNumber asks again on negative input",
					Integer.valueOf(cli.inputNumber("test prompt three ")),
					equalTo(Integer.valueOf(0)));
			assertThat("inputNumber asks again on negative input",
					out.toString(), equalTo("test prompt three test prompt three "));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format(
					 "not-number%n0%n")), out)) {
			assertThat("inputNumber asks again on non-numeric input",
					Integer.valueOf(cli.inputNumber(" test prompt four")),
					equalTo(Integer.valueOf(0)));
			assertThat("inputNumber asks again on negative input",
					out.toString(), equalTo(" test prompt four test prompt four"));
		}
	}

	/**
	 * Test inputDecimal().
	 *
	 * @throws IOException on I/O error causing test failure
	 */
	@Test
	public void testInputDecimal() throws IOException {
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("10%n")),
												   out)) {
			assertThat("inputDecimal works with integers",
					cli.inputDecimal("test prompt"), equalTo(BigDecimal.TEN));
			assertThat("inputDecimal uses given prompt", out.toString(),
					equalTo("test prompt"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("2.5%n")),
												   out)) {
			assertThat("inputDecimal works with decimals",
					cli.inputDecimal("test prompt two"),
					equalTo(new BigDecimal(5).divide(new BigDecimal(2))));
			assertThat("inputDecimal uses given prompt", out.toString(),
					equalTo("test prompt two"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format(
					 "-2.5%n0.5%n")), out)) {
			assertThat("inputDecimal asks again on negative input",
					cli.inputDecimal("test prompt three "),
					equalTo(BigDecimal.ONE.divide(new BigDecimal(2))));
			assertThat("inputDecimal asks again on negative input", out.toString(),
					equalTo("test prompt three test prompt three "));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format(
					 "non-number%n.1%n")), out)) {
			assertThat("inputDecimal asks again on non-numeric input",
					cli.inputDecimal(" test prompt four "),
					equalTo(BigDecimal.ONE.divide(BigDecimal.TEN)));
			assertThat("inputDecimal asks again on non-numeric input", out.toString(),
					equalTo(String.format(
							" test prompt four Invalid number.%n test prompt four ")));
		}
	}

	/**
	 * Test that inputNumber() bails out on EOF.
	 *
	 * @throws IOException never
	 */
	@Test(expected = IOException.class)
	public void testInputNumberEOF() throws IOException {
		try (ICLIHelper cli = new CLIHelper(new StringReader(""),
												   new OutputStreamWriter(new NullStream
																				  ()))) {
			cli.inputNumber("test prompt");
		}
	}

	/**
	 * Test for inputString().
	 *
	 * @throws IOException on I/O error causing test failure
	 */
	@Test
	public void testInputString() throws IOException {
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("first%n")),
												   out)) {
			assertThat("inputString returns the inputted string",
					cli.inputString("string prompt"), equalTo("first"));
			assertThat("inputString displays prompt", out.toString(),
					equalTo("string prompt"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("second%n")),
												   out)) {
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
	 *
	 * @throws IOException on I/O error causing test failure
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testInputBoolean() throws IOException {
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("yes%n")),
												   out)) {
			assertThat("inputBoolean returns true on 'yes'",
					cli.inputBoolean("bool prompt"), equalTo(true));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("bool prompt"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("true%n")),
												   out)) {
			assertThat("inputBoolean returns true on 'true'",
					cli.inputBoolean("prompt two"), equalTo(true));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt two"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("y%n")),
												   out)) {
			assertThat("inputBoolean returns true on 'y'",
					cli.inputBoolean("prompt three"), equalTo(true));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt three"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("t%n")),
												   out)) {
			assertThat("inputBoolean returns true on 't'",
					cli.inputBoolean("prompt four"), equalTo(true));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt four"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("no%n")),
												   out)) {
			assertThat("inputBoolean returns false on 'no'",
					cli.inputBoolean("prompt five"), equalTo(false));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt five"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("false%n")),
												   out)) {
			assertThat("inputBoolean returns false on 'false'",
					cli.inputBoolean("prompt six"), equalTo(false));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt six"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("n%n")),
												   out)) {
			assertThat("inputBoolean returns false on 'n'",
					cli.inputBoolean("prompt seven"), equalTo(false));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt seven"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("f%n")),
												   out)) {
			assertThat("inputBoolean returns false on 'f'",
					cli.inputBoolean("prompt eight"), equalTo(false));
			assertThat("inputBoolean displays prompt", out.toString(),
					equalTo("prompt eight"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format(
					 "xyzzy%nyes%n")), out)) {
			assertThat("inputBoolean rejects other input",
					cli.inputBoolean(" prompt nine"), equalTo(true));
			assertThat("inputBoolean gives message on invalid input", out.toString(),
					equalTo(String.format(
							" prompt ninePlease enter 'yes', 'no', 'true', or 'false'," +
									"%n" +
									"or the first character of any of those.%n prompt" +
									" nine")));
		}
	}

	/**
	 * Test the input-boolean-with-skipping functionality.
	 *
	 * @throws IOException on I/O error causing test failure
	 */
	@Test
	public void testInputBooleanInSeries() throws IOException {
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("yes%n")),
												   out)) {
			assertThat("inputBooleanInSeries returns true on 'yes'",
					cli.inputBooleanInSeries("bool prompt"), equalTo(true));
			assertThat("inputBooleanInSeries displays prompt", out.toString(),
					equalTo("bool prompt"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("true%n")),
												   out)) {
			assertThat("inputBooleanInSeries returns true on 'true'",
					cli.inputBooleanInSeries("prompt two"), equalTo(true));
			assertThat("inputBooleanInSeries displays prompt", out.toString(),
					equalTo("prompt two"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("y%n")),
												   out)) {
			assertThat("inputBooleanInSeries returns true on 'y'",
					cli.inputBooleanInSeries("prompt three"), equalTo(true));
			assertThat("inputBooleanInSeries displays prompt", out.toString(),
					equalTo("prompt three"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("t%n")),
												   out)) {
			assertThat("inputBooleanInSeries returns true on 't'",
					cli.inputBooleanInSeries("prompt four"), equalTo(true));
			assertThat("inputBooleanInSeries displays prompt", out.toString(),
					equalTo("prompt four"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("no%n")),
												   out)) {
			assertThat("inputBooleanInSeries returns false on 'no'",
					cli.inputBooleanInSeries("prompt five"), equalTo(false));
			assertThat("inputBooleanInSeries displays prompt", out.toString(),
					equalTo("prompt five"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("false%n")),
												   out)) {
			assertThat("inputBooleanInSeries returns false on 'false'",
					cli.inputBooleanInSeries("prompt six"), equalTo(false));
			assertThat("inputBooleanInSeries displays prompt", out.toString(),
					equalTo("prompt six"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("n%n")),
												   out)) {
			assertThat("inputBooleanInSeries returns false on 'n'",
					cli.inputBooleanInSeries("prompt seven"), equalTo(false));
			assertThat("inputBooleanInSeries displays prompt", out.toString(),
					equalTo("prompt seven"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("f%n")),
												   out)) {
			assertThat("inputBooleanInSeries returns false on 'f'",
					cli.inputBooleanInSeries("prompt eight"), equalTo(false));
			assertThat("inputBooleanInSeries displays prompt", out.toString(),
					equalTo("prompt eight"));
		}
	}
	/**
	 * Another test of the input-boolean-with-skipping functionality.
	 *
	 * @throws IOException on I/O error causing test failure
	 */
	@Test
	public void testInputBooleanInSeriesMore() throws IOException {
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format(
					 "xyzzy%nyes%n")), out)) {
			assertThat("inputBooleanInSeries rejects other input",
					cli.inputBooleanInSeries(" prompt nine"), equalTo(true));
			assertThat("inputBooleanInSeries gives message on invalid input",
					out.toString(),
					equalTo(String.format(
							" prompt ninePlease enter 'yes', 'no', 'true', or 'false', " +
									"the first%ncharacter of any of those, or 'all', " +
									"'none', 'always'%nor 'never' to use the same " +
									"answer" +
									" for all further questions%n prompt nine")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("all%n")),
												   out)) {
			assertThat("inputBooleanInSeries allows yes-to-all",
					cli.inputBooleanInSeries("prompt ten "), equalTo(true));
			assertThat("inputBooleanInSeries honors yes-to-all when prompt is the same",
					cli.inputBooleanInSeries("prompt ten "), equalTo(true));
			assertThat("inputBooleanInSeries shows automatic yes", out.toString(),
					equalTo(String.format("prompt ten prompt ten yes%n")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("none%n")),
												   out)) {
			assertThat("inputBooleanInSeries allows no-to-all",
					cli.inputBooleanInSeries("prompt eleven "), equalTo(false));
			assertThat("inputBooleanInSeries honors no-to-all when prompt is the same",
					cli.inputBooleanInSeries("prompt eleven "), equalTo(false));
			assertThat("inputBooleanInSeries shows automatic no", out.toString(),
					equalTo(String.format("prompt eleven prompt eleven no%n")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("all%n")),
												   out)) {
			assertThat("inputBooleanInSeries allows yes-to-all",
					cli.inputBooleanInSeries("prompt twelve ", "key"), equalTo(true));
			assertThat(
					"inputBooleanInSeries honors yes-to-all if prompt differs, same key",
					cli.inputBooleanInSeries("prompt thirteen ", "key"), equalTo(true));
			assertThat("inputBooleanInSeries shows automatic yes", out.toString(),
					equalTo(String.format("prompt twelve prompt thirteen yes%n")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("none%n")),
												   out)) {
			assertThat("inputBooleanInSeries allows no-to-all",
					cli.inputBooleanInSeries("prompt fourteen ", "secondKey"),
					equalTo(false));
			assertThat(
					"inputBooleanInSeries honors no-to-all if prompt differs, same key",
					cli.inputBooleanInSeries("prompt fifteen ", "secondKey"),
					equalTo(false));
			assertThat("inputBooleanInSeries shows automatic no", out.toString(),
					equalTo(String.format("prompt fourteen prompt fifteen no%n")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format(
					 "all%nnone%n")), out)) {
			assertThat("inputBooleanInSeries allows yes-to-all with one key",
					cli.inputBooleanInSeries("prompt sixteen ", "thirdKey"),
					equalTo(true));
			assertThat("inputBooleanInSeries allows no-to-all with second key",
					cli.inputBooleanInSeries("prompt seventeen ", "fourthKey"),
					equalTo(false));
			assertThat("inputBooleanInSeries then honors yes-to-all",
					cli.inputBooleanInSeries("prompt eighteen ", "thirdKey"),
					equalTo(true));
			assertThat("inputBooleanInSeries then honors no-to-all",
					cli.inputBooleanInSeries(" prompt nineteen ", "fourthKey"),
					equalTo(false));
			assertThat("inputBooleanInSeries shows prompts", out.toString(),
					equalTo(String.format(
							"prompt sixteen prompt seventeen prompt eighteen yes%n " +
									"prompt nineteen no%n")));
		}
	}

	/**
	 * Test chooseStringFromList().
	 *
	 * @throws IOException on I/O error causing test failure
	 */
	@Test
	public void testStringChooseFromList() throws IOException {
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("0%n")),
												   out)) {
			assertThat("chooseStringFromList chooses the one specified by user",
					Integer.valueOf(cli.chooseStringFromList(
							Arrays.asList("one", "two"),
							"test desc", "none present", "prompt", false)),
					equalTo(Integer.valueOf(0)));
			assertThat("chooseStringFromList prompted the user",
					out.toString(),
					equalTo(String.format("test desc%n0: one%n1: two%nprompt")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("1%n")),
												   out)) {
			assertThat("chooseStringFromList chooses the one specified by user",
					Integer.valueOf(cli.chooseStringFromList(
							Arrays.asList("one", "two"),
							"test desc", "none present", "prompt", false)),
					equalTo(Integer.valueOf(1)));
			assertThat("chooseStringFromList prompted the user",
					out.toString(),
					equalTo(String.format("test desc%n0: one%n1: two%nprompt")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(""), out)) {
			assertThat("chooseStringFromList chooses only choice when told to",
					Integer.valueOf(cli.chooseStringFromList(
							Collections.singletonList("one"),
							"test desc", "none present", "prompt", true)),
					equalTo(Integer.valueOf(0)));
			assertThat("chooseStringFromList automatically chose only choice",
					out.toString(),
					equalTo(String.format(
							"test desc%nAutomatically choosing only item, one%n")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("0%n")),
												   out)) {
			assertThat("chooseStringFromList doesn't always auto-choose",
					Integer.valueOf(cli.chooseStringFromList(
							Collections.singletonList("one"),
							"test desc", "none present", "prompt", false)),
					equalTo(Integer.valueOf(0)));
			assertThat(
					"chooseStringFromList didn't automatically chose only choice",
					out.toString(), equalTo(String.format("test desc%n0: one%nprompt")));
		}
	}

	/**
	 * A second test of chooseStringFromList().
	 *
	 * @throws IOException on I/O error causing test failure
	 */
	@Test
	public void testStringChooseFromListMore() throws IOException {
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("1%n")),
												   out)) {
			assertThat("chooseStringFromList doesn't always auto-choose",
					Integer.valueOf(cli.chooseStringFromList(
							Arrays.asList("zero", "one", "two"),
							"test desc", "none present", "prompt", true)),
					equalTo(Integer.valueOf(1)));
			assertThat(
					"chooseStringFromList didn't automatically chose only choice",
					out.toString(),
					equalTo(String.format("test desc%n0: zero%n1: one%n2: " +
												  "two%nprompt")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("-1%n0%n")),
												   out)) {
			assertThat(
					"chooseStringFromList prompts again when negative index given",
					Integer.valueOf(cli.chooseStringFromList(
							Arrays.asList("one", "two"),
							"test desc", "none present", "prompt", false)),
					equalTo(Integer.valueOf(0)));
			assertThat(
					"chooseStringFromList prompts again when negative index given",
					out.toString(),
					equalTo(String.format("test desc%n0: one%n1: two%npromptprompt")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("3%n")),
												   out)) {
			assertThat("chooseStringFromList allows too-large choice",
					Integer.valueOf(cli.chooseStringFromList(
							Arrays.asList("one", "two"),
							"test desc", "none present", "prompt", false)),
					equalTo(Integer.valueOf(3)));
			assertThat("chooseStringFromList allows too-large choice",
					out.toString(),
					equalTo(String.format("test desc%n0: one%n1: two%nprompt")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(""), out)) {
			assertThat("chooseStringFromList handles empty list",
					Integer.valueOf(
							cli.chooseStringFromList(Collections.emptyList(),
									"test desc", "none present", "prompt", false)),
					equalTo(Integer.valueOf(-1)));
			assertThat("chooseStringFromList handles empty list", out.toString(),
					equalTo(String.format("none present%n")));
		}
	}

	/**
	 * Test print() and friends.
	 *
	 * @throws IOException never
	 */
	@Test
	public void testPrinting() throws IOException {
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(""), out)) {
			cli.print("test string");
			assertThat("print() prints string", out.toString(), equalTo("test string"));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(""), out)) {
			cli.println("test two");
			assertThat("println() adds newline", out.toString(),
					equalTo(String.format("test two%n")));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(""), out)) {
			cli.printf("test %s", "three");
			assertThat("printf() works", out.toString(), equalTo("test three"));
		}
	}

	/**
	 * Test inputPoint().
	 *
	 * @throws IOException never
	 */
	@Test
	public void testInputPoint() throws IOException {
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format("2%n3%n")),
												   out)) {
			assertThat("reads row then column", cli.inputPoint("point prompt one"),
					equalTo(PointFactory.point(2, 3)));
			assertThat("prompts as expected", "point prompt oneRow: Column: ",
					equalTo(out.toString()));
		}
		try (StringWriter out = new StringWriter();
			 ICLIHelper cli = new CLIHelper(new StringReader(String.format(
					 "-1%n0%n-1%n4%n")), out)) {
			assertThat("doesn't accept negative row or column",
					cli.inputPoint("point prompt two"),
					equalTo(PointFactory.point(0, 4)));
			assertThat("prompts as expected",
					"point prompt twoRow: Row: Column: Column: ",
					equalTo(out.toString()));
		}
	}
}
