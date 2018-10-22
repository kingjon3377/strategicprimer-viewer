import strategicprimer.model.common.map.fixtures {
    UnitMember
}

"A (marker) interface for centaurs, trolls, ogres, fairies, and the like."
shared interface Immortal satisfies MobileFixture&UnitMember {
    "Clone the object."
    shared actual formal Immortal copy(Boolean zero);
}

"A list of immortals that are currently represented as [[animals|Animal]] but that may
 transition to individual subclasses of [[Immortal]] in the near future. This is provided
 so XML-reading code can *now* start to *accept* that idiom, and so not break when given a
 map written after the XML-writing code starts to produce it."
shared {String+} immortalAnimals = ["snowbird", "thunderbird", "pegasus", "unicorn",
    "kraken"];
