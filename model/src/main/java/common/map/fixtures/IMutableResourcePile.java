package common.map.fixtures;

import common.map.HasMutableImage;

// TODO: If the mutators are used, surely this should extend HasMutableKind?
public interface IMutableResourcePile extends IResourcePile, HasMutableImage {
	/**
	 * Set what specific kind of thing is in the resource pile.
	 * FIXME: Why do we want this? Remove if unused.
	 */
	void setContents(String contents);

	/**
	 * Set how much of that thing is in the pile, including units.
	 * FIXME: Why do we want this? Remove if unused.
	 */
	void setQuantity(Quantity quantity);

	/**
	 * Set the turn on which the resource was created.
	 * FIXME: Why do we want this? Remove if unused.
	 */
	void setCreated(int created);

	/**
	 * Specialization.
	 */
	@Override
	IMutableResourcePile copy(boolean zero);
}
