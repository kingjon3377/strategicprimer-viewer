package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public class Sphinx extends SimpleImmortal {
	public Sphinx(final int id) {
		super("sphinx", "Sphinxes", 35, id);
	}

	@Override
	public @NotNull Sphinx copy(final CopyBehavior zero) {
		return new Sphinx(getId());
	}
}
