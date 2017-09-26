import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    TileFixture
}
"A marker interface for TileFixtures that are terrain-related and so, if not the top
 fixture on the tile, should change the tile's presentation."
todo("Should there be any members?")
shared interface TerrainFixture satisfies TileFixture {}
