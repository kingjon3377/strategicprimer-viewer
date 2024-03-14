package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public class Troll extends SimpleImmortal {
	public Troll(final int id) {
		super("troll", "Trolls", 28, id);
	}

	@Override
	public @NotNull Troll copy(final CopyBehavior zero) {
		return new Troll(getId());
	}
}
