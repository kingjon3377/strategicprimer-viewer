package legacy.map.fixtures;

import legacy.map.HasMutableImage;
import org.jetbrains.annotations.NotNull;

// TODO: If the mutators are used, surely this should extend HasMutableKind?
public interface IMutableResourcePile extends IResourcePile, HasMutableImage {
	/**
	 * Set how much of that thing is in the pile, including units.
	 *
	 * Used in turn-running model to reduce quantity.
	 *
	 * TODO: Replace with method returning a similar pile with a different quantity (or reduced-by)?
	 */
	void setQuantity(LegacyQuantity quantity);

	/**
	 * Set the turn on which the resource was created.
	 *
	 * TODO: replace with constructor parameter (most callers use only on newly-constructed piles)
	 */
	void setCreated(int created);

	/**
	 * Specialization.
	 */
	@Override
	@NotNull
	IMutableResourcePile copy(@NotNull CopyBehavior zero);
}
