package drivers.common.cli;

import org.eclipse.jdt.annotation.Nullable;
import org.javatuples.Pair;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Collections;

import legacy.map.PlayerImpl;
import legacy.map.Player;
import legacy.map.Point;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.Stream;

// TODO: Convert to property-based testing where possible
public final class CLITest {
	private static final List<String> TRUE_POSSIBILITIES = List.of("yes", "true", "y", "t");

	private static final List<String> FALSE_POSSIBILITIES = List.of("no", "false", "n", "f");

	private static final Runnable NOOP = () -> {
	};

	/**
	 * A helper method to condense tests.
	 *
	 * @param method         The method under test.
	 * @param input          The lines of input to pass to the CLIHelper's input stream.
	 * @param expectedOutput What the CLIHelper is expected to print to its
	 *                       output stream. If an Iterable, it's the lines of output, each
	 *                       <em>except the last</em> followed by a newline.
	 * @param expectedResult The expected result of the method under test.
	 * @param resultMessage  The assertion message for the assertion that the result is as expected.
	 * @param outputMessage  The assertion message for the assertion that the output is as expected.
	 *
	 * In Ceylon, we provided defaults for resultMessage and outputMessage ("CLIHelper method result
	 * was as expected" and "CLIHelper output was as expected"
	 * respectively), but I won't add overloads for that unless I have to.
	 */
	static <T> void assertCLI(final Function<ICLIHelper, @Nullable T> method, final List<String> input,
							  final String expectedOutput, final @Nullable T expectedResult, final String resultMessage,
							  final String outputMessage) {
		final StringBuilder ostream = new StringBuilder();
		final ICLIHelper cli = new CLIHelper(new LinkedList<>(input)::pollFirst, ostream::append, NOOP);
		assertEquals(expectedResult, method.apply(cli), resultMessage);
		assertEquals(expectedOutput, ostream.toString(), outputMessage);
	}

	/**
	 * Test {@link ICLIHelper#chooseFromList}.
	 */
	@Test
	public void testChooseFromList() {
		assertCLI(cli -> cli.<Player>chooseFromList(Arrays.asList(new PlayerImpl(1, "one"),
						new PlayerImpl(2, "two")), "test desc", "none present", "prompt",
						ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT),
				Collections.singletonList("0"), "test desc%n0: one%n1: two%nprompt ".formatted(),
				Pair.with(0, new PlayerImpl(1, "one")),
				"chooseFromList chooses the one specified by the user",
				"chooseFromList prompted the user");
		assertCLI(cli -> cli.<Player>chooseFromList(Arrays.asList(new PlayerImpl(1, "one"),
						new PlayerImpl(2, "two")), "test desc", "none present", "prompt",
						ICLIHelper.ListChoiceBehavior.AUTO_CHOOSE_ONLY),
				Collections.singletonList("1"), "test desc%n0: one%n1: two%nprompt ".formatted(),
				Pair.with(1, new PlayerImpl(2, "two")),
				"chooseFromList chooses the one specified by the user",
				"chooseFromList prompted the user");
		assertCLI(cli -> cli.<Player>chooseFromList(Collections.singletonList(new PlayerImpl(1, "one")),
						"test desc", "none present", "prompt", ICLIHelper.ListChoiceBehavior.AUTO_CHOOSE_ONLY),
				Collections.emptyList(), "test desc%nAutomatically choosing only item, one.%n".formatted(),
				Pair.with(0, new PlayerImpl(1, "one")),
				"chooseFromList chooses only choice when this is specified",
				"chooseFromList automatically chose only choice");
		assertCLI(cli -> cli.<Player>chooseFromList(Collections.singletonList(new PlayerImpl(1, "one")),
						"test desc", "none present", "prompt", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT),
				Collections.singletonList("0"), "test desc%n0: one%nprompt ".formatted(),
				Pair.with(0, new PlayerImpl(1, "one")),
				"chooseFromList doesn't always auto-choose only choice",
				"chooseFromList didn't automatically choose only choice");
	}

