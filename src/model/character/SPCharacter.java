package model.character;

import java.io.Serializable;

/**
 * A worker or warrior.
 * 
 * @author Jonathan Lovelace
 */
public class SPCharacter implements Serializable {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = -1906146834037495060L;
	/**
	 * The character's name.
	 */
	private String name;

	/**
	 * @return the character's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param _name
	 *            the character's name
	 */
	public void setName(final String _name) {
		name = _name;
	}

	/**
	 * Constructor.
	 * 
	 * @param _name
	 *            the character's name
	 * @param _name
	 */
	public SPCharacter(final String _name) {
		name = _name;
	}
}
