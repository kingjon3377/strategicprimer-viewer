package drivers.exploration.old;

import org.jetbrains.annotations.Nullable;
import common.map.TileType;
import common.map.Point;
import common.map.MapDimensions;
import common.map.TileFixture;
import java.util.Collections;
import java.util.Set;

/**
 * An {@link EncounterTable} that always returns the same value.
 */
class ConstantTable implements EncounterTable {
	private final String constant;
	public ConstantTable(String constant) {
		this.constant = constant;
	}

	@Override
	public String generateEvent(Point point, @Nullable TileType terrain, boolean mountainous, 
			Iterable<TileFixture> fixtures, MapDimensions dimensions) {
		return constant;
	}

	@Override
	public Set<String> getAllEvents () {
		return Collections.singleton(constant);
	}

	@Override
	public String toString() {
		return "ConstantTable: " + constant;
	}
}
