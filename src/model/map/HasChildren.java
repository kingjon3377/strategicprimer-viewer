package model.map;
/**
 * An interface for XMLWritable objects that have children that store their own "file" property.
 * @author Jonathan Lovelace
 *
 */
public interface HasChildren {
	/**
	 * Set all children's file property to the specified value.
	 * @param value the value to set
	 */
	void setFileOnChildren(final String value);
}
