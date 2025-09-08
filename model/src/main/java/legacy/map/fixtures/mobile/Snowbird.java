package legacy.map.fixtures.mobile;

public final class Snowbird extends ImmortalAnimal {
	@SuppressWarnings("MagicNumber")
	public Snowbird(final int id) {
		super("snowbird", "Snowbirds", 29, id);
	}

	@Override
	public Snowbird copy(final CopyBehavior zero) {
		return new Snowbird(getId());
	}
}
