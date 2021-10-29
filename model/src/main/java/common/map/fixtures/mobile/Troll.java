package common.map.fixtures.mobile;

public class Troll extends SimpleImmortal {
	public Troll(int id) {
		super("troll", "Trolls", 28, id);
	}

	@Override
	public Troll copy(boolean zero) {
		return new Troll(getId());
	}
}