	/**
	 * A second test of {@link ICLIHelper#chooseFromList}.
	 */
	@Test
	public void testChooseFromListMore() {
		assertCLI(cli -> cli.<Player>chooseFromList(Arrays.asList(new PlayerImpl(1, "one"),
						new PlayerImpl(2, "two")), "test desc", "none present", "prompt ",
						ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT),
				Arrays.asList("-1", "0"), "test desc%n0: one%n1: two%nprompt prompt ".formatted(),
				Pair.with(0, new PlayerImpl(1, "one")),
				"chooseFromList prompts again when negative index given",
				"chooseFromList prompts again when negative index given");
		assertCLI(cli -> cli.<Player>chooseFromList(Arrays.asList(new PlayerImpl(1, "one"),
						new PlayerImpl(2, "two")), "test desc", "none present", "prompt",
						ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT),
				Collections.singletonList("3"), "test desc%n0: one%n1: two%nprompt ".formatted(),
				Pair.with(3, null), "chooseFromList allows too-large choice",
				"chooseFromList allows too-large choice");
		assertCLI(cli -> cli.<Player>chooseFromList(Arrays.asList(new PlayerImpl(1, "one"),
						new PlayerImpl(2, "two")), "test desc", "none present", "prompt",
						ICLIHelper.ListChoiceBehavior.AUTO_CHOOSE_ONLY),
				Collections.singletonList("0"), "test desc%n0: one%n1: two%nprompt ".formatted(),
				Pair.with(0, new PlayerImpl(1, "one")),
				"chooseFromList asks even if 'auto' when multiple items",
				"chooseFromList prompted the user");
		assertCLI(cli -> cli.<Player>chooseFromList(Collections.emptyList(), "test desc",
						"none present", "prompt", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT), Collections.emptyList(),
				"none present%n".formatted(), Pair.with(-1, null),
				"chooseFromList handles no-item case", "chooseFromList didn't prompt the user");
	}

	/**
	 * Test {@link ICLIHelper#inputNumber}
	 */
	@Test
	public void testInputNumber() {
		assertCLI(cli -> cli.inputNumber("test prompt"), Collections.singletonList("2"),
				"test prompt ", 2, "inputNumber works", "inputNumber uses given prompt");
		assertCLI(cli -> cli.inputNumber("test prompt two"), Collections.singletonList("8"),
				"test prompt two ", 8, "inputNumber works", "inputNumber uses given prompt");
		assertCLI(cli -> cli.inputNumber("test prompt three "), Arrays.asList("-1", "0"),
				"test prompt three test prompt three ", 0,
				"inputNumber asks again on negative input",
				"inputNumber asks again on negative input");
		assertCLI(cli -> cli.inputNumber("test prompt four "), Arrays.asList("not-number", "9"),
				"test prompt four test prompt four ", 9,
				"inputNumber asks again on non-numeric input",
				"inputNumber asks again on non-numeric input");
		assertCLI(cli -> cli.inputNumber("test prompt five "), Collections.emptyList(),
				"test prompt five ", null, "inputNumber produces null on EOF",
				"inputNumber doesn't ask again on EOF");
	}

	/**
	 * Test {@link ICLIHelper#inputDecimal}
	 */
	@Test
	public void testInputDecimal() {
		assertCLI(cli -> cli.inputDecimal("test prompt"), Collections.singletonList("10"),
				"test prompt ", BigDecimal.TEN, "inputDecimal works with integers",
				"inputDecimal uses given prompt");
		assertCLI(cli -> cli.inputDecimal("test prompt two"), Collections.singletonList("2.5"),
				"test prompt two ", new BigDecimal(5).divide(new BigDecimal(2)),
				"inputDecimal works with decimals", "inputDecimal uses given prompt");
		assertCLI(cli -> cli.inputDecimal("test prompt three "), Arrays.asList("-2.5", "0.5"),
				"test prompt three test prompt three ", BigDecimal.ONE.divide(new BigDecimal(2)),
				"inputDecimal asks again on negative input",
				"inputDecimal asks again on negative input");
		assertCLI(cli -> cli.inputDecimal("test prompt four "), Arrays.asList("non-number", ".1"),
				"test prompt four Invalid number.%ntest prompt four ".formatted(),
				BigDecimal.ONE.divide(BigDecimal.TEN),
				"inputDecimal asks again on non-numerc input",
				"inputDecimal asks again on non-numeric input");
		assertCLI(cli -> cli.inputDecimal("test prompt five "), Collections.emptyList(),
				"test prompt five ", null, "inputDecimal produces null on EOF",
				"inputDecimal doesn't prompt again on EOF");
	}

	/**
	 * Test for {@link ICLIHelper#inputString}"
	 */
	@Test
	public void testInputString() {
		assertCLI(cli -> cli.inputString("string prompt"), Collections.singletonList("first"),
				"string prompt ", "first", "inputString returns the entered string",
				"inputString displays prompt");
		assertCLI(cli -> cli.inputString("second prompt"), Collections.singletonList("second"),
				"second prompt ", "second", "inputString returns the entered string",
				"inputString displays prompt");
		assertCLI(cli -> cli.inputString("third prompt"), Collections.emptyList(), "third prompt ",
				null, "inputString returns null on EOF", "inputString displays prompt");
	}

