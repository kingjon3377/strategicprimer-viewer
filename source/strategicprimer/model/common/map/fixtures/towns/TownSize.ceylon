"Sizes of towns, fortifications, and cities."
shared class TownSize of small|medium|large {
    shared static TownSize|ParseException parse(String size) =>
            parseTownSize(size);

    shared actual String string;

    shared Integer ordinal;

    shared new small {
        string = "small";
        ordinal = 0;
    }

    shared new medium {
        string = "medium";
        ordinal = 1;
    }

    shared new large {
        string = "large";
        ordinal = 2;
    }
}

TownSize|ParseException parseTownSize(String size) {
    switch (size)
    case ("small") { return TownSize.small; }
    case ("medium") { return TownSize.medium; }
    case ("large") { return TownSize.large; }
    else { return ParseException("Failed to parse TownSize from '``size``'"); }
}
