package org.openpixi.pixi.distributed.profile;

import org.openpixi.pixi.profile.Print;
import org.openpixi.pixi.profile.ProfileInfo;

/**
 * Holds the information collected by aspectj profiling.
 * The informartion is ready after the last call to Simulation.setp() method.
 * The fields of this class are filled only when profile profiling is turned on.
 * One can turn on profile profiling by enabling maven profile profile-profile.
 */
public class DistributedProfileInfo extends ProfileInfo {

	private static long arrivingParticlesWaitingTime;
	private static long ghostParticlesWaitingTime;
	private static long ghostCellsWaitingTime;


	public static long getArrivingParticlesWaitingTime() {
		return arrivingParticlesWaitingTime;
	}

	public static void addArrivingParticlesWaitingTime(long value) {
		DistributedProfileInfo.arrivingParticlesWaitingTime += value;
	}

	public static long getGhostParticlesWaitingTime() {
		return ghostParticlesWaitingTime;
	}

	public static void addGhostParticlesWaitingTime(long value) {
		DistributedProfileInfo.ghostParticlesWaitingTime += value;
	}

	public static long getGhostCellsWaitingTime() {
		return ghostCellsWaitingTime;
	}

	public static void addGhostCellsWaitingTime(long value) {
		DistributedProfileInfo.ghostCellsWaitingTime += value;
	}


	public static void printProfileInfo() {
		if (!ProfileInfo.isProfilingOn()) {
			System.out.println("Profiling information was not captured.");
			return;
		}

		ProfileInfo.printProfileInfo();
		Print.partTime("Arriving particles waiting time",
				arrivingParticlesWaitingTime, ProfileInfo.getSimulationTime());
		Print.partTime("Ghost particles waiting time",
				ghostParticlesWaitingTime, ProfileInfo.getSimulationTime());
		Print.partTime("Ghost cells waiting time",
				ghostCellsWaitingTime, ProfileInfo.getSimulationTime());
	}
}
