package legacy.map.fixtures.mobile;

import legacy.map.Player;
import legacy.map.PlayerImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that proxy-units work properly, initially to aid in debugging an issue with (the port to Java of) the advancement feature.
 */
public class TestProxyUnit {
	private static IUnit createUnitForFirstTest(final Player owner) {
		final IMutableUnit retval = new Unit(owner, "unitKind", "unitName", 99);
		retval.addMember(new Worker("workerOne", "human", 52));
		retval.addMember(new Worker("workerTwo", "human", 33));
		return retval;
	}

	@Test
	public void testWorkersInProxyUnit() {
		final Player owner = new PlayerImpl(1, "playerOne");
		final String workerOneName = "workerOne";
		final String workerTwoName = "workerTwo";
		final int workerOneId = 52;
		final int workerTwoId = 33;
		final String race = "human";
		final int unitId = 99;
		final String unitKind = "unitKind";
		final String unitName = "unitName";
		final ProxyUnit proxy = new ProxyUnit(99);
		for (int i = 0; i < 6; i++) {
			proxy.addProxied(createUnitForFirstTest(owner));
		}
		assertEquals(2, (int) proxy.stream().count(), "Proxy contains two objects");
		assertEquals(2, (int) proxy.stream()
				.filter(IWorker.class::isInstance).map(IWorker.class::cast).count(), "Proxy unit contains two workers");
	}
}
