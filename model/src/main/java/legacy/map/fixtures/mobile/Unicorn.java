package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public final class Unicorn extends ImmortalAnimal {
	@SuppressWarnings("MagicNumber")
	public Unicorn(final int id) {
		super("unicorn", "Unicorns", 29, id);
	}

	@Override
	public @NotNull Unicorn copy(final CopyBehavior zero) {
		return new Unicorn(getId());
	}
}
