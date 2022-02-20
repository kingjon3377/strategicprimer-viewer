package common.map.fixtures.mobile;

public class Kraken extends ImmortalAnimal {
	public Kraken(final int id) {
		super("kraken", "Krakens", 30, id);
	}

	@Override
	public Kraken copy(final CopyBehavior zero) {
		return new Kraken(getId());
	}
}
