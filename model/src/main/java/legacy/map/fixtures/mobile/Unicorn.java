package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public class Unicorn extends ImmortalAnimal {
	public Unicorn(final int id) {
		super("unicorn", "Unicorns", 29, id);
	}

	@Override
	public @NotNull Unicorn copy(final CopyBehavior zero) {
		return new Unicorn(getId());
	}
}
