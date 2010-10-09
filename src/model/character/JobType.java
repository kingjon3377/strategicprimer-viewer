package model.character;

/**
 * FIXME: This really shouldn't be hard-coded like this; the various Jobs---and
 * their effects, which don't fit an Enum anyway---should be loaded from file.
 * 
 * @author Jonathan Lovelace
 * 
 */
public enum JobType {
	/**
	 * Unspecified labor.
	 */
	Laborer,
	/**
	 * Farmer.
	 */
	Farmer,
	/**
	 * Woodcutter.
	 */
	Woodcutter,
	/**
	 * Warrior.
	 */
	Warrior;
}
