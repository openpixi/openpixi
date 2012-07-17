package org.openpixi.pixi.distributed;

import junit.framework.TestCase;
import org.openpixi.pixi.distributed.partitioning.Partitioner;
import org.openpixi.pixi.distributed.partitioning.SimplePartitioner;
import org.openpixi.pixi.physics.GeneralBoundaryType;
import org.openpixi.pixi.physics.movement.boundary.BoundaryRegions;
import org.openpixi.pixi.physics.util.IntBox;

/**
 * Tests the creation of neighbors under hardwall and periodic boundaries.
 * There are two kinds of tests:
 *
 * 1) GENERIC TESTS - Test the neighbor maps of all partitions for:
 *    - Correct number of potential neighbors at the region in question.
 *      Corner border regions have 3 potential neighbors
 *      while edge border regions or boundary regions have only 1 potential neighbor.
 *    - Valid neighbor id range or dummy NO_NEIGHBOR id.
 *      E.g. periodic corner border regions have always 3 valid neighbors
 *      while the hardwall corner border regions might have 0,1 or 3 valid neighbors.
 *    (Do not verify the specific neighbor values)
 *
 * 2) SPECIFIC TESTS - Test the specific neighbor values for a given region
 *    (verify whether region r really has a neighbor n).
 *    The result of the specific test is dependent on the partitioning!
 *    It expects the following partitioning:
 *    0 2
 *    1 3
 *    4 6
 *    5 7
 */
public class NeighborMapTest extends TestCase {

	// Constants for generic tests - feel free to change them.
	private static final int NUM_CELLS_X_GEN = 64;
	private static final int NUM_CELLS_Y_GEN = 128;
	private static final int NUM_PARTITIONS_GEN = 32;

	// Constants for specific tests - can not be changed!
	private static final int NUM_CELLS_X_SPC = 32;
	private static final int NUM_CELLS_Y_SPC = 32;
	private static final int NUM_PARTITIONS_SPC = 8;

	public void testPeriodicMapsGeneric() {

		// Create partitions
		Partitioner partitioner = new SimplePartitioner();
		IntBox[] partitions = partitioner.partition(NUM_CELLS_X_GEN, NUM_CELLS_Y_GEN, NUM_PARTITIONS_GEN);
		IntBox globalSimulation = new IntBox(0, NUM_CELLS_X_GEN - 1, 0, NUM_CELLS_Y_GEN - 1);

		// Go through each partition and test its neighbor map
		for (int i = 0; i < partitions.length; ++i) {
			NeighborMap neighborMap = new NeighborMap(
					i, partitions, globalSimulation, GeneralBoundaryType.Periodic);
			testSinglePeriodicMap(i, neighborMap, partitions.length);
		}
	}


	public void testHardwallMapsGeneric() {

		// Create partitions
		Partitioner partitioner = new SimplePartitioner();
		IntBox[] partitions = partitioner.partition(NUM_CELLS_X_GEN, NUM_CELLS_Y_GEN, NUM_PARTITIONS_GEN);
		IntBox globalSimulation = new IntBox(0, NUM_CELLS_X_GEN - 1, 0, NUM_CELLS_Y_GEN - 1);

		// Go through each partition and test its neighbor map
		for (int i = 0; i < partitions.length; ++i) {
			NeighborMap neighborMap = new NeighborMap(
					i, partitions, globalSimulation, GeneralBoundaryType.Hardwall);
			testSingleHardwallBoundaryMap(i, partitions, globalSimulation, neighborMap);
			testSingleHardwallBorderMap(i, partitions, globalSimulation, neighborMap);
		}
	}


