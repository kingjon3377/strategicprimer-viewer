package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public final class Phoenix extends SimpleImmortal {
	@SuppressWarnings("MagicNumber")
	public Phoenix(final int id) {
		super("phoenix", "Phoenixes", 35, id);
	}

	@Override
	public @NotNull Phoenix copy(final CopyBehavior zero) {
		return new Phoenix(getId());
	}
}