	public static Stream<String> truePossibilities() {
		return TRUE_POSSIBILITIES.stream();
	}

	/**
	 * Test that {@link ICLIHelper#inputBoolean} returns true when it should.
	 */
	@ParameterizedTest
	@MethodSource("truePossibilities")
	public void testInputBooleanSimpleTrue(final String arg) {
		assertCLI(cli -> cli.inputBoolean("bool prompt"), Collections.singletonList(arg),
				"bool prompt ", true, "inputBoolean returns true on " + arg,
				"inputBoolean displays prompt");
	}

	public static Stream<String> falsePossibilities() {
		return FALSE_POSSIBILITIES.stream();
	}

	/**
	 * Test that {@link ICLIHelper#inputBoolean} returns false when it should.
	 */
	@ParameterizedTest
	@MethodSource("falsePossibilities")
	public void testInputBooleanSimpleFalse(final String arg) {
		assertCLI(cli -> cli.inputBoolean("prompt two"), Collections.singletonList(arg),
				"prompt two ", false, "inputBoolean returns false on " + arg,
				"inputBoolean displays prompt");
	}

	/**
	 * Test that {@link ICLIHelper#inputBoolean} asks again on invalid input.
	 */
	@Test
	public void testInputBooleanInvalidInput() {
		assertCLI(cli -> cli.inputBoolean("prompt three "), Arrays.asList("yoo-hoo", "no"),
				("prompt three Please enter \"yes\", \"no\", \"true\", or \"false\",%n" +
								"or the first character of any of those.%nprompt three ").formatted(), false,
				"inputBoolean rejects other input", "inputBoolean gives message on invalid input");
	}

	/**
	 * Test that {@link ICLIHelper#inputBooleanInSeries} handles the basic "truthy" inputs properly.
	 */
	@ParameterizedTest
	@MethodSource("truePossibilities")
	public void testInputBooleanInSeriesSimpleTrue(final String arg) {
		assertCLI(cli -> cli.inputBooleanInSeries("bool prompt"), Collections.singletonList(arg),
				"bool prompt ", true,
				"inputBooleanInSeries returns true on '%s'".formatted(arg),
				"inputBooleanInSeries displays prompt");
	}

	/**
	 * Test that {@link ICLIHelper#inputBooleanInSeries} handles the basic "falsey" inputs properly.
	 */
	@ParameterizedTest
	@MethodSource("falsePossibilities")
	public void testInputBooleanInSeriesSimpleFalse(final String arg) {
		assertCLI(cli -> cli.inputBooleanInSeries("prompt two"), Collections.singletonList(arg),
				"prompt two ", false, "inputBooleanInSeries returns false on " + arg,
				"inputBooleanInSeries displays prompt");
	}

	/**
	 * Test that {@link ICLIHelper#inputBooleanInSeries} supports "always" and "never" and synonyms.
	 *
	 * TODO: convert to parameterized tests
	 */
	@Test
	public void testInputBooleanInSeriesAll() {
		assertCLI(cli -> cli.inputBooleanInSeries("prompt three "), Arrays.asList("nothing", "true"),
				("prompt three Please enter \"yes\", \"no\", \"true\", or \"false\", " +
								"the first%ncharacter of any of those, or \"all\", \"none\", \"always\", " +
								"or%n\"never\" to use the same answer for all further questions.%nprompt " +
								"three ").formatted(),
				true, "inputBoolean rejects other input",
				"inputBoolean gives message on invalid input");
		final StringBuilder ostream = new StringBuilder();
		ICLIHelper cli = new CLIHelper(new LinkedList<>(Collections.singletonList("all"))::pollFirst,
				ostream::append, NOOP);
		assertEquals(true, cli.inputBooleanInSeries("prompt four "),
				"inputBooleanInSeries allows yes-to-all");
		assertEquals(true, cli.inputBooleanInSeries("prompt four "),
				"inputBooleanInSeries honors yes-to-all when prompt is the same");
		assertEquals("prompt four prompt four yes%n".formatted(), ostream.toString(),
				"inputBooleanInSeries shows automatic yes");
		// TODO: Should we assert that [() -> cli.inputBooleanInSeries("other prompt")] throws
		// IOException? We had an assertion of that commented out in the Ceylon version of this test,
		// commented out because of a bug in the Java interop of the Ceylon metamodel used by the
		// assertThatException().hasType() idiom.
	}

