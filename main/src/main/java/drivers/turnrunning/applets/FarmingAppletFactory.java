package drivers.turnrunning.applets;

import drivers.common.cli.ICLIHelper;

import common.idreg.IDRegistrar;

import drivers.resourceadding.ResourceAddingCLIHelper;

import drivers.turnrunning.ITurnRunningModel;

import com.google.auto.service.AutoService;

@AutoService(TurnAppletFactory.class)
public class FarmingAppletFactory implements TurnAppletFactory {
	@Override
	public TurnApplet create(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		return new SimpleProductApplet("farm",
			"Plant, weed or prune, or harvest a field, meadow, or orchard", model, cli, idf);
	}
}
