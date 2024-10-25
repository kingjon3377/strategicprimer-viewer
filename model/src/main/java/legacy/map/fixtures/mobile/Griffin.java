package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public class Griffin extends SimpleImmortal {
	@SuppressWarnings("MagicNumber")
	public Griffin(final int id) {
		super("griffin", "Griffins", 28, id);
	}

	@Override
	public @NotNull Griffin copy(final CopyBehavior zero) {
		return new Griffin(getId());
	}
}
