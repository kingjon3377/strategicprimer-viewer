package common.map.fixtures.resources;

import java.util.Random;

/**
 * Possible status of fields (and meadows, and orchards ...) Fields should
 * rotate between these, at a rate determined by the kind of field.
 *
 * TODO: Implement that
 */
public enum FieldStatus {
	/**
	 * Fallow: waiting to be planted, or waiting to start growing.
	 */
	Fallow("fallow"),
	/**
	 * Seeding: being planted, by human or animal activity.
	 */
	Seeding("seeding"),
	/**
	 * Growing.
	 */
	Growing("growing"),
	/**
	 * Bearing: ready to be harvested.
	 */
	Bearing("bearing");
	private final String string;
	private FieldStatus(final String str) {
		string = str;
	}
	@Override
	public String toString() {
		return string;
	}

	public static FieldStatus random(final int seed) {
		FieldStatus[] statuses = FieldStatus.values();
		return statuses[new Random(seed).nextInt(statuses.length)];
	}

	public static FieldStatus parse(final String status) {
		// TODO: Have HashMap cache to speed this up?
		for (FieldStatus val : values()) {
			if (status.equals(val.toString())) {
				return val;
			}
		}
		throw new IllegalArgumentException("Failed to parse FieldStatus from " + status);
	}
}
