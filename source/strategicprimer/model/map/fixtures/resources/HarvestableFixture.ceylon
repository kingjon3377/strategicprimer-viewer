import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    HasMutableImage,
    TileFixture
}
"A (for now marker) interface for fixtures that can have resources harvested, mined, etc.,
 from them."
todo("What methods should this have?",
    "Satisfy [[strategicprimer.model.map::HasKind]]")
shared interface HarvestableFixture satisfies TileFixture&HasMutableImage {}