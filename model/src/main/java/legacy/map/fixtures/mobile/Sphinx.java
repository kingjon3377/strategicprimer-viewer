package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public final class Sphinx extends SimpleImmortal {
	@SuppressWarnings("MagicNumber")
	public Sphinx(final int id) {
		super("sphinx", "Sphinxes", 35, id);
	}

	@Override
	public @NotNull Sphinx copy(final CopyBehavior zero) {
		return new Sphinx(getId());
	}
}
