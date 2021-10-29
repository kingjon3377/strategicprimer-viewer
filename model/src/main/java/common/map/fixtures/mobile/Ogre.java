package common.map.fixtures.mobile;

public class Ogre extends SimpleImmortal {
	public Ogre(int id) {
		super("ogre", "Ogres", 28, id);
	}

	@Override
	public Ogre copy(boolean zero) {
		return new Ogre(getId());
	}

	@Override
	public String getShortDescription() {
		return "an ogre";
	}
}