	public void testPeriodicMapSpecific() {

		// Create partitions
		Partitioner partitioner = new SimplePartitioner();
		IntBox[] partitions = partitioner.partition(NUM_CELLS_X_SPC, NUM_CELLS_Y_SPC, NUM_PARTITIONS_SPC);
		IntBox globalSimulation = new IntBox(0, NUM_CELLS_X_SPC - 1, 0, NUM_CELLS_Y_SPC - 1);

		int myPartID = 0;
		NeighborMap neighborMap = new NeighborMap(
				myPartID, partitions, globalSimulation, GeneralBoundaryType.Periodic);

		// Test boundary corner regions

		int neighbor = neighborMap.getBoundaryNeighbor(
				BoundaryRegions.X_MIN + BoundaryRegions.Y_MIN);
		assertEquals(7, neighbor);

		neighbor = neighborMap.getBoundaryNeighbor(
				BoundaryRegions.X_MAX + BoundaryRegions.Y_MAX);
		assertEquals(3, neighbor);

		neighbor = neighborMap.getBoundaryNeighbor(
				BoundaryRegions.X_MIN + BoundaryRegions.Y_MAX);
		assertEquals(3, neighbor);

		// Test boundary edge regions

		neighbor = neighborMap.getBoundaryNeighbor(
				BoundaryRegions.X_MIN + BoundaryRegions.Y_CENTER);
		assertEquals(2, neighbor);

		neighbor = neighborMap.getBoundaryNeighbor(
				BoundaryRegions.X_MAX + BoundaryRegions.Y_CENTER);
		assertEquals(2, neighbor);

		neighbor = neighborMap.getBoundaryNeighbor(
				BoundaryRegions.X_CENTER + BoundaryRegions.Y_MIN);
		assertEquals(5, neighbor);

		// Test border corner regions

		int[] neighbors = neighborMap.getBorderNeighbors(
				BorderRegions.X_BORDER_MIN + BorderRegions.Y_BORDER_MIN);
		assertContains(neighbors, 2);
		assertContains(neighbors, 5);
		assertContains(neighbors, 7);

		neighbors = neighborMap.getBorderNeighbors(
				BorderRegions.X_BORDER_MAX + BorderRegions.Y_BORDER_MAX);
		assertContains(neighbors, 1);
		assertContains(neighbors, 2);
		assertContains(neighbors, 3);

		// Test border edge regions

		neighbors = neighborMap.getBorderNeighbors(
				BorderRegions.X_CENTER + BorderRegions.Y_BORDER_MIN);
		assertContains(neighbors, 5);

		neighbors = neighborMap.getBorderNeighbors(
				BorderRegions.X_BORDER_MAX + BorderRegions.Y_CENTER);
		assertContains(neighbors, 2);
	}


	public void testHardwallMapSpecific() {

		// Create partitions
		Partitioner partitioner = new SimplePartitioner();
		IntBox[] partitions = partitioner.partition(NUM_CELLS_X_SPC, NUM_CELLS_Y_SPC, NUM_PARTITIONS_SPC);
		IntBox globalSimulation = new IntBox(0, NUM_CELLS_X_SPC - 1, 0, NUM_CELLS_Y_SPC - 1);

		int myPartID = 0;
		NeighborMap neighborMap = new NeighborMap(
				myPartID, partitions, globalSimulation, GeneralBoundaryType.Hardwall);

		// Test boundary corner regions

		int neighbor = neighborMap.getBoundaryNeighbor(
				BoundaryRegions.X_MIN + BoundaryRegions.Y_MIN);
		assertEquals(NeighborMap.NO_NEIGHBOR, neighbor);

		neighbor = neighborMap.getBoundaryNeighbor(
				BoundaryRegions.X_MAX + BoundaryRegions.Y_MAX);
		assertEquals(3, neighbor);

		neighbor = neighborMap.getBoundaryNeighbor(
				BoundaryRegions.X_MIN + BoundaryRegions.Y_MAX);
		assertEquals(1, neighbor);

		// Test boundary edge regions

		neighbor = neighborMap.getBoundaryNeighbor(
				BoundaryRegions.X_MIN + BoundaryRegions.Y_CENTER);
		assertEquals(NeighborMap.NO_NEIGHBOR, neighbor);

		neighbor = neighborMap.getBoundaryNeighbor(
				BoundaryRegions.X_MAX + BoundaryRegions.Y_CENTER);
		assertEquals(2, neighbor);

		neighbor = neighborMap.getBoundaryNeighbor(
				BoundaryRegions.X_CENTER + BoundaryRegions.Y_MAX);
		assertEquals(1, neighbor);

		// Test border corner regions

		int[] neighbors = neighborMap.getBorderNeighbors(
				BorderRegions.X_BORDER_MIN + BorderRegions.Y_BORDER_MAX);
		assertContains(neighbors, 1);
		assertContains(neighbors, NeighborMap.NO_NEIGHBOR);

		neighbors = neighborMap.getBorderNeighbors(
				BorderRegions.X_BORDER_MAX + BorderRegions.Y_BORDER_MAX);
		assertContains(neighbors, 1);
		assertContains(neighbors, 2);
		assertContains(neighbors, 3);

		// Test border outside corner regions

		neighbors = neighborMap.getBorderNeighbors(
				BorderRegions.X_BORDER_MAX + BorderRegions.Y_BOUNDARY_MIN);
		assertContains(neighbors, 2);

		neighbors = neighborMap.getBorderNeighbors(
				BorderRegions.X_BOUNDARY_MAX + BorderRegions.Y_BORDER_MIN);
		assertContains(neighbors, NeighborMap.NO_NEIGHBOR);

		// Test border edge regions

		neighbors = neighborMap.getBorderNeighbors(
				BorderRegions.X_CENTER + BorderRegions.Y_BORDER_MAX);
		assertContains(neighbors, 1);

		neighbors = neighborMap.getBorderNeighbors(
				BorderRegions.X_BORDER_MAX + BorderRegions.Y_CENTER);
		assertContains(neighbors, 2);
	}


