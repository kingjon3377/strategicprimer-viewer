package drivers.turnrunning.applets;

import drivers.common.cli.ICLIHelper;

import legacy.idreg.IDRegistrar;

import drivers.turnrunning.ITurnRunningModel;

import com.google.auto.service.AutoService;

@AutoService(TurnAppletFactory.class)
public final class MiningAppletFactory implements TurnAppletFactory {
	@Override
	public TurnApplet create(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		return new SimpleProductApplet("mine", "Extract mineral resources from the ground", model, cli, idf);
	}
}
