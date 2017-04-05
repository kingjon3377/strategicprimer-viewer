import java.awt {
    Image
}
import java.lang {
    IllegalArgumentException
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    TileFixture
}
"A version-1 tile-draw-helper."
TileDrawHelper verOneHelper = DirectTileDrawHelper(); // CachingTileDrawHelper();
"A factory method for [[TileDrawHelper]]s."
todo("split so ver-1 omits ZOF etc. and ver-2 requires it as non-null?")
shared TileDrawHelper tileDrawHelperFactory(
        "The version of the map that is to be drawn."
        Integer version,
        "The object to arrange to be notified as images finish drawing. In Java it's the
          [[java.awt.image::ImageObserver]] interface, but we don't want to have to
          construct *objects* for this when a lambda will do."
        Boolean(Image, Integer, Integer, Integer, Integer, Integer) observer,
        "The filter to tell a version-two helper which fixtures to draw."
        Boolean(TileFixture)? zof,
        "A series of matchers to tell a version-two helper which fixture is on top"
        {FixtureMatcher*} matchers) {
    switch (version)
    case (1) { return verOneHelper; }
    case (2) {
        assert (exists zof);
        return Ver2TileDrawHelper(observer, zof, matchers);
    }
    else { throw IllegalArgumentException("Unsupported map version"); }
}
