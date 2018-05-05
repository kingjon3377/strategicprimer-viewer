import lovelace.util.common {
	matchingValue
}
"The kinds of stone we know about."
shared class StoneKind
        of limestone|marble|slate|pumice|conglomerate|sandstone|laterite|shale {
    shared static StoneKind|ParseException parse(String stone) =>
            parseStoneKind(stone);
    shared actual String string;
    shared new limestone { string = "limestone"; }
    shared new marble { string = "marble"; }
    shared new slate { string = "slate"; }
    shared new pumice { string = "pumice"; }
    shared new conglomerate { string = "conglomerate"; }
    shared new sandstone { string = "sandstone"; }
    "Laterite should only be found under jungle."
    shared new laterite { string = "laterite"; }
    shared new shale { string = "shale"; }
}
StoneKind|ParseException parseStoneKind(String stone) =>
        `StoneKind`.caseValues.find(matchingValue(stone, StoneKind.string))
            else ParseException("Failed to parse StoneKind from '``stone``");
