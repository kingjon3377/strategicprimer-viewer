package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public class Djinn extends SimpleImmortal {
	public Djinn(final int id) {
		super("djinn", "Djinni", 30, id);
	}

	@Override
	public @NotNull Djinn copy(final CopyBehavior zero) {
		return new Djinn(getId());
	}
}
