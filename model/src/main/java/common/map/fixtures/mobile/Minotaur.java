package common.map.fixtures.mobile;

public class Minotaur extends SimpleImmortal {
	public Minotaur(int id) {
		super("minotaur", "Minotaurs", 30, id);
	}

	@Override
	public Minotaur copy(boolean zero) {
		return new Minotaur(getId());
	}
}
