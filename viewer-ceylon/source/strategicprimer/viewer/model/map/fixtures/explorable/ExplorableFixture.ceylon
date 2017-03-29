import lovelace.util.common {
    todo
}

import model.map {
    TileFixture,
    HasMutableImage
}
"A (for now marker) interface for fixtures that can be somehow explored."
todo("What methods should this have?")
shared interface ExplorableFixture satisfies TileFixture&HasMutableImage {}