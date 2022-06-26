package common.map.fixtures.mobile;

public class Troll extends SimpleImmortal {
	public Troll(final int id) {
		super("troll", "Trolls", 28, id);
	}

	@Override
	public Troll copy(final CopyBehavior zero) {
		return new Troll(getId());
	}
}
