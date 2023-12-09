package legacy.map.fixtures.mobile;

public class Griffin extends SimpleImmortal {
	public Griffin(final int id) {
		super("griffin", "Griffins", 28, id);
	}

	@Override
	public Griffin copy(final CopyBehavior zero) {
		return new Griffin(getId());
	}
}
