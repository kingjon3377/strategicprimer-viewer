package model.map.fixtures.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Possible status of fields (and meadows, and orchards ...) Fields should
 * rotate between these, at a rate determined by the kind of field.
 * 
 * @author Jonathan Lovelace
 * 
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
	/**
	 * A mapping from strings to FieldStatus.
	 */
	private static final Map<String, FieldStatus> FST_MAP = new HashMap<String, FieldStatus>();
	/**
	 * A string representing the FieldStatus.
	 */
	private final String str;

	/**
	 * Constructor.
	 * 
	 * @param string a string representing the status.
	 */
	private FieldStatus(final String string) {
		str = string;
	}

	static {
		for (final FieldStatus fs : values()) {
			FST_MAP.put(fs.str, fs);
		}
	}

	/**
	 * @param string a string representing a FieldStatus
	 * @return the FieldStatus it represents
	 */
	public static FieldStatus parse(final String string) {
		return FST_MAP.get(string);
	}

	/**
	 * @return a string representation of the status
	 */
	@Override
	public String toString() {
		return str;
	}

	/**
	 * @param seed a number to use to seed the RNG
	 * @return a random status
	 */
	public static FieldStatus random(final int seed) {
		return values()[new Random(seed).nextInt(values().length)];
	}
}
