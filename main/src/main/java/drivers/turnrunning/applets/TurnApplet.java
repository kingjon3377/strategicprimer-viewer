package drivers.turnrunning.applets;

import java.util.List;
import drivers.common.cli.ICLIHelper;
import drivers.common.cli.Applet;
import common.idreg.IDRegistrar;
import common.map.Point;
import common.map.Player;
import common.map.TileFixture;
import common.map.HasPopulation;
import common.map.HasOwner;

import common.map.fixtures.towns.IFortress;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.Quantity;
import common.map.fixtures.mobile.IUnit;

import drivers.turnrunning.ITurnRunningModel;

import org.jetbrains.annotations.Nullable;

public interface TurnApplet extends Applet {
	@Override
	List<String> getCommands();

	@Override
	String getDescription();

	@Nullable String run();

	@Override
	default void invoke() {
		run();
	}

	default String inHours(int minutes) {
		if (minutes < 60) {
			return String.format("%d minutes", minutes);
		} else {
			return String.format("%d hours, %d minutes", minutes / 60, minutes % 60);
		}
	}
}
