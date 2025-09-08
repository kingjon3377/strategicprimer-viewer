package legacy.map.fixtures.mobile;

public final class Djinn extends SimpleImmortal {
	@SuppressWarnings("MagicNumber")
	public Djinn(final int id) {
		super("djinn", "Djinni", 30, id);
	}

	@Override
	public Djinn copy(final CopyBehavior zero) {
		return new Djinn(getId());
	}
}
