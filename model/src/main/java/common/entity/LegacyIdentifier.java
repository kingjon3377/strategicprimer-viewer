package common.entity;

import org.jspecify.annotations.NonNull;

/**
 * In the 2009-2022 campaign, "fixtures" in the map were identified using a single integer, starting from 0 and
 * counting up.
 *
 * TODO: Maybe add an "origin world" parameter, defaulting to "prime"?
 *
 * @param idNumber the ID number
 */
public record LegacyIdentifier(int idNumber) implements EntityIdentifier {
	public LegacyIdentifier {
		if (idNumber < -1) {
			throw new IllegalArgumentException("ID number must be -1 if unspecified, or else nonnegative");
		}
	}

	@Override
	public @NonNull String getIdentifierString() {
		return Integer.toString(idNumber);
	}
}
