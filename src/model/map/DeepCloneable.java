package model.map;
/**
 * An interface for objects that can produce deep copies of themselves. Like Cloneable, but typesafe.
 * @author Jonathan Lovelace
 *
 * @param <T> the type of object that the cloning method will produce
 */
public interface DeepCloneable<T> {
	/**
	 * @return a clone of the object
	 */
	T deepCopy();
}
