package model.map;

/**
 * An interface for fixtures that have a 'kind' property.
 *
 * FIXME: Should we split this, and other similar interfaces, into "HasX" and
 * "MutableHasX"?
 *
 * @author Jonathan Lovelace
 *
 */
public interface HasKind {
	/**
	 * @return the kind of whatever this is
	 */
	String getKind();

	/**
	 * @param nKind the thing's new kind
	 */
	void setKind(final String nKind);
}
