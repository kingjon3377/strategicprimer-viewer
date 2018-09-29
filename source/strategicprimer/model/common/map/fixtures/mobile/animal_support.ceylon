import lovelace.util.common {
    readFileContents,
    narrowedStream,
    todo
}
object fileSplitter {
    {String+} splitOnFirstTab(String line) => line.split('\t'.equals, true, true, 1);
    String->Type lineToEntry<Type>({String+} line, Type(String) factory) =>
            line.first->factory(line.rest.first else "");
    shared Map<String, Type> getFileContents<Type, BroaderType=Type>(String filename,
            BroaderType(String) factory) given Type satisfies BroaderType {
        assert (exists textContent =
                readFileContents(`module strategicprimer.model.common`, filename));
        return map(narrowedStream<String, Type>(textContent.split('\n'.equals)
            .map(splitOnFirstTab)
            .map(shuffle(curry(lineToEntry<BroaderType>))(factory))));
    }
}
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
shared object animalPlurals satisfies Correspondence<String, String> {
    Map<String, String> plurals =
            fileSplitter.getFileContents<String>(
                "animal_data/plurals.txt", identity);
    shared actual String get(String key) => plurals[key] else key;
    shared actual Boolean defines(String key) => plurals.defines(key);

}
