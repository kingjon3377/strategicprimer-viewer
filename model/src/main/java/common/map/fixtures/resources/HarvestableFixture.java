package common.map.fixtures.resources;

import common.map.TileFixture;
import common.map.HasMutableImage;
import common.map.HasKind;

/**
 * A (for now marker) interface for fixtures that can have resources harvested,
 * mined, etc., from them.
 *
 * TODO: What methods should this have?
 */
public interface HarvestableFixture extends TileFixture, HasMutableImage, HasKind {}