	/**
	 * Test that {@link ICLIHelper#inputBooleanInSeries} supports "always" and "never" and synonyms.
	 *
	 * TODO: convert to parameterized tests
	 */
	@Test
	public void testInputBooleanInSeriesNone() {
		final StringBuilder ostream = new StringBuilder();
		ICLIHelper cli = new CLIHelper(new LinkedList<>(Collections.singletonList("none"))::pollFirst,
				ostream::append, NOOP);
		assertEquals(false, cli.inputBooleanInSeries("prompt five "),
				"inputBooleanInSeries allows no-to-all");
		assertEquals(false, cli.inputBooleanInSeries("prompt five "),
				"inputBooleanInSeries honors no-to-all when prompt is the same");
		assertEquals("prompt five prompt five no%n".formatted(), ostream.toString(),
				"inputBooleanInSeries shows automatic no");
		// TODO: Should we assert that [() -> cli.inputBooleanInSeries("other prompt")] throws
		// IOException? (See above.)
	}

	/**
	 * Test that {@link ICLIHelper#inputBooleanInSeries} supports "always" and "never" and synonyms.
	 *
	 * TODO: convert to parameterized tests
	 */
	@Test
	public void testInputBooleanInSeriesAlways() {
		final StringBuilder ostream = new StringBuilder();
		ICLIHelper cli = new CLIHelper(new LinkedList<>(Collections.singletonList("always"))::pollFirst,
				ostream::append, NOOP);
		assertEquals(true, cli.inputBooleanInSeries("prompt six ", "key"),
				"inputBooleanInSeries allows yes-to-all");
		assertEquals(true, cli.inputBooleanInSeries("prompt seven ", "key"),
				"inputBooleanInSeries honors yes-to-all if prompt differs, same key");
		assertEquals("prompt six prompt seven yes%n".formatted(), ostream.toString(),
				"inputBooleanInSeries shows automatic yes");
	}

	/**
	 * Test that {@link ICLIHelper#inputBooleanInSeries} supports "always" and "never" and synonyms.
	 *
	 * TODO: convert to parameterized tests
	 */
	@Test
	public void testInputBooleanInSeriesNever() {
		final StringBuilder ostream = new StringBuilder();
		ICLIHelper cli = new CLIHelper(new LinkedList<>(Collections.singletonList("never"))::pollFirst,
				ostream::append, NOOP);
		assertEquals(false, cli.inputBooleanInSeries("prompt eight ", "secondKey"),
				"inputBooleanInSeries allows no-to-all");
		assertEquals(false, cli.inputBooleanInSeries("prompt nine ", "secondKey"),
				"inputBooleanInSeries honors no-to-all if prompt differs, same key");
		assertEquals("prompt eight prompt nine no%n".formatted(), ostream.toString(),
				"inputBooleanInSeries shows automatic no");
	}

	/**
	 * Test that {@link ICLIHelper#inputBooleanInSeries} supports "always" and "never" and synonyms.
	 *
	 * TODO: convert to parameterized tests
	 */
	@Test
	public void testInputBooleanInSeriesSeparateKeys() {
		final StringBuilder ostream = new StringBuilder();
		ICLIHelper cli = new CLIHelper(new LinkedList<>(Arrays.asList("all", "none"))::pollFirst,
				ostream::append, NOOP);
		assertEquals(true, cli.inputBooleanInSeries("prompt ten ", "thirdKey"),
				"inputBooleanInSeries allows yes-to-all with one key");
		assertEquals(false, cli.inputBooleanInSeries("prompt eleven ", "fourthKey"),
				"inputBooleanInSeries allows no-to-all with second key");
		assertEquals(true, cli.inputBooleanInSeries("prompt twelve ", "thirdKey"),
				"inputBooleanInSeries then honors yes-to-all");
		assertEquals(false, cli.inputBooleanInSeries("prompt thirteen ", "fourthKey"),
				"inputBooleanInSeries then honors no-to-all");
		assertEquals("prompt ten prompt eleven prompt twelve yes%nprompt thirteen no%n".formatted(),
				ostream.toString(), "inputBooleanInSeries shows prompts");
	}

