package common.map.fixtures.mobile;

public class Unicorn extends ImmortalAnimal {
	public Unicorn(final int id) {
		super("unicorn", "Unicorns", 29, id);
	}

	@Override
	public Unicorn copy(final CopyBehavior zero) {
		return new Unicorn(getId());
	}
}
