import strategicprimer.model.common.map {
    TileFixture
}
import lovelace.util.common {
    todo
}

"""An interface for fixtures representing things that were on the original table of
   numeric "events" used for version-0 maps."""
todo("Eventually get rid of this interface?")
shared interface IEvent satisfies TileFixture {
    "Exploration-result text describing the event."
    shared formal String text;
}
