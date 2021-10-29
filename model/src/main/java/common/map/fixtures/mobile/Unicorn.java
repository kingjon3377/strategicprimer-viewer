package common.map.fixtures.mobile;

public class Unicorn extends ImmortalAnimal {
	public Unicorn(int id) {
		super("unicorn", "Unicorns", 29, id);
	}

	@Override
	public Unicorn copy(boolean zero) {
		return new Unicorn(getId());
	}
}
