package model.map.fixtures.resources;

import model.map.HasImage;
import model.map.HasKind;
import model.map.IFixture;
import model.map.TileFixture;

/**
 * A field or meadow. If in forest, should increase a unit's vision slightly
 * when the unit is on it.
 *
 * @author Jonathan Lovelace
 *
 */
public class Meadow implements HarvestableFixture,
		HasImage, HasKind {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Which season the field is in.
	 */
	private final FieldStatus status;
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
	private String kind;

	/**
	 * Constructor.
	 *
	 * @param grain the kind of grass or grain growing in the field or meadow
	 * @param fld whether this is a field (as opposed to a meadow)
	 * @param cult whether it's under cultivation
	 * @param idNum the ID number.
	 * @param stat the status of the field, i.e. what season it's in
	 */
	public Meadow(final String grain, final boolean fld, final boolean cult,
			final int idNum, final FieldStatus stat) {
		super();
		kind = grain;
		field = fld;
		cultivated = cult;
		id = idNum;
		status = stat;
	}

	/**
	 * @return the kind of grass or grain growing in the meadow or field
	 */
	@Override
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
	 * @return the status of the field, i.e. what season it's in
	 */
	public FieldStatus getStatus() {
		return status;
	}

	/**
	 * TODO: This should be more granular based on the kind of field.
	 *
	 * @return the name of an image to represent the field or meadow
	 */
	@Override
	public String getDefaultImage() {
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
	 *
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
		return this == obj || (obj instanceof Meadow && kind.equals(((Meadow) obj).kind)
				&& field == ((Meadow) obj).field
				&& cultivated == ((Meadow) obj).cultivated
				&& id == ((TileFixture) obj).getID());
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * @param fix A TileFixture to compare to
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
	private final int id; // NOPMD

	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof Meadow && kind.equals(((Meadow) fix).kind)
				&& field == ((Meadow) fix).field
				&& cultivated == ((Meadow) fix).cultivated && status == ((Meadow) fix).status;
	}
	/**
	 * @param nKind the new kind
	 */
	@Override
	public final void setKind(final String nKind) {
		kind = nKind;
	}
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}
	/**
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}
}
