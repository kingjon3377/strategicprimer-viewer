package common.map.fixtures.mobile;

public class Kraken extends ImmortalAnimal {
	public Kraken(int id) {
		super("kraken", "Krakens", 30, id);
	}

	@Override
	public Kraken copy(boolean zero) {
		return new Kraken(getId());
	}
}
