package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public final class Kraken extends ImmortalAnimal {
	@SuppressWarnings("MagicNumber")
	public Kraken(final int id) {
		super("kraken", "Krakens", 30, id);
	}

	@Override
	public @NotNull Kraken copy(final CopyBehavior zero) {
		return new Kraken(getId());
	}
}
