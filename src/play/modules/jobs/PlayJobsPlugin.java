package play.modules.jobs;

import play.PlayPlugin;
import play.mvc.Router;

public class PlayJobsPlugin extends PlayPlugin {

	/**
	 * On application start.
	 */
	@Override
	public void onApplicationStart() {
		Router.addRoute("GET", "/@jobs", "PlayJobsDashboard.index");
		Router.addRoute("GET", "/@jobs/executeNow", "PlayJobsDashboard.executeNow");
	}

}
