package legacy.map.fixtures.resources;

import legacy.map.TileFixture;
import legacy.map.HasMutableImage;
import legacy.map.HasKind;

/**
 * A (for now marker) interface for fixtures that can have resources harvested,
 * mined, etc., from them.
 *
 * TODO: What methods should this have?
 */
public interface HarvestableFixture extends TileFixture, HasMutableImage, HasKind {
}
