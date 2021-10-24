package common.map.fixtures.mobile;

import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.Optional;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import common.map.IFixture;

/**
 * A proxy for corresponding animal populations in different maps.
 */
/* default */ final class ProxyAnimal implements Animal, ProxyFor<Animal> {

	public ProxyAnimal(Animal... proxiedAnimals) {
		animals = new ArrayList<>();
		Stream.of(proxiedAnimals).forEach(animals::add);
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
	public boolean isSubset(IFixture obj, Consumer<String> report) {
		report.accept("isSubset called on ProxyAnimal");
		return false;
	}

	@Override
	public void addProxied(Animal item) {
		if (item == this) {
			return;
		}
		animals.add(item);
	}

	@Override
	public int getBorn() {
		Integer retval = getConsensus(Animal::getBorn);
		return (retval == null) ? -1 : retval;
	}

	@Override
	public Animal copy(boolean zero) {
		return new ProxyAnimal(animals.stream().map((a) -> a.copy(zero)).toArray(Animal[]::new));
	}

	@Override
	public int getId() {
		Integer retval = getConsensus(Animal::getId);
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
		Integer retval = getConsensus(Animal::getPopulation);
		return (retval == null) ? -1 : retval;
	}

	@Override
	public Iterable<Animal> getProxied() {
		return animals;
	}

	@Override
	public Animal reduced(int newPopulation, int newId) {
		return new ProxyAnimal(animals.stream().map((a) -> a.reduced(newPopulation, newId)).toArray(Animal[]::new));
	}

	@Override
	public Animal combined(Animal addend) {
		if (addend instanceof ProxyFor/*<Animal>*/ && ((ProxyFor<Animal>) addend).isParallel()) {
			List<Animal> interim = new ArrayList<>();
			Iterator<Animal> ours = getProxied().iterator();
			Iterator<Animal> theirs = ((ProxyFor<Animal>) addend).getProxied().iterator();
			while (ours.hasNext() && theirs.hasNext()) {
				interim.add(ours.next().combined(theirs.next()));
			}
			if (!ours.hasNext() && theirs.hasNext()) { // sizes match
				return new ProxyAnimal(interim.stream().toArray(Animal[]::new));
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
		Boolean retval = getConsensus(Animal::isTalking);
		return (retval == null) ? false : retval;
	}

	@Override
	public int getDC() {
		Integer retval = getConsensus(Animal::getDC);
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
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Animal) {
			return ((Animal) obj).getId() == getId() && equalsIgnoringID((Animal) obj);
		} else {
			return false;
		}
	}
}

