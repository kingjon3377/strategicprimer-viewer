package common.map.fixtures.mobile;

import common.map.Player;
import common.map.PlayerImpl;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that proxy-units work properly, initially to aid in debugging an issue with (the port to Java of) the advancement feature.
 */
public class TestProxyUnit {
	private static IUnit createUnitForFirstTest(Player owner) {
		IMutableUnit retval = new Unit(owner, "unitKind", "unitName", 99);
		retval.addMember(new Worker("workerOne", "human", 52));
		retval.addMember(new Worker("workerTwo", "human", 33));
		return retval;
	}
	@Test
	public void testWorkersInProxyUnit() {
		Player owner = new PlayerImpl(1, "playerOne");
		String workerOneName = "workerOne";
		String workerTwoName = "workerTwo";
		int workerOneId = 52;
		int workerTwoId = 33;
		String race = "human";
		int unitId = 99;
		String unitKind = "unitKind";
		String unitName = "unitName";
		ProxyUnit proxy = new ProxyUnit(99);
		for (int i = 0; i < 6; i++) {
			proxy.addProxied(createUnitForFirstTest(owner));
		}
		assertEquals(2, (int) proxy.stream().count(), "Proxy contains two objects");
		assertEquals(2, (int) proxy.stream()
				.filter(IWorker.class::isInstance).map(IWorker.class::cast).count(), "Proxy unit contains two workers");
	}
}
