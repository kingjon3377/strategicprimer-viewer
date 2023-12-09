package legacy.map.fixtures.mobile;

public class Pegasus extends ImmortalAnimal {
	public Pegasus(final int id) {
		super("pegasus", "Pegasi", 29, id);
	}

	@Override
	public Pegasus copy(final CopyBehavior zero) {
		return new Pegasus(getId());
	}
}
