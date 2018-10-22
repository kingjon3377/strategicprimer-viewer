import lovelace.util.common {
    readFileContents,
    narrowedStream,
    todo
}

"A helper to load tab-separated data from file."
object fileSplitter {
    "Split a line on its first tab."
    {String+} splitOnFirstTab(String line) => line.split('\t'.equals, true, true, 1);
    "Convert the results of splitting a line into an Entry, with the first
     field as the key and passing the second (presumed only) field through the
     provided method to get the item."
    String->Type lineToEntry<Type>({String+} line, Type(String) factory) =>
            line.first->factory(line.rest.first else "");
    "Read a tab-separated file from either the filesystem or this module's
     classpath, and return its contents as a Map from keys (the first field) to
     values (the remainder passed through the provided factory)."
    shared Map<String, Type> getFileContents<Type, BroaderType=Type>(String filename,
            BroaderType(String) factory) given Type satisfies BroaderType {
        assert (exists textContent =
                readFileContents(`module strategicprimer.model.common`, filename));
        return map(narrowedStream<String, Type>(textContent.split('\n'.equals)
            .map(splitOnFirstTab)
            .map(shuffle(curry(lineToEntry<BroaderType>))(factory))));
    }
}

"A model, loaded from file, of the ages at which young animals become adults.
 We also, for lack of a better way of making it available where necessary in
 the codebase, store a notion of the current turn here."
shared object maturityModel {
    shared Map<String, Integer> maturityAges =
            fileSplitter.getFileContents<Integer, Integer|ParseException>(
                "animal_data/maturity.txt", Integer.parse);
    variable Integer currentTurnLocal = -1;

    shared Integer currentTurn => currentTurnLocal;

    assign currentTurn {
        if (currentTurnLocal < 0) {
            currentTurnLocal = currentTurn;
        }
    }

    "Clear the stored current turn"
    restricted shared void resetCurrentTurn() => currentTurnLocal = -1;
}

"Discovery DCs for animal populations based on the kind of animal, loaded from
 file, and if not present there defaulting to what had been the flat constant
 DC for the [[Animal]] type."
todo("While better than a per-*class* constant, this is still an inferior solution:
      instead, load animals' *categories* (bird, fish, general), *size* categories, and
      stealthiness modifiers, then use those and the number of animals in the population
      to *calculate* the DC.")
shared object animalDiscoveryDCs satisfies Correspondence<String, Integer> {
    Map<String, Integer> dcs =
            fileSplitter.getFileContents<Integer, Integer|ParseException>(
                "animal_data/discovery_dc.txt", Integer.parse);

    shared actual Integer get(String key) => dcs[key] else 22;

    shared actual Boolean defines(String key) => dcs.defines(key);
}

"Plurals for various kinds of animals, loaded from file, so that a population
 of multiple animals can be stored with a singular kind but be presented to the
 user with the proper plural. If one is not found, it defaults to the same as
 the singular."
shared object animalPlurals satisfies Correspondence<String, String> {
    Map<String, String> plurals =
            fileSplitter.getFileContents<String>(
                "animal_data/plurals.txt", identity);

    shared actual String get(String key) => plurals[key] else key;

    shared actual Boolean defines(String key) => plurals.defines(key);
}
