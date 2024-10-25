package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public class Pegasus extends ImmortalAnimal {
	@SuppressWarnings("MagicNumber")
	public Pegasus(final int id) {
		super("pegasus", "Pegasi", 29, id);
	}

	@Override
	public @NotNull Pegasus copy(final CopyBehavior zero) {
		return new Pegasus(getId());
	}
}
