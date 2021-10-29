package common.map.fixtures.mobile;

public class Simurgh extends SimpleImmortal {
	public Simurgh(int id) {
		super("simurgh", "Simurghs", 35, id);
	}

	@Override
	public Simurgh copy(boolean zero) {
		return new Simurgh(getId());
	}
}
