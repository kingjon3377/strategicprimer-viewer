package legacy.map.fixtures.mobile;

public class Snowbird extends ImmortalAnimal {
	public Snowbird(final int id) {
		super("snowbird", "Snowbirds", 29, id);
	}

	@Override
	public Snowbird copy(final CopyBehavior zero) {
		return new Snowbird(getId());
	}
}