	/**
	 * Test of {@link ICLIHelper#chooseStringFromList}
	 */
	@Test
	public void testChooseStringFromList() {
		assertCLI(cli -> cli.chooseStringFromList(Arrays.asList("one", "two"),
						"test desc", "none present", "prompt", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT),
				Collections.singletonList("0"), "test desc%n0: one%n1: two%nprompt ".formatted(),
				Pair.with(0, "one"), "chooseStringFromList chooses the one specified by the user",
				"chooseStringFromList prompts the user");
		assertCLI(cli -> cli.chooseStringFromList(Arrays.asList("one", "two", "three"),
						"test desc", "none present", "prompt two", ICLIHelper.ListChoiceBehavior.AUTO_CHOOSE_ONLY),
				Collections.singletonList("1"), "test desc%n0: one%n1: two%n2: three%nprompt two ".formatted(),
				Pair.with(1, "two"), "chooseStringFromList chooses the one specified by the user",
				"chooseStringFromList prompts the user");
		assertCLI(cli -> cli.chooseStringFromList(Collections.singletonList("one"), "test desc",
						"none present", "prompt", ICLIHelper.ListChoiceBehavior.AUTO_CHOOSE_ONLY),
				Collections.emptyList(), "test desc%nAutomatically choosing only item, one.%n".formatted(),
				Pair.with(0, "one"),
				"chooseStringFromList automatically chooses only choice when told to",
				"chooseStringFromList automatically chose only choice");
		assertCLI(cli -> cli.chooseStringFromList(Collections.singletonList("one"), "test desc",
						"none present", "prompt", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT),
				Collections.singletonList("0"), "test desc%n0: one%nprompt ".formatted(), Pair.with(0, "one"),
				"chooseStringFromList doesn't always auto-choose",
				"chooseStringFromList didn't automatically choose only choice");
	}

	/**
	 * A second test of {@link ICLIHelper#chooseStringFromList}
	 */
	@Test
	public void testChooseStringFromListMore() {
		assertCLI(cli -> cli.chooseStringFromList(Arrays.asList("zero", "one", "two"),
						"test desc", "none present", "prompt", ICLIHelper.ListChoiceBehavior.AUTO_CHOOSE_ONLY),
				Collections.singletonList("1"), "test desc%n0: zero%n1: one%n2: two%nprompt ".formatted(),
				Pair.with(1, "one"), "chooseStringFromList doesn't auto-choose when more than one item",
				"chooseStringFromList doesn't auto-choose when more than one item");
		assertCLI(cli -> cli.chooseStringFromList(Arrays.asList("one", "two"),
						"test desc", "none present", "prompt", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT),
				Arrays.asList("-1", "0"), "test desc%n0: one%n1: two%nprompt prompt ".formatted(), Pair.with(0, "one"),
				"chooseStringFromList prompts again when negative index given",
				"chooseStringFromList prompts again when negative index given");
		assertCLI(cli -> cli.chooseStringFromList(Arrays.asList("one", "two"), "test desc",
						"none present", "prompt", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT),
				Collections.singletonList("3"), "test desc%n0: one%n1: two%nprompt ".formatted(), Pair.with(3, null),
				"chooseStringFromList allows too-large choice",
				"chooseStringFromList allows too-large choice");
		assertCLI(cli -> cli.chooseStringFromList(Collections.emptyList(), "test desc",
						"none present", "prompt", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT), Collections.emptyList(),
				"none present%n".formatted(), Pair.with(-1, null),
				"chooseStringFromList handles empty list",
				"chooseStringFromList handles empty list");
	}

	/**
	 * Test {@link ICLIHelper#print} and {@link ICLIHelper#println println}.
	 */
	@Test
	public void testPrinting() {
		final StringBuilder ostream = new StringBuilder();
		new CLIHelper(new LinkedList<String>()::pollFirst, ostream::append, NOOP).print("test string");
		assertEquals("test string", ostream.toString(), "print() prints string");
		ostream.setLength(0);
		new CLIHelper(new LinkedList<String>()::pollFirst, ostream::append, NOOP).println("test two");
		assertEquals("test two%n".formatted(), ostream.toString(), "println() adds newline");
		ostream.setLength(0);
		new CLIHelper(new LinkedList<String>()::pollFirst, ostream::append, NOOP)
				.print("test three ", "test four");
		assertEquals("test three test four", ostream.toString(),
				"print() with multiple arguments works");
	}

	/**
	 * Test {@link ICLIHelper#inputPoint}.
	 */
	@Test
	public void testInputPoint() {
		assertCLI(cli -> cli.inputPoint("point prompt one "), Arrays.asList("2", "3"),
				"point prompt one  Row: Column: ", new Point(2, 3),
				"reads row then column", "prompts as expected");
		assertCLI(cli -> cli.inputPoint("point prompt two "), Arrays.asList("-1", "0", "-2", "4"),
				"point prompt two  Row: Row: Column: Column: ", new Point(0, 4),
				"doesn't accept negative row or column", "prompts as expected");
	}
}
