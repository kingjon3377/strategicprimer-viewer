package common.map.fixtures.mobile;

public class Phoenix extends SimpleImmortal {
	public Phoenix(final int id) {
		super("phoenix", "Phoenixes", 35, id);
	}

	@Override
	public Phoenix copy(final boolean zero) {
		return new Phoenix(getId());
	}
}
