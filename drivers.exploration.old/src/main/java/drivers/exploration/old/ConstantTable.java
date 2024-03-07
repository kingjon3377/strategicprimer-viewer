package drivers.exploration.old;

import org.jetbrains.annotations.Nullable;
import legacy.map.TileType;
import legacy.map.Point;
import legacy.map.MapDimensions;
import legacy.map.TileFixture;

import java.util.Collections;
import java.util.Set;

/**
 * An {@link EncounterTable} that always returns the same value.
 */
class ConstantTable implements EncounterTable {
	private final String constant;

	public ConstantTable(final String constant) {
		this.constant = constant;
	}

	@Override
	public String generateEvent(final Point point, final @Nullable TileType terrain, final boolean mountainous,
								final Iterable<TileFixture> fixtures, final MapDimensions dimensions) {
		return constant;
	}

	@Override
	public Set<String> getAllEvents() {
		return Collections.singleton(constant);
	}

	@Override
	public String toString() {
		return "ConstantTable: " + constant;
	}
}
