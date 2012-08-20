package org.openpixi.pixi.profile;

/**
 * Holds the information collected by aspectj profiling.
 * The informartion is ready after the last call to Simulation.setp() method.
 * The fields of this class are filled only when profile profiling is turned on.
 * One can turn on profile profiling by enabling maven profile profile-profile.
 */
public class ProfileInfo {

	private static long simulationTime;
	private static long pushParticlesTime;
	private static long interpolateToGridTime;
	private static long solveFieldsTime;
	private static long interpolateToParticleTime;

	/** Signalizes whether the profiling information was captured or not */
	private static boolean isProfilingOn;


	public static boolean isProfilingOn() {
		return isProfilingOn;
	}

	public static void setProfilingOn() {
		isProfilingOn = true;
	}

	public static long getSimulationTime() {
		return simulationTime;
	}

	public static void addSimulationTime(long value) {
		simulationTime += value;
	}

	public static long getPushParticlesTime() {
		return pushParticlesTime;
	}

	public static void addPushParticlesTime(long value) {
		pushParticlesTime += value;
	}

	public static long getInterpolateToGridTime() {
		return interpolateToGridTime;
	}

	public static void addInterpolateToGridTime(long value) {
		interpolateToGridTime += value;
	}

	public static long getSolveFieldsTime() {
		return solveFieldsTime;
	}

	public static void addSolveFieldsTime(long value) {
		solveFieldsTime += value;
	}

	public static long getInterpolateToParticleTime() {
		return interpolateToParticleTime;
	}

	public static void addInterpolateToParticleTime(long value) {
		interpolateToParticleTime += value;
	}


	public static void printProfileInfo() {
		if (!isProfilingOn()) {
			System.out.println("Profiling information was not captured.");
			return;
		}

		Print.partTime("Simulation time", simulationTime, simulationTime);
		Print.partTime("Push particles time", pushParticlesTime, simulationTime);
		Print.partTime("Interpolate to grid time", interpolateToGridTime, simulationTime);
		Print.partTime("Solve fields time", solveFieldsTime, simulationTime);
		Print.partTime("Interpolate to particle time", interpolateToParticleTime, simulationTime);
	}
}
