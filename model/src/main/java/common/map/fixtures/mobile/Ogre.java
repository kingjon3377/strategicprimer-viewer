package common.map.fixtures.mobile;

public class Ogre extends SimpleImmortal {
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
