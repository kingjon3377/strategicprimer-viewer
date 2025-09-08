package legacy.map.fixtures.mobile;

public final class Troll extends SimpleImmortal {
	@SuppressWarnings("MagicNumber")
	public Troll(final int id) {
		super("troll", "Trolls", 28, id);
	}

	@Override
	public Troll copy(final CopyBehavior zero) {
		return new Troll(getId());
	}
}
