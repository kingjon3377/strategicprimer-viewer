package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public class Minotaur extends SimpleImmortal {
	public Minotaur(final int id) {
		super("minotaur", "Minotaurs", 30, id);
	}

	@Override
	public @NotNull Minotaur copy(final CopyBehavior zero) {
		return new Minotaur(getId());
	}
}
