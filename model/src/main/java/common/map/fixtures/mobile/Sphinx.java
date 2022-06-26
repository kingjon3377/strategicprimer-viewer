package common.map.fixtures.mobile;

public class Sphinx extends SimpleImmortal {
	public Sphinx(final int id) {
		super("sphinx", "Sphinxes", 35, id);
	}

	@Override
	public Sphinx copy(final CopyBehavior zero) {
		return new Sphinx(getId());
	}
}
