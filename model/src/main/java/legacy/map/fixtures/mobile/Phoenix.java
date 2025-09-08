package legacy.map.fixtures.mobile;

public final class Phoenix extends SimpleImmortal {
	@SuppressWarnings("MagicNumber")
	public Phoenix(final int id) {
		super("phoenix", "Phoenixes", 35, id);
	}

	@Override
	public Phoenix copy(final CopyBehavior zero) {
		return new Phoenix(getId());
	}
}
