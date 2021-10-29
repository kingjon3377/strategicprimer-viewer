package common.map.fixtures.mobile;

public class Phoenix extends SimpleImmortal {
	public Phoenix(int id) {
		super("phoenix", "Phoenixes", 35, id);
	}

	@Override
	public Phoenix copy(boolean zero) {
		return new Phoenix(getId());
	}
}
