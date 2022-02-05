package common.map.fixtures.mobile;

public class Simurgh extends SimpleImmortal {
	public Simurgh(final int id) {
		super("simurgh", "Simurghs", 35, id);
	}

	@Override
	public Simurgh copy(final boolean zero) {
		return new Simurgh(getId());
	}
}
