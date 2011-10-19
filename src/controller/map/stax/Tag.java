package controller.map.stax;

/**
 * An enumerated type for the tags we know about.
 * 
 * @author Jonathan Lovelace
 */
@Deprecated
enum Tag {
	/**
	 * The main map tag.
	 */
	Map("map"),
	/**
	 * Rows: we actually ignore these tags.
	 */
	Row("row"),
	/**
	 * Tiles.
	 */
	Tile("tile"),
	/**
	 * Players.
	 */
	Player("player"),
	/**
	 * Fortresses.
	 */
	Fortress("fortress"),
	/**
	 * Units.
	 */
	Unit("unit"),
	/**
	 * Rivers.
	 */
	River("river"),
	/**
	 * Lakes: internally a special case of rivers, but we want a simpler XML
	 * tile for them.
	 */
	Lake("lake"),
	/**
	 * An Event. @see model.viewer.events.AbstractEvent
	 */
	Event("event"),
	/**
	 * A battlefield. @see model.viewer.events.BattlefieldEvent
	 */
	Battlefield("battlefield"),
	/**
	 * Cave. @see model.veiwer.events.CaveEvent
	 */
	Cave("cave"),
	/**
	 * City. @see model.viewer.events.CityEvent
	 */
	City("city"),
	/**
	 * Fortification: @see model.viewer.events.FortificationEvent FIXME: We want
	 * this to use the Fortress tag instead, eventually.
	 */
	Fortification("fortification"),
	/**
	 * Minerals. @see model.viewer.events.MineralEvent
	 */
	Mineral("mineral"),
	/**
	 * Stone. @see model.viewer.events.StoneEvent
	 */
	Stone("stone"),
	/**
	 * Town. @see model.viewer.events.TownEvent
	 */
	Town("town"),
	/**
	 * Anything not enumerated.
	 */
	Unknown("unknown");
	/**
	 * The text version of the tag.
	 */
	private final String text;

	/**
	 * Constructor.
	 * 
	 * @param tagText
	 *            The string to associate with the tag.
	 */
	private Tag(final String tagText) {
		text = tagText;
	}

	/**
	 * 
	 * @return the string associated with the tag.
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param tagText
	 *            a string
	 * 
	 * @return the tag that represents that string, if any, or Unknown if none.
	 */
	public static Tag fromString(final String tagText) {
		Tag retval = Unknown;
		if (tagText != null) {
			for (final Tag tag : Tag.values()) {
				if (tagText.equalsIgnoreCase(tag.text)) {
					retval = tag;
					break;
				}
			}
		}
		return retval;
	}
}
