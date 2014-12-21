package model.map.fixtures;

import model.map.HasImage;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.Player;
import model.map.TileFixture;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A Fixture representing an adventure hook.
 * @author Jonathan Lovelace
 *
 */
public class AdventureFixture implements TileFixture, HasImage, HasOwner {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * The player that has undertaken the adventure.
	 */
	private Player owner;
	/**
	 * A brief description of the adventure.
	 */
	private final String briefDesc;
	/**
	 * A longer description of the adventure.
	 */
	private final String fullDesc;
	/**
	 * A unique ID # for the fixture.
	 */
	private final int id;
	/**
	 * Constructor.
	 *
	 * @param player
	 *            the player who has undertaken the adventure, or the
	 *            independent player if none
	 * @param brief
	 *            a brief description of the adventure
	 * @param full
	 *            a fuller description of the adventure
	 * @param idNum an ID number for the fixture
	 */
	public AdventureFixture(final Player player, final String brief,
			final String full, final int idNum) {
		owner = player;
		briefDesc = brief;
		fullDesc = full;
		id = idNum;
	}
	/**
	 * @return a brief description of the adventure
	 */
	public String getBriefDescription() {
		return briefDesc;
	}
	/**
	 * @return a fuller description of the adventure
	 */
	public String getFullDescription() {
		return fullDesc;
	}
	/**
	 * @return a String representation of the fixture
	 */
	@Override
	public String toString() {
		if (fullDesc.isEmpty()) {
			if (briefDesc.isEmpty()) {
				return "Adventure hook";
			} else {
				return briefDesc;
			}
		} else {
			return fullDesc;
		}
	}

	/**
	 * @return the name of an image to represent the fixture
	 */
	@Override
	public String getDefaultImage() {
		return "adventure.png";
	}
	/**
	 * @return a z-value for use in ordering tile icons on a tile
	 */
	@Override
	public int getZValue() {
		return 25;
	}
	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof AdventureFixture
				&& id == ((AdventureFixture) obj).id
				&& equalsImpl((AdventureFixture) obj);
	}
	/**
	 * @param obj an adventure fixture
	 * @return whether it's equal to this one
	 */
	private boolean equalsImpl(final AdventureFixture obj) {
		return equalOwner(obj.owner) && briefDesc.equals(obj.briefDesc)
				&& fullDesc.equals(obj.fullDesc);
	}
	/**
	 * @param player a player
	 * @return whether it's the same as the adventure's owner
	 */
	private boolean equalOwner(final Player player) {
		if (owner.isIndependent()) {
			return player.isIndependent();
		} else {
			return owner.getPlayerId() == player.getPlayerId();
		}
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return briefDesc.hashCode() | (fullDesc.hashCode() << owner.getPlayerId());
	}
	/**
	 * @return the player that has taken on this adventure
	 */
	@Override
	public Player getOwner() {
		return owner;
	}
	/**
	 * @param player the player who has now taken on the adventure
	 */
	@Override
	public void setOwner(final Player player) {
		owner = player;
	}
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
	/**
	 * @return a string describing all text fixtures as a class
	 */
	@Override
	public String plural() {
		return "Adventures";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return briefDesc;
	}
	@Override
	public int getID() {
		return id;
	}
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof AdventureFixture && equalsImpl((AdventureFixture) fix);
	}
	@Override
	public int compareTo(@Nullable final TileFixture fix) {
		if (fix == null) {
			throw new IllegalArgumentException("Compared to null fixture");
		}
		return fix.hashCode() - hashCode();
	}
}
