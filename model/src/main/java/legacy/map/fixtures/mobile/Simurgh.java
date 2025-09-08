package legacy.map.fixtures.mobile;

public final class Simurgh extends SimpleImmortal {
	@SuppressWarnings("MagicNumber")
	public Simurgh(final int id) {
		super("simurgh", "Simurghs", 35, id);
	}

	@Override
	public Simurgh copy(final CopyBehavior zero) {
		return new Simurgh(getId());
	}
}
