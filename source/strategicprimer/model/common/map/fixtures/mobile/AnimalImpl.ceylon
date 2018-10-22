import lovelace.util.common {
    todo
}
import strategicprimer.model.common.map {
    HasMutableImage
}

"An animal or population of animals in the map."
shared class AnimalImpl(kind, talking, status, id, born = -1, population = 1)
        satisfies Animal&HasMutableImage {
    "ID number."
    shared actual Integer id;

    "Whether this is a talking animal."
    shared actual Boolean talking;

    "The domestication status of the animal."
    shared actual variable String status;

    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";

    "What kind of animal this is"
    shared actual String kind;

    "The turn the animal was born, or -1 if it is an adult (or if this is traces ...)"
    shared actual variable Integer born;

    "How many individual animals are in the population this represents."
    shared actual Integer population;

    "Clone the animal."
    shared actual Animal copy(Boolean zero) {
        AnimalImpl retval = AnimalImpl(kind, talking, status, id,
            (zero) then -1 else born, (zero) then 1 else population); // TODO: change, here and elsewhere, so that "unknown" is -1 population
        retval.image = image;
        return retval;
    }

    shared actual Animal reduced(Integer newPopulation, Integer newId) =>
            AnimalImpl(kind, talking, status, newId, born, newPopulation);

    shared actual Animal combined(Animal addend) =>
            AnimalImpl(kind, talking, status, id, born,
                Integer.largest(0, population) + Integer.largest(0, addend.population));

    "Required Perception check result to find the animal."
    todo("Should be based on population size as well as animal kind")
    shared actual Integer dc => animalDiscoveryDCs[kind] else 22;
}
