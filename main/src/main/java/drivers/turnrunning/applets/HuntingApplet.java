package drivers.turnrunning.applets;

import drivers.common.cli.ICLIHelper;

import legacy.idreg.IDRegistrar;

import drivers.turnrunning.ITurnRunningModel;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/* package */ class HuntingApplet extends HuntGeneralApplet {
	public HuntingApplet(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		super("fight and process", model, cli, idf);
	}

	@Override
	public String getDescription() {
		return "search for wild animals";
	}

	@Override
	public List<String> getCommands() {
		return Collections.singletonList("hunt");
	}

	@Override
	public @Nullable String run() {
		return impl("hunt", huntingModel::hunt);
	}
}
