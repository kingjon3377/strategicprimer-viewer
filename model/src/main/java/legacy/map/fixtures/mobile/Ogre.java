package legacy.map.fixtures.mobile;

public final class Ogre extends SimpleImmortal {
	@SuppressWarnings("MagicNumber")
	public Ogre(final int id) {
		super("ogre", "Ogres", 28, id);
	}

	@Override
	public Ogre copy(final CopyBehavior zero) {
		return new Ogre(getId());
	}

	@Override
	public String getShortDescription() {
		return "an ogre";
	}
}
