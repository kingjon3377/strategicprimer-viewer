package common.map.fixtures.mobile;

public class Thunderbird extends ImmortalAnimal {
	public Thunderbird(int id) {
		super("thunderbird", "Thunderbirds", 29, id);
	}

	@Override
	public Thunderbird copy(boolean zero) {
		return new Thunderbird(getId());
	}
}
