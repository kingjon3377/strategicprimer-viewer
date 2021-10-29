package common.map.fixtures.mobile;

public class Pegasus extends ImmortalAnimal {
	public Pegasus(int id) {
		super("pegasus", "Pegasi", 29, id);
	}

	@Override
	public Pegasus copy(boolean zero) {
		return new Pegasus(getId());
	}
}
