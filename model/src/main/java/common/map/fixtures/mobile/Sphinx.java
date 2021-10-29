package common.map.fixtures.mobile;

public class Sphinx extends SimpleImmortal {
	public Sphinx(int id) {
		super("sphinx", "Sphinxes", 35, id);
	}

	@Override
	public Sphinx copy(boolean zero) {
		return new Sphinx(getId());
	}
}
