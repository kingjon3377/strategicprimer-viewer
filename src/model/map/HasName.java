package model.map;
/**
 * An interface for things that have a name.
 * @author Jonathan Lovelace
 *
 */
public interface HasName {
	/**
	 * @return the name
	 */
	String getName();
	/**
	 * @param nomen the thing's new name
	 */
	void setName(final String nomen);
}
