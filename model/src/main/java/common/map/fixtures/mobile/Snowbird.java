package common.map.fixtures.mobile;

public class Snowbird extends ImmortalAnimal {
	public Snowbird(int id) {
		super("snowbird", "Snowbirds", 29, id);
	}

	@Override
	public Snowbird copy(boolean zero) {
		return new Snowbird(getId());
	}
}
