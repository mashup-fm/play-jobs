/** 
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Felipe Oliveira (http://mashup.fm)
 * 
 */
package play.modules.jobs;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import play.Logger;
import play.Play;
import play.exceptions.UnexpectedException;
import play.jobs.Job;
import play.jobs.JobsPlugin;
import play.modules.jobs.PlayJobs.JobEntry;

/**
 * The Class JobsService.
 */
@Path("/jobs")
public class PlayJobsService {

	/**
	 * Gets the scheduled jobs.
	 * 
	 * @return the scheduled jobs
	 */
	@GET
	@Path("/list")
	@Produces("application/json")
	public PlayJobs getScheduledJobs() {
		// Get Jobs Plugin
		JobsPlugin plugin = Play.plugin(JobsPlugin.class);
		if (plugin == null) {
			throw new UnexpectedException("Cannot load jobs plugin!");
		}

		// Get Metrics
		int poolSize = plugin.executor.getPoolSize();
		int activeCount = plugin.executor.getActiveCount();
		long scheduledTaskCount = plugin.executor.getTaskCount();
		int queueSize = plugin.executor.getPoolSize();

		// Get Scheduled Jobs
		List<Job> scheduledJobs = plugin.scheduledJobs;

		// Wrap Pojos
		List<JobEntry> jobEntries = new ArrayList<JobEntry>();
		if (scheduledJobs != null) {
			for (Job j : scheduledJobs) {
				jobEntries.add(new JobEntry(j));
			}
		}

		// Create Data Container
		PlayJobs jobs = new PlayJobs(poolSize, activeCount, scheduledTaskCount, queueSize, jobEntries);
		Logger.info("Scheduled Jobs: %s", jobs);

		// Return Schedule Jobs
		return jobs;
	}

	/**
	 * Trigger.
	 * 
	 * @param jobClass
	 *            the job class
	 */
	@GET
	@Path("/trigger/{jobClass}")
	@Produces("application/json")
	public void triggerJob(@PathParam("jobClass") String jobClass) {
		// Get Plugin
		JobsPlugin plugin = Play.plugin(JobsPlugin.class);

		// Look for Job
		List<Job> scheduledJobs = plugin.scheduledJobs;
		for (Job job : scheduledJobs) {
			// Check Class Match
			if ((job != null) && job.getClass().getName().equals(jobClass)) {
				// Log Debug
				Logger.info("Firing Job: %s", job);

				// Fire Job
				job.now();
			}
		}

		// Job wasn't found
		throw new UnexpectedException(String.format("Couldn't find job %s on current list of scheduled jobs %s", jobClass, scheduledJobs));
	}

}
