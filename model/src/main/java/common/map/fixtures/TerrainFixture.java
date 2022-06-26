package common.map.fixtures;

import common.map.TileFixture;

/**
 * A marker interface for TileFixtures that are terrain-related and so, if not
 * the top fixture on the tile, should change the tile's presentation.
 *
 * TODO: Should there be any members?
 */
public interface TerrainFixture extends TileFixture {
}
