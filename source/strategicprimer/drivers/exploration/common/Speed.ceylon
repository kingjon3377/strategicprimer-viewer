import strategicprimer.model.common.map {
    HasName
}

"An enumeration of possible movement speeds, joining their effects on MP costs and
 Perception. Traveling to [[Direction.nowhere]] should give an additional bonus (+2?) to
 Perception."
shared class Speed of hurried|normal|observant|careful|meticulous
        satisfies HasName&Comparable<Speed> {
    "The multiplicative modifier to apply to movement costs."
    shared Float mpMultiplier;
    "The modifier to add to Perception checks."
    shared Integer perceptionModifier;
    "A description to use in menus."
    shared actual String name;
    "A description to use in prose text."
    shared String shortName;

    abstract new delegate(Float multMod, Integer addMod, String desc) {
        mpMultiplier = multMod;
        perceptionModifier = addMod;
        String perceptionString = (addMod >= 0) then "+``addMod``" else addMod.string;
        name = "``desc``: x``Float.format(multMod, 0,
            1)`` MP costs, ``perceptionString`` Perception";
        shortName = desc;
    }

    "Traveling as quickly as possible."
    shared new hurried extends delegate(0.66, -6, "Hurried") {}
    "Normal speed."
    shared new normal extends delegate(1.0, -4, "Normal") {}
    "Moving slowly enough to notice one's surroundings."
    shared new observant extends delegate(1.5, -2, "Observant") {}
    "Looking carefully at one's surroundings to try not to miss anything important."
    shared new careful extends delegate(2.0, 0, "Careful") {}
    "Painstaking searches."
    shared new meticulous extends delegate(2.5, 2, "Meticulous") {}

    "A description to use in GUI menus."
    shared actual String string => name;
    shared actual Comparison compare(Speed other) =>
            perceptionModifier <=> other.perceptionModifier;
}
