import model.map {
    HasKind,
    IFixture
}
"An orchard (fruit trees) or grove (other trees) on the map."
shared class Grove(orchard, cultivated, kind, id)
        satisfies HarvestableFixture&HasKind {
    "If true, this is a fruit orchard; if false, a non-fruit grove."
    shared Boolean orchard;
    "If true, this is a cultivated grove or orchard; if false, wild or abandoned."
    shared Boolean cultivated;
    "What kind of tree is in this orchard or grove."
    shared actual String kind;
    "An ID number to identify this orchard or grove."
    shared actual Integer id;
    "The filename of an image to use as an icon for this instance."
    variable String imageFilename = "";
    shared actual String image => imageFilename;
    shared actual void setImage(String image) => imageFilename = image;
    shared actual Grove copy(Boolean zero) {
        Grove retval = Grove(orchard, cultivated, kind, id);
        retval.setImage(image);
        return retval;
    }
    shared actual String defaultImage = (orchard) then "orchard.png" else "tree.png";
    shared actual String shortDesc() =>
            "``(cultivated) then "Cultivated" else "Wild"`` ``kind`` ``(orchard) then
                "orchard" else "grove"``";
    shared actual String string = shortDesc();
    shared actual Boolean equals(Object obj) {
        if (is Grove obj) {
            return kind == obj.kind && orchard == obj.orchard &&
                cultivated == obj.cultivated && id == obj.id;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Grove fixture) {
            return kind == fixture.kind && orchard == fixture.orchard &&
                cultivated == fixture.cultivated;
        } else {
            return false;
        }
    }
    shared actual String plural() => "Groves and orchards";
    shared actual Integer dc = 18;
}