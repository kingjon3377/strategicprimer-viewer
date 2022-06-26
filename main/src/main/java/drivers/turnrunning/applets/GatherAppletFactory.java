package drivers.turnrunning.applets;

import com.google.auto.service.AutoService;
import common.idreg.IDRegistrar;
import drivers.common.cli.ICLIHelper;
import drivers.turnrunning.ITurnRunningModel;

@AutoService(TurnAppletFactory.class)
public class GatherAppletFactory implements TurnAppletFactory {
	@Override
	public TurnApplet create(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		return new GatherApplet(model, cli, idf);
	}
}
