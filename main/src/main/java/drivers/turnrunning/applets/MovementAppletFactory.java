package drivers.turnrunning.applets;

import drivers.common.cli.ICLIHelper;

import common.map.fixtures.towns.IFortress;

import common.map.HasOwner;
import common.map.Point;

import drivers.exploration.ExplorationCLIHelper;

import common.idreg.IDRegistrar;

import common.map.fixtures.mobile.IUnit;

import common.map.fixtures.IResourcePile;

import java.util.ArrayList;
import java.util.List;

import drivers.turnrunning.ITurnRunningModel;

import com.google.auto.service.AutoService;

@AutoService(TurnAppletFactory.class)
public class MovementAppletFactory implements TurnAppletFactory {
	@Override
	public TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) {
		return new MovementApplet(model, cli, idf);
	}
}
