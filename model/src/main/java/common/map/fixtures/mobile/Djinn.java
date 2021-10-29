package common.map.fixtures.mobile;

public class Djinn extends SimpleImmortal {
	public Djinn(int id) {
		super("djinn", "Djinni", 30, id);
	}

	@Override
	public Djinn copy(boolean zero) {
		return new Djinn(getId());
	}
}