	private void assertContains(int[] neighbors, int n) {
		boolean ok = false;
		for (int neighbor: neighbors) {
			if (neighbor == n) {
				ok = true;
			}
		}
		if (!ok) {
			fail("Missing neighbor: " + n + "!");
		}
	}


	private void testSinglePeriodicMap(int workerID, NeighborMap neighborMap, int numOfWorkers) {

		// Boundary regions test

		for (int region = 0; region < BoundaryRegions.NUM_OF_REGIONS; ++region) {
			int neighbor = neighborMap.getBoundaryNeighbor(region);

			String identification = identificationString(
					GeneralBoundaryType.Periodic, workerID, region, "boundary");
			testForAnyNeighbor(
					neighbor, numOfWorkers, identification);
		}

		// Border regions test

		for (int region: BorderRegions.EDGE_REGIONS) {
			int[] neighbors = neighborMap.getBorderNeighbors(region);
			String identification = identificationString(
					GeneralBoundaryType.Periodic, workerID, region, "border-edge");

			testPotentialNeighborsCount(1, neighbors.length, identification);
			testForAnyNeighbor(neighbors[0], numOfWorkers, identification);
		}

		for (int region: BorderRegions.CORNER_REGIONS) {
			int[] neighbors = neighborMap.getBorderNeighbors(region);
			String identification = identificationString(
					GeneralBoundaryType.Periodic, workerID, region, "border-corner");

			testPotentialNeighborsCount(3, neighbors.length, identification);
			for (int neighbor: neighbors) {
				testForAnyNeighbor(neighbor, numOfWorkers, identification);
			}
		}

		for (int region: BorderRegions.OUTSIDE_CORNER_REGIONS) {
			int[] neighbors = neighborMap.getBorderNeighbors(region);
			String identification = identificationString(
					GeneralBoundaryType.Periodic, workerID, region, "border-outside-corner");

			testPotentialNeighborsCount(1, neighbors.length, identification);
			for (int neighbor: neighbors) {
				testForNoNeighbor(neighbor, identification);
			}
		}
	}


	/**
	 * Does not test all the regions.
	 * Tests only one corner and one edge region.
	 */
	private void testSingleHardwallBoundaryMap(
			int myPartID, IntBox[] partitions, IntBox globalSimulation, NeighborMap neighborMap) {

		IntBox myPart = partitions[myPartID];

		// Test upper-left corner

		int region = BoundaryRegions.X_MIN + BoundaryRegions.Y_MIN;
		int neighbor = neighborMap.getBoundaryNeighbor(region);
		String identification = identificationString(
				GeneralBoundaryType.Hardwall, myPartID, region, "boundary-corner");

		if (myPart.xmin() != globalSimulation.xmin() || myPart.ymin() != globalSimulation.ymin()) {

			// If the region is not a simulation corner it must have one neighbor
			testForAnyNeighbor(neighbor, partitions.length, identification);
		} else {

			// At the global simulation corner there should be no neighbor
			testForNoNeighbor(neighbor, identification);
		}

		// Test bottom edge

		region = BoundaryRegions.X_CENTER + BoundaryRegions.Y_MAX;
		neighbor = neighborMap.getBoundaryNeighbor(region);
		identification = identificationString(
				GeneralBoundaryType.Hardwall, myPartID, region, "boundary-edge");

		if (myPart.ymax() != globalSimulation.ymax()) {

			// If the region is not at the bottom of global simulation
			// then it must have one neighbor
			testForAnyNeighbor(neighbor, partitions.length, identification);
		} else {

			testForNoNeighbor(neighbor, identification);
		}
	}


