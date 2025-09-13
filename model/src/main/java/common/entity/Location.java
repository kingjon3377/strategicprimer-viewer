package common.entity;

/**
 * An interface for where something in the game-world could be located. A marker interface to start with, since
 * we will want to support both "inside such-and-such parent," "somewhere in such-and-such region," and "at
 * such-and-such coordinates in such-and-such region".
 */
public interface Location {
	/**
	 * @return a representation of the location suitable for displaying to a user.
	 */
	String getDisplayRepresentation();
}
