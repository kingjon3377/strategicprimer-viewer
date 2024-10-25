package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public class Snowbird extends ImmortalAnimal {
	@SuppressWarnings("MagicNumber")
	public Snowbird(final int id) {
		super("snowbird", "Snowbirds", 29, id);
	}

	@Override
	public @NotNull Snowbird copy(final CopyBehavior zero) {
		return new Snowbird(getId());
	}
}
