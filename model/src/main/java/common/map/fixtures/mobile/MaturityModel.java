package common.map.fixtures.mobile;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import lovelace.util.LovelaceLogger;

/**
 * A model, loaded from file, of the ages at which young animals become adults.
 * We also, for lack of a better way of making it available where necessary in
 * the codebase, store a notion of the current turn here.
 */
public final class MaturityModel {
	private static final Map<String, Integer> MATURITY_AGES =
		initMaturityAges();

	private MaturityModel() {
	}

	private static Map<String, Integer> initMaturityAges() {
		try {
			return FileSplitter.getFileContents("animal_data/maturity.txt",
				Integer::parseInt);
		} catch (final IOException except) {
			throw new RuntimeException(except);
		}
	}

	public static Map<String, Integer> getMaturityAges() {
		return Collections.unmodifiableMap(MATURITY_AGES);
	}

	private static int currentTurnLocal = -1;

	public static int getCurrentTurn() {
		return currentTurnLocal;
	}

	public static void setCurrentTurn(final int currentTurn) {
		if (currentTurnLocal < 0) {
			currentTurnLocal = currentTurn;
		} else if (currentTurnLocal != currentTurn) {
			LovelaceLogger.warning("Tried to reset current turn");
		}
	}

	/**
	 * Clear the stored current turn
	 *
	 * TODO: Can we restrict access somehow?
	 */
	public static void resetCurrentTurn() {
		currentTurnLocal = -1;
	}
}

