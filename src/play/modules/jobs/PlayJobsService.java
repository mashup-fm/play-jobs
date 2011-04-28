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

import static play.modules.jobs.util.Exceptions.logError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import play.classloading.ApplicationClasses.ApplicationClass;
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
	public PlayJobs getJobs() {
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
		List<JobEntry> jobEntries = new ArrayList<JobEntry>();
		List<ApplicationClass> classes = Play.classes.all();
		if (classes != null) {
			// Wrap Pojos
			for (Class clazz : Play.classloader.getAllClasses()) {
				if ((clazz != null) && Job.class.isAssignableFrom(clazz)) {
					jobEntries.add(new JobEntry(clazz));
				}
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
	@Path("/trigger/{jobClass}/${instances}")
	@Produces("application/json")
	public void triggerJob(@PathParam("jobClass") String jobClass, @PathParam("numberOfInstances") Integer instances) {
		// Jobs Plugin
		Play.plugin(JobsPlugin.class);

		// Check Job Class
		if (StringUtils.isBlank(jobClass)) {
			throw new RuntimeException("Invalid Job Class!");
		}

		// Check Number of Instances
		if (instances == null) {
			instances = 1;
		}
		if (instances < 1) {
			instances = 1;
		}

		// Log Debug
		Logger.info("Trigger Job - Class: %s, Number of Instances: %s", jobClass, instances);

		// Trigger Job(s)
		try {
			// Define Class
			Class clazz = Class.forName(jobClass);
			if (clazz == null) {
				throw new RuntimeException("Invalid Job Class: " + jobClass);
			}

			// Define Executor Service
			ExecutorService executor = Executors.newFixedThreadPool(instances);

			// Loop on number of instances needed
			int count = 0;
			for (int i = 0; i < instances; i++) {
				// Add Count (I don't like to mix with "i" to avoid causing bugs
				// when code gets refactored or something that I can't predict)
				count = count + 1;

				// Create Instance
				Object o = clazz.newInstance();

				// Check Instance Type
				if ((o instanceof Job) == false) {
					throw new RuntimeException("Invalid Class Instance: " + o);
				}

				// Log Debug
				Logger.info("Triggering Job: %s", o);

				// Fire Job
				Job job = (Job) o;
				executor.submit((Callable) job);
				Logger.info("%s) Triggered Job: %s", count, job);
			}

		} catch (Throwable t) {
			// Job wasn't found
			logError(t);
			throw new UnexpectedException(String.format("Couldn't trigger job: %s", jobClass));
		}
	}
}
