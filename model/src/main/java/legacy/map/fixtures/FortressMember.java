package legacy.map.fixtures;

import legacy.map.SubsettableFixture;
import org.jetbrains.annotations.NotNull;

/**
 * A (marker) interface for things that can be in a fortress.
 *
 * TODO: Members?
 */
public interface FortressMember extends SubsettableFixture {
	/**
	 * Specialization.
	 */
	@Override
	@NotNull
	FortressMember copy(@NotNull CopyBehavior zero);
}
