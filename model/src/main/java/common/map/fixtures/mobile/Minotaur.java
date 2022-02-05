package common.map.fixtures.mobile;

public class Minotaur extends SimpleImmortal {
	public Minotaur(final int id) {
		super("minotaur", "Minotaurs", 30, id);
	}

	@Override
	public Minotaur copy(final boolean zero) {
		return new Minotaur(getId());
	}
}
