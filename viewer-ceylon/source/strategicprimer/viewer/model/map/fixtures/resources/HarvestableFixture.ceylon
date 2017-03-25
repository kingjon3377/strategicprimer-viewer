import lovelace.util.common {
    todo
}

import model.map {
    TileFixture,
    HasMutableImage
}
"A (for now marker) interface for fixtures that can have resources harvested, mined, etc.,
 from them."
todo("What methods should this have?")
shared interface HarvestableFixture satisfies TileFixture&HasMutableImage {}