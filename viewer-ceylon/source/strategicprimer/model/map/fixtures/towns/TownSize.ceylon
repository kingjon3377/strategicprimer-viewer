"Sizes of towns, fortifications, and cities."
shared class TownSize of small|medium|large {
    shared static TownSize|ParseException parse(String size) =>
            parseTownSize(size);
    shared actual String string;
    shared new small { string = "small"; }
    shared new medium { string = "medium"; }
    shared new large { string = "large"; }
}
TownSize|ParseException parseTownSize(String size) {
    switch (size)
    case ("small") { return TownSize.small; }
    case ("medium") { return TownSize.medium; }
    case ("large") { return TownSize.large; }
    else { return ParseException("Failed to parse TownSize from '``size``'"); }
}