	/**
	 * Does not test all the regions.
	 * Tests only one corner, one outside corner and one edge region.
	 */
	private void testSingleHardwallBorderMap(
			int myPartID, IntBox[] partitions, IntBox globalSimulation, NeighborMap neighborMap) {

		IntBox myPart = partitions[myPartID];

		// Test bottom-right corner

		int region = BorderRegions.X_BORDER_MAX + BorderRegions.Y_BORDER_MAX;
		int[] neighbors = neighborMap.getBorderNeighbors(region);
		String identification = identificationString(
				GeneralBoundaryType.Hardwall, myPartID, region, "border-corner");

		testPotentialNeighborsCount(3, neighbors.length,  identification);

		if (myPart.xmax() == globalSimulation.xmax() && myPart.ymax() == globalSimulation.ymax()) {

			// If the region is at global simulation corner it can not have any neighbors
			for (int neighbor: neighbors) {
				testForNoNeighbor(neighbor, identification);
			}
		}
		else if (myPart.xmax() == globalSimulation.xmax() || myPart.ymax() == globalSimulation.ymax()) {

			// If the region is not at global simulation corner but at its edge
			// it must have exactly one neighbor
			testForSingleNeighbor(neighbors, partitions.length, identification);
		} else {

			// If the region is inside the global simulation it must have exactly 3 neighbors
			for (int neighbor: neighbors) {
				testForAnyNeighbor(neighbor, partitions.length, identification);
			}
		}

		// Test right-top outside corner

		region = BorderRegions.X_BOUNDARY_MAX + BorderRegions.Y_BORDER_MIN;
		neighbors = neighborMap.getBorderNeighbors(region);
		identification = identificationString(
				GeneralBoundaryType.Hardwall, myPartID, region, "border-outside-corner");

		testPotentialNeighborsCount(1, neighbors.length,  identification);

		if (myPart.xmax() == globalSimulation.xmax() && myPart.ymin() != globalSimulation.ymin()) {
			testForAnyNeighbor(neighbors[0], partitions.length, identification);
		}
		else {
			testForNoNeighbor(neighbors[0], identification);
		}

		// Test left edge

		region = BorderRegions.X_BORDER_MIN + BorderRegions.Y_CENTER;
		neighbors = neighborMap.getBorderNeighbors(region);
		identification = identificationString(
				GeneralBoundaryType.Hardwall, myPartID, region, "border-edge");

		testPotentialNeighborsCount(1, neighbors.length,  identification);

		if (myPart.xmin() != globalSimulation.xmin()) {
			testForAnyNeighbor(neighbors[0], partitions.length, identification);
		}
		else {
			testForNoNeighbor(neighbors[0], identification);
		}
	}


	private void testPotentialNeighborsCount(int expected, int actual, String identification) {
		if (actual < expected) {
			myFail(identification, "Too few potential neighbors!");
		}
		else if (actual > expected) {
			myFail(identification, "Too many potential neighbors!");
		}
	}


	private void testForSingleNeighbor(int[] neighbors, int numOfWorkers, String identification) {
		int realNeighbor = NeighborMap.NO_NEIGHBOR;
		for (int neighbor: neighbors) {
			if (neighbor != NeighborMap.NO_NEIGHBOR) {
				if (realNeighbor != NeighborMap.NO_NEIGHBOR) {
					myFail(identification, "Two valid neighbors!");
				}
				realNeighbor = neighbor;
			}
		}
		testForAnyNeighbor(realNeighbor, numOfWorkers, identification);
	}


	private void testForNoNeighbor(int neighbor, String identification) {
		if (neighbor != NeighborMap.NO_NEIGHBOR) {
			myFail(identification, "Found neighbor!");
		}
	}


	private void testForAnyNeighbor(int neighbor, int numOfWorkers, String identification) {
		if (neighbor == NeighborMap.NO_NEIGHBOR) {
			myFail(identification, "No neighbor detected!");
		}
		else if (neighbor < 0) {
			myFail(identification, "Negative neighbor id!");
		}
		else if (neighbor >= numOfWorkers) {
			myFail(identification, "Neighbor ID too large!");
		}
	}


	/**
	 * Identifies the tested partition, region and boundary.
	 */
	private String identificationString(
			GeneralBoundaryType boundaryType, int workerID, int regionID, String regionType) {
		return "Boundary: " + boundaryType + ", Partition: " + workerID + ", Region ID: " + regionID +
				", Region type: " + regionType;
	}


	private void myFail(String identification, String msg) {
		fail(identification + "\n" + msg);
	}
}
