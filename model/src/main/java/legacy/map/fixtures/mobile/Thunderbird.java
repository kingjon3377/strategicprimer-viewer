package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public class Thunderbird extends ImmortalAnimal {
	@SuppressWarnings("MagicNumber")
	public Thunderbird(final int id) {
		super("thunderbird", "Thunderbirds", 29, id);
	}

	@Override
	public @NotNull Thunderbird copy(final CopyBehavior zero) {
		return new Thunderbird(getId());
	}
}
