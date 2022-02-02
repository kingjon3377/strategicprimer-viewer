package drivers.turnrunning.applets;

import drivers.common.cli.ICLIHelper;

import common.idreg.IDRegistrar;

import common.map.fixtures.ResourcePileImpl;
import common.map.fixtures.Quantity;

import java.math.BigDecimal;

import static lovelace.util.Decimalize.decimalize;

import common.map.fixtures.terrain.Forest;

import common.map.Point;
import common.map.HasExtent;
import common.map.TileFixture;

import drivers.turnrunning.ITurnRunningModel;

import com.google.auto.service.AutoService;

@AutoService(TurnAppletFactory.class)
public class WoodcuttingAppletFactory implements TurnAppletFactory {
	@Override
	public TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) {
		return new WoodcuttingApplet(model, cli, idf);
	}
}
