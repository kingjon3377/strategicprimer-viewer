package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public class Simurgh extends SimpleImmortal {
	public Simurgh(final int id) {
		super("simurgh", "Simurghs", 35, id);
	}

	@Override
	public @NotNull Simurgh copy(final CopyBehavior zero) {
		return new Simurgh(getId());
	}
}
