package model.map;
/**
 * A superclass to simplify Tile.
 * @author Jonathan Lovelace
 *
 */
public class SimpleTile implements XMLWritable, Subsettable<SimpleTile> {
	/**
	 * Constructor.
	 * @param loc the tile's location
	 * @param tileType the tile type
	 */
	public SimpleTile(final Point loc, final TileType tileType) {
		location = loc;
		type = tileType;
	}
	/**
	 * @param obj another tile
	 * @return whether it's a subset of this one
	 */
	@Override
	public boolean isSubset(final SimpleTile obj) {
		return location.equals(obj.location) && type.equals(obj.type);
	}
	/**
	 * As this is deprecated, we expect subclasses to override, instead of designing for extensibility here.
	 * @return an XML representation of the tile.
	 */
	@Override
	@Deprecated
	public String toXML() {
		return TileType.NotVisible.equals(getTerrain()) ? "" : "<tile "
				+ location.toXML() + " kind=\"" + getTerrain().toXML()
				+ "\"></tile>";
	}
	/**
	 * The file this was loaded from.
	 */
	private String file;
	/**
	 * @return the file this was loaded from
	 */
	@Override
	public String getFile() {
		return file;
	}
	/**
	 * @param filename the file this was loaded from
	 */
	@Override
	public void setFile(final String filename) {
		file = filename;
	}
	/**
	 * The tile's location.
	 */
	private final Point location;
	/**
	 * @return the tile's location
	 */
	public Point getLocation() {
		return location;
	}
	/**
	 * The tile type.
	 */
	private TileType type;

	/**
	 * 
	 * @return the kind of tile 
	 */
	public TileType getTerrain() {
		return type;
	}

	/**
	 * @param ttype
	 *            the tile's new terrain type
	 */
	public void setTerrain(final TileType ttype) {
		type = ttype;
	}
	/**
	 * A SimpleTile is "empty" if its tile type is NotVisible.
	 * @return whether this tile is "empty"
	 */
	public boolean isEmpty() {
		return TileType.NotVisible.equals(getTerrain());
	}
	/**
	 * Update with data from a tile in another map.
	 * 
	 * @param tile
	 *            the same tile in another map.
	 */
	public void update(final SimpleTile tile) {
		setTerrain(tile.getTerrain());
	}
	/**
	 * Use this in creating subclass toString methods.
	 * @return a String representation of the tile.
	 */
	@Override
	public String toString() {
		return new StringBuilder(getLocation().toString()).append(": ").append(getTerrain()).append('.').toString();
	}
	/**
	 * Use this in creating subclass hashCode methods. In fact, it might be a
	 * good idea to just use it as is, and maybe even not use the terrain here,
	 * to avoid the hash-table bug I found. (TODO: consider.)
	 * 
	 * @return a hash code for the object.
	 */
	@Override
	public int hashCode() {
		return getLocation().hashCode() + getTerrain().ordinal() << 6;
	}
	/**
	 * Use this in creating subclass equals methods.
	 * @param obj an object
	 * @return whether it is an equal tile.
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj || obj instanceof SimpleTile
				&& getLocation().equals(((SimpleTile) obj).getLocation())
				&& getTerrain().equals(((SimpleTile) obj).getTerrain());
	}
}
