package legacy.map.fixtures.mobile;

public final class Minotaur extends SimpleImmortal {
	@SuppressWarnings("MagicNumber")
	public Minotaur(final int id) {
		super("minotaur", "Minotaurs", 30, id);
	}

	@Override
	public Minotaur copy(final CopyBehavior zero) {
		return new Minotaur(getId());
	}
}
