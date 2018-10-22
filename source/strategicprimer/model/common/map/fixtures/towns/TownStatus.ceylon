"Possible statuses of towns, fortifications, and cities."
shared class TownStatus of active|abandoned|burned|ruined {
    shared static TownStatus|ParseException parse(String status) =>
            parseTownStatus(status);

    shared actual String string;

    shared Integer ordinal;

    shared new active {
        string = "active";
        ordinal = 0;
    }
    shared new abandoned {
        string = "abandoned";
        ordinal = 1;
    }
    shared new burned {
        string = "burned";
        ordinal = 2;
    }
    shared new ruined {
        string = "ruined";
        ordinal = 3;
    }
}

TownStatus|ParseException parseTownStatus(String status) {
    switch (status)
    case ("active") { return TownStatus.active; }
    case ("abandoned") { return TownStatus.abandoned; }
    case ("burned") { return TownStatus.burned; }
    case ("ruined") { return TownStatus.ruined; }
    else { return ParseException("Failed to parse TownStatus from '``status``'"); }
}
