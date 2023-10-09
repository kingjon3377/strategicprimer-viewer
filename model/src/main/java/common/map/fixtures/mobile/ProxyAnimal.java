package common.map.fixtures.mobile;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.Optional;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import common.map.IFixture;

/**
 * A proxy for corresponding animal populations in different maps.
 *
 * @deprecated We're trying to get rid of the notion of 'proxies' in favor of
 * driver model methods.
 */
@Deprecated
/* default */ final class ProxyAnimal implements AnimalProxy {

    public ProxyAnimal(final Animal... proxiedAnimals) {
        animals = new ArrayList<>(Arrays.asList(proxiedAnimals));
    }

    /**
     * This class can only be used to represent the corresponding animals
     * in corresponding units in different maps.
     */
    @Override
    public boolean isParallel() {
        return true;
    }

    /**
     * The animals being proxied.
     */
    private final List<Animal> animals;

    @Override
    public boolean isSubset(final IFixture obj, final Consumer<String> report) {
        report.accept("isSubset called on ProxyAnimal");
        return false;
    }

    @Override
    public void addProxied(final Animal item) {
        if (item == this) {
            return;
        }
        animals.add(item);
    }

    @Override
    public int getBorn() {
        final Integer retval = getConsensus(Animal::getBorn);
        return (retval == null) ? -1 : retval;
    }

    @Override
    public Animal copy(final CopyBehavior zero) {
        return new ProxyAnimal(animals.stream().map((a) -> a.copy(zero)).toArray(Animal[]::new));
    }

    @Override
    public int getId() {
        final Integer retval = getConsensus(Animal::getId);
        return (retval == null) ? -1 : retval;
    }

    @Override
    public String getImage() {
        return Optional.ofNullable(getConsensus(Animal::getImage)).orElse("");
    }

    @Override
    public String getKind() {
        return Optional.ofNullable(getConsensus(Animal::getKind)).orElse("proxied");
    }

    @Override
    public int getPopulation() {
        final Integer retval = getConsensus(Animal::getPopulation);
        return (retval == null) ? -1 : retval;
    }

    @Override
    public Collection<Animal> getProxied() {
        return animals;
    }

    @Override
    public Animal reduced(final int newPopulation, final int newId) {
        return new ProxyAnimal(animals.stream().map((a) -> a.reduced(newPopulation, newId)).toArray(Animal[]::new));
    }

    @Override
    public Animal combined(final Animal addend) {
        if (addend instanceof final AnimalProxy ap && ap.isParallel()) {
            final List<Animal> interim = new ArrayList<>();
            final Iterator<Animal> ours = getProxied().iterator();
            final Iterator<Animal> theirs = ap.getProxied().iterator();
            while (ours.hasNext() && theirs.hasNext()) {
                interim.add(ours.next().combined(theirs.next()));
            }
            if (!ours.hasNext() && theirs.hasNext()) { // sizes match
                return new ProxyAnimal(interim.toArray(Animal[]::new));
            }
        }
        return new ProxyAnimal(animals.stream().map((a) -> a.combined(addend)).toArray(Animal[]::new));
    }

    @Override
    public String getStatus() {
        return Optional.ofNullable(getConsensus(Animal::getStatus)).orElse("proxied");
    }

    @Override
    public boolean isTalking() {
        final Boolean retval = getConsensus(Animal::isTalking);
        return retval != null && retval;
    }

    @Override
    public int getDC() {
        final Integer retval = getConsensus(Animal::getDC);
        return (retval == null) ? 22 : retval;
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public String toString() {
        return getShortDescription();
    }

    /**
     * An object is equal if it is an animal with equal kind, either both
     * or neither are talking, and their IDs are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof final Animal a) {
            return a.getId() == getId() && equalsIgnoringID(a);
        } else {
            return false;
        }
    }
}

