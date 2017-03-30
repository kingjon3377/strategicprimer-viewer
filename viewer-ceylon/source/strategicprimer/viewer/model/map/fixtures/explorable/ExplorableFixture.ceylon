import lovelace.util.common {
    todo
}
import strategicprimer.viewer.model.map {
    HasMutableImage,
    TileFixture
}
"A (for now marker) interface for fixtures that can be somehow explored."
todo("What methods should this have?")
shared interface ExplorableFixture satisfies TileFixture&HasMutableImage {}