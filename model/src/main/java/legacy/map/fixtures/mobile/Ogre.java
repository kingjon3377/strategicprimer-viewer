package legacy.map.fixtures.mobile;

import org.jetbrains.annotations.NotNull;

public final class Ogre extends SimpleImmortal {
	@SuppressWarnings("MagicNumber")
	public Ogre(final int id) {
		super("ogre", "Ogres", 28, id);
	}

	@Override
	public @NotNull Ogre copy(final CopyBehavior zero) {
		return new Ogre(getId());
	}

	@Override
	public String getShortDescription() {
		return "an ogre";
	}
}
