package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * A field or meadow. If in forest, should increase a unit's vision slightly when the unit is on it.
 * @author Jonathan Lovelace
 *
 */
public class Meadow implements TileFixture, HasImage {
	/**
	 * Whether this is a field. If not, it's a meadow.
	 */
	private final boolean field;
	/**
	 * Whether it's under cultivation.
	 */
	private final boolean cultivated;
	/**
	 * Kind of grass or grain growing there.
	 */
	private final String kind;
	/**
	 * Constructor.
	 * @param grain the kind of grass or grain growing in the field or meadow
	 * @param fld whether this is a field (as opposed to a meadow)
	 * @param cult whether it's under cultivation
	 * @param idNum the ID number.
	 */
	public Meadow(final String grain, final boolean fld, final boolean cult,
			final long idNum) {
		kind = grain;
		field = fld;
		cultivated = cult;
		id = idNum;
	}
	/**
	 * @return the kind of grass or grain growing in the meadow or field
	 */
	public String getKind() {
		return kind;
	}
	/**
	 * @return if this is a cultivated field or meadow
	 */
	public boolean isCultivated() {
		return cultivated;
	}
	/**
	 * @return true if this is a field, false if it's a meadow
	 */
	public boolean isField() {
		return field;
	}
	/**
	 * TODO: inline.
	 * @return an XML representation of the Fixture.
	 */
	@Override
	public String toXML() {
		final StringBuilder builder = new StringBuilder();
		if (field) {
			builder.append("<field");
		} else {
			builder.append("<meadow");
		}
		builder.append(" kind=\"");
		builder.append(kind);
		builder.append("\" cultivated=\"");
		builder.append(cultivated);
		builder.append("\" id=\"");
		builder.append(id);
		builder.append("\" />");
		return builder.toString();
	}
	/**
	 * TODO: This should be more granular based on the kind of field.
	 * @return the name of an image to represent the field or meadow
	 */
	@Override
	public String getImage() {
		return field ? "field.png" : "meadow.png";
	}
	/**
	 * @return a String representation of the field or meadow
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		if (field) {
			if (!cultivated) {
				builder.append("Wild or abandoned ");
			}
			builder.append(kind);
			builder.append(" field");
		} else {
			builder.append(kind);
			builder.append(" meadow");
		}
		return builder.toString();
	}
	/**
	 * TODO: Should probably depend.
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 15;
	}
	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof Meadow && kind.equals(((Meadow) obj).kind)
				&& field == ((Meadow) obj).field
				&& cultivated == ((Meadow) obj).cultivated
				&& id == ((TileFixture) obj).getID();
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return (int) id;
	}
	/**
	 * @param fix
	 *            A TileFixture to compare to
	 * 
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}
	/**
	 * ID number.
	 */
	private final long id; // NOPMD
	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public long getID() {
		return id;
	}
}
