package common.map.fixtures.mobile;

public class Thunderbird extends ImmortalAnimal {
	public Thunderbird(final int id) {
		super("thunderbird", "Thunderbirds", 29, id);
	}

	@Override
	public Thunderbird copy(final boolean zero) {
		return new Thunderbird(getId());
	}
}
