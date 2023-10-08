package common.map.fixtures;

/**
 * A quantity of some kind of resource.
 *
 * TODO: More members?
 */
public final class ResourcePileImpl implements IMutableResourcePile {

    public ResourcePileImpl(final int id, final String kind, final String contents, final Quantity quantity) {
        this.id = id;
        this.kind = kind;
        this.contents = contents;
        this.quantity = quantity;
    }

    @Override
    public String getPlural() {
        return "Resource Piles";
    }

    /**
     * The ID # of the resource pile.
     */
    private final int id;

    /**
     * The ID # of the resource pile.
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * What general kind of thing is in the resource pile.
     */
    private final String kind;

    /**
     * What general kind of thing is in the resource pile.
     */
    @Override
    public String getKind() {
        return kind;
    }

    /**
     * What specific kind of thing is in the resource pile.
     */
    private final String contents;

    /**
     * What specific kind of thing is in the resource pile.
     */
    @Override
    public String getContents() {
        return contents;
    }

    /**
     * How much of that thing is in the pile, including units.
     */
    private Quantity quantity;

    /**
     * How much of that thing is in the pile, including units.
     */
    @Override
    public Quantity getQuantity() {
        return quantity;
    }

    /**
     * Set how much of that thing is in the pile, including units.
     */
    public void setQuantity(final Quantity quantity) {
        this.quantity = quantity;
    }

    /**
     * The filename of an image to use as an icon for this instance.
     */
    private String image = "";

    /**
     * The filename of an image to use as an icon for this instance.
     */
    @Override
    public String getImage() {
        return image;
    }

    /**
     * Set the filename of an image to use as an icon for this instance.
     */
    @Override
    public void setImage(final String image) {
        this.image = image;
    }

    /**
     * The turn on which the resource was created.
     */
    private int createdTurn = -1;

    /**
     * The turn on which the resource was created.
     */
    @Override
    public int getCreated() {
        return createdTurn;
    }

    @Override
    public void setCreated(final int created) {
        if (created < 0) {
            createdTurn = -1;
        } else {
            createdTurn = created;
        }
    }

    @Override
    public String getDefaultImage() {
        return "resource.png";
    }

    /**
     * Clone the object.
     *
     * TODO: If {@link common.map.IFixture.CopyBehavior#ZERO}, probably shouldn't expose <em>precise</em> quantity.
     */
    @Override
    public ResourcePileImpl copy(final CopyBehavior zero) {
        final ResourcePileImpl retval = new ResourcePileImpl(id, kind, contents, quantity);
        if (zero == CopyBehavior.KEEP) {
            retval.setCreated(createdTurn);
        }
        return retval;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof IResourcePile rp) {
            return id == rp.getId() && equalsIgnoringID(rp);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        if (quantity.units().isEmpty()) {
            if (createdTurn < 0) {
                return String.format("A pile of %s %s (%s)", quantity, contents, kind);
            } else {
                return String.format("A pile of %s %s (%s) from turn %d",
                        quantity, contents, kind, createdTurn);
            }
        } else {
            if (createdTurn < 0) {
                return String.format("A pile of %s of %s (%s)", quantity, contents, kind);
            } else {
                return String.format("A pile of %s of %s (%s) from turn %d",
                        quantity, contents, kind, createdTurn);
            }
        }
    }
}
