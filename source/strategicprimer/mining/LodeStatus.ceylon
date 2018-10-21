import ceylon.random {
    Random
}

"The status of a vein of ore or deposit of stone at any given point."
class LodeStatus of
        minimal | veryPoor | poor | fair | good | veryGood | motherLode {
    shared static LodeStatus? parse(String str) => parseLodeStatus(str);

    "The number of parts of other rock per part of ore."
    shared Integer ratio;

    "The probability that an adjacent area will be the next state lower."
    Float lowerProbability;

    "The probability that an adjacent area will be either the next state lower or the
     same state, not the next state higher."
    Float notHigherProbability;

    "Delegating constructor."
    abstract new delegated(Integer qty, Float lowerChance = 0.5,
            Float notHigherChance = 0.8) {
        ratio = qty;
        lowerProbability = lowerChance;
        notHigherProbability = notHigherChance;
    }

    "There is very little ore: one part ore per 16,384 parts other rock."
    shared new minimal extends delegated(16384, 0.4) {}
    "There is quite little ore: one part ore per 4096 parts other rock."
    shared new veryPoor extends delegated(4096) {}
    "There is little ore: one part ore per 1096 parts other rock."
    shared new poor extends delegated(1096) {}
    "There is a fair amount of ore: one part ore per 256 parts other rock."
    shared new fair extends delegated(256) {}
    "There is quite a bit of ore: one part ore per 16 parts other rock."
    shared new good extends delegated(16) {}
    "There is a relatively high ratio of ore: one part ore per 4 parts other rock."
    shared new veryGood extends delegated(4) {}
    "The mother-lode: one part ore per one part other rock."
    shared new motherLode extends delegated(1, 0.7, 1.0) {}

    "The next lower status."
    LodeStatus? lower {
        switch (this)
        case (minimal) { return null; }
        case (veryPoor) { return minimal; }
        case (poor) { return veryPoor; }
        case (fair) { return poor; }
        case (good) { return fair; }
        case (veryGood) { return good; }
        case (motherLode) { return veryGood; }
    }

    "The next higher status."
    LodeStatus? higher {
        switch (this)
        case (minimal) { return veryPoor; }
        case (veryPoor) { return poor; }
        case (poor) { return fair; }
        case (fair) { return good; }
        case (good) { return veryGood; }
        case (veryGood) { return motherLode; }
        case (motherLode) { return motherLode; }
    }

    "Randomly choose a status of a location adjacent to one with this status."
    shared LodeStatus? adjacent("The random-number generator to use" Float() rng) {
        Float rand = rng();
        if (rand < lowerProbability) {
            return lower;
        } else if (rand < notHigherProbability) {
            return this;
        } else {
            return higher;
        }
    }

    """Randomly choose the status of a location horizontally adjacent in a "banded" (e.g.
       sand) mine to one with this status."""
    shared LodeStatus? bandedAdjacent(Random rng) {
        // Since ceylon.random doesn't provide a Gaussian ("normal") distribution method,
        // approximate it by averaging several values, then scaling the result to have
        // mean 0.5. I don't think it has the desired standard deviation of 0.25 anymore,
        // however.
        return adjacent(() {
            assert (exists sum = rng.floats().take(12).reduce(plus));
            return sum / 12;
        });
    }
}

"Given a String, if it is the name of a [[LodeStatus]], return that value;
 otherwise, return null."
LodeStatus? parseLodeStatus(String status) {
    switch (status.lowercased)
    case ("none") { return null; }
    case ("minimal") { return LodeStatus.minimal; }
    case ("verypoor") { return LodeStatus.veryPoor; }
    case ("poor") { return LodeStatus.poor; }
    case ("fair") { return LodeStatus.fair; }
    case ("good") { return LodeStatus.good; }
    case ("verygood") { return LodeStatus.veryGood; }
    case ("motherlode") { return LodeStatus.motherLode; }
    else { return null; }
}
