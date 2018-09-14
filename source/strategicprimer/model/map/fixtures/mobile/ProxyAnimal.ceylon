import ceylon.collection {
    MutableList,
    ArrayList
}
import strategicprimer.model.map {
    IFixture
}
import ceylon.test {
    test
}
import lovelace.util.common {
    randomlyGenerated
}

class ProxyAnimal(Animal* proxiedAnimals) satisfies Animal&ProxyFor<Animal> {
    "This class can only be used to represent the corresponding animals in corresponding
     units in different maps."
    shared actual Boolean parallel = true;
    "The animals being proxied."
    MutableList<Animal> animals = ArrayList<Animal>{ elements = proxiedAnimals; };
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        report("isSubset called on ProxyAnimal");
        return false;
    }
    shared actual void addProxied(Animal item) {
        if (item === this) {
            return;
        }
        animals.add(item);
    }
    shared actual Integer born => getConsensus(Animal.born) else -1;
    shared actual Animal copy(Boolean zero) =>
            ProxyAnimal(*animals.map(shuffle(Animal.copy)(zero)));
    shared actual Integer id => getConsensus(Animal.id) else -1;
    shared actual String image => getConsensus(Animal.image) else "";
    shared actual String kind => getConsensus(Animal.kind) else "proxied";
    shared actual Integer population => getConsensus(Animal.population) else -1;
    shared actual {Animal*} proxied => animals;
    shared actual Animal reduced(Integer newPopulation, Integer newId) =>
            ProxyAnimal(*animals.map(shuffle(Animal.reduced)(newPopulation, newId)));
    shared actual Animal combined(Animal addend) {
        if (is ProxyFor<Animal> addend, addend.parallel,
                addend.proxied.size == animals.size) {
            return ProxyAnimal(*zipPairs(animals, addend.proxied)
                .map(unflatten(uncurry(Animal.combined))));
        } else {
            return ProxyAnimal(*animals.map(shuffle(Animal.combined)(addend)));
        }
    }
    shared actual String status => getConsensus(Animal.status) else "proxied";
    shared actual Boolean talking => getConsensus(Animal.talking) else false;
}

test
void testProxyAnimalReduction(randomlyGenerated(3) Integer id,
        randomlyGenerated(1) Integer newId) {
    Animal base = AnimalImpl("test", false, "status", id, -1, 12);
    "The basic [[Animal.reduced]] works the way we expect."
    ProxyAnimal proxy = ProxyAnimal(base, base.copy(false), base.copy(false));
    assert (base.reduced(3, newId).population == 3);
    assert (is ProxyAnimal reduced = proxy.reduced(3, newId));
    for (proxied in reduced.proxied) {
        assert (proxied.id == newId, proxied.population == 3);
    }
}