package legacy.map.fixtures.mobile;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.Optional;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import legacy.map.IFixture;
import org.jetbrains.annotations.NotNull;

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
		return Objects.requireNonNullElse(retval, -1);
	}

	@Override
	public @NotNull Animal copy(final CopyBehavior zero) {
		return new ProxyAnimal(animals.stream().map((a) -> a.copy(zero)).toArray(Animal[]::new));
	}

	@Override
	public int getId() {
		final Integer retval = getConsensus(Animal::getId);
		return Objects.requireNonNullElse(retval, -1);
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
		return Objects.requireNonNullElse(retval, -1);
	}

	@Override
	public Collection<Animal> getProxied() {
		return Collections.unmodifiableList(animals);
	}

	@Override
	public Animal reduced(final int newPopulation, final int newId) {
		return new ProxyAnimal(animals.stream().map((a) -> a.reduced(newPopulation, newId)).toArray(Animal[]::new));
	}

	@Override
	public Animal combined(final Animal addend) {
		if (addend instanceof final AnimalProxy ap && ap.isParallel()) {
			final Collection<Animal> interim = new ArrayList<>();
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
		return Boolean.TRUE.equals(retval);
	}

	@SuppressWarnings("MagicNumber")
	@Override
	public int getDC() {
		final Integer retval = getConsensus(Animal::getDC);
		return Objects.requireNonNullElse(retval, 22);
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

