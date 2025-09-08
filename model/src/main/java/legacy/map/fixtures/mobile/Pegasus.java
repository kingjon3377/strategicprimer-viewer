package legacy.map.fixtures.mobile;

public final class Pegasus extends ImmortalAnimal {
	@SuppressWarnings("MagicNumber")
	public Pegasus(final int id) {
		super("pegasus", "Pegasi", 29, id);
	}

	@Override
	public Pegasus copy(final CopyBehavior zero) {
		return new Pegasus(getId());
	}
}
