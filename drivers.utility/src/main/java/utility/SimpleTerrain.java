package utility;

/**
 * A simplified model of terrain, dividing tiles into "ocean", "forested", and "unforested".
 */
/* package */ enum SimpleTerrain {
	/**
	 * Plains, desert, and mountains
	 */
	Unforested,
	/**
	 * Temperate forest, boreal forest, and steppe
	 */
	Forested,
	/**
	 * Ocean.
	 */
	Ocean;
}
