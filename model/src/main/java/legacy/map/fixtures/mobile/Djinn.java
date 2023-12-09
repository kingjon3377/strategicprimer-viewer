package legacy.map.fixtures.mobile;

public class Djinn extends SimpleImmortal {
	public Djinn(final int id) {
		super("djinn", "Djinni", 30, id);
	}

	@Override
	public Djinn copy(final CopyBehavior zero) {
		return new Djinn(getId());
	}
}
