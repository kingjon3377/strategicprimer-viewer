package model.map;

/**
 * An interface for collections of XMLWritable objects, to call setFile on their
 * children. This used to be implemented by XMLWritable objects with children,
 * but they should now call setFile on their children in their own setFile
 * implementation.
 * 
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
