import lovelace.util.common {
    todo
}
"Kinds of minerals whose events the program knows about."
deprecated(" We now use free-form strings for minerals' kinds.")
todo("Load a list of kinds from file")
shared class MineralKind of iron|copper|gold|silver|coal {
    shared actual String string;
    shared new iron { string = "iron"; }
    shared new copper { string = "copper"; }
    shared new gold { string = "gold"; }
    shared new silver { string = "silver"; }
    shared new coal { string = "coal"; }
}