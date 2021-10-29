package common.map.fixtures.mobile;

public class Griffin extends SimpleImmortal {
	public Griffin(int id) {
		super("griffin", "Griffins", 28, id);
	}

	@Override
	public Griffin copy(boolean zero) {
		return new Griffin(getId());
	}
}
