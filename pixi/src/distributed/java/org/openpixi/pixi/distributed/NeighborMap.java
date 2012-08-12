package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.movement.boundary.BorderRegions;
import org.openpixi.pixi.physics.GeneralBoundaryType;
import org.openpixi.pixi.physics.movement.boundary.BoundaryRegions;
import org.openpixi.pixi.physics.util.IntBox;
import org.openpixi.pixi.physics.util.Point;


/**
 * Maps boundary and border regions to neighbors.
 * We distinguish border neighbors (those to whom we send data)
 * and boundary neighbors (those from which we receive data).
 * These neighbors are the same at the edges but different at the corners.
 *
 * Finds also the direction of the neighbors.
 * The neighbor directions are necessary for
 * correct particle position translation when sending the particle to neighbor
 * and for correct mapping of border cells at one node to ghost cells at other node.
 *
 * While the periodic regions have the directions of their neighbors predefined in the fields
 * boundaryNeighborsDirections and borderNeighborsDirections, for the hardwall regions it is
 * easier to determine the direction based on the region neighbor.
 *
 * For the hardwall regions we can not have the directions predefined as the directions at the
 * corner depend on the layout.
 *
 * On the other hand, we can not find the neighbor direction through the neighbor in periodic
 * boundaries. For example, at the top edge of the global simulation area we want the neighbor
 * direction to point upward and not downward!
 *
 * How do we find the neighbors?
 * =============================
 *
 * First we determine for both types of neighbors (boundary and border) helper points
 * which help us find the neighbors.
 *
 * For each boundary region we specify a point which belongs to the region.
 * Afterwards we search the list of neighbors for the neighbor which contains the point.
 * If such a neighbor is not found, it means that local simulation boundary
 * is also global simulation boundary.
 *
 * Note that the situation differs between hardwall and periodic boundary types.
 * While under periodic boundaries we always have a neighbor on each side,
 * under hardwall boundaries this is not true.
 *
 * For border regions we also have the helper points which identify the potential neighbors.
 * For each edge region one helper point is enough.
 * However, the corner border regions have three helper points
 * since they can have up to three neighbors,
 * depending on boundary type and corner location.
 */
public class NeighborMap {

	/** Dummy neighbor ID signalizing that there is no neighbor. */
	public static final int NO_NEIGHBOR = -1;

	private IntBox[] partitions;
	private int thisWorkerID;
	private IntBox globalSimArea;
	private GeneralBoundaryType boundaryType;

	/** Maps boundary regions to neighbors. */
	private int[] boundaryNeighbors = new int[BoundaryRegions.NUM_OF_REGIONS];
	private Point[] boundaryPoints = new Point[BoundaryRegions.NUM_OF_REGIONS];
	/** Only used for periodic boundaries. */
	private Point[] boundaryNeighborsDirections = new Point[BoundaryRegions.NUM_OF_REGIONS];

	/** Maps border regions to neighbors. */
	private int[][] borderNeighbors = new int[BorderRegions.NUM_OF_REGIONS][];
	private Point[][] borderPoints = new Point[BorderRegions.NUM_OF_REGIONS][];
	/** Only used for periodic boundaries. */
	private Point[][] borderNeighborsDirections = new Point[BorderRegions.NUM_OF_REGIONS][];


	public int getBoundaryNeighbor(int region) {
		return boundaryNeighbors[region];
	}


	public int[] getBorderNeighbors(int region) {
		return borderNeighbors[region];
	}


	public Point getBoundaryNeighborsDirections(int region) {
		if (boundaryType == GeneralBoundaryType.Periodic) {
			return  boundaryNeighborsDirections[region];
		}
		else {
			return getNeighborDirection(getBoundaryNeighbor(region));
		}
	}


	public Point[] getBorderNeighborsDirections(int region) {
		if (boundaryType == GeneralBoundaryType.Periodic) {
			return borderNeighborsDirections[region];
		}
		else
		{
			int[] neighbors = getBorderNeighbors(region);
			Point[] directions = new Point[neighbors.length];
			for (int i = 0; i < neighbors.length; ++i) {
				directions[i] = getNeighborDirection(neighbors[i]);
			}
			return directions;
		}
	}


	public NeighborMap(int thisWorkerID, IntBox[] partitions,
	                   IntBox globalSimArea, GeneralBoundaryType boundaryType) {
		this.partitions = partitions;
		this.thisWorkerID = thisWorkerID;
		this.globalSimArea = globalSimArea;
		this.boundaryType = boundaryType;

		initBoundaryNeighbors();
		initBoundaryPoints(partitions[thisWorkerID]);
		initBoundaryNeighborsDirections(partitions[thisWorkerID]);

		initBorderNeighbors();
		initBorderPoints(partitions[thisWorkerID]);
		initBorderNeighborsDirections(partitions[thisWorkerID]);

		if (boundaryType == GeneralBoundaryType.Hardwall) {
			setHardwallBoundaryNeighbors();
			setHardwallBorderNeighbors();
		} else if (boundaryType == GeneralBoundaryType.Periodic) {
			setPeriodicBoundaryNeighbors();
			setPeriodicBorderNeighbors();
		} else {
			throw new RuntimeException("Unsupported boundary type!");
		}
	}


	private void initBoundaryNeighbors() {
		for (int region = 0; region < BoundaryRegions.NUM_OF_REGIONS; ++region) {
			boundaryNeighbors[region] = NO_NEIGHBOR;
		}
	}


	private void initBorderNeighbors() {
		for (int region = 0; region < BorderRegions.NUM_OF_REGIONS; ++region) {
			borderNeighbors[region] = new int[] {};
		}
	}


	private void initBoundaryPoints(IntBox partition) {

		int xmid = partition.xmin() + partition.xsize() / 2;
		int ymid = partition.ymin() + partition.ysize() / 2;

		boundaryPoints[BoundaryRegions.X_MIN + BoundaryRegions.Y_MIN] =
				new Point(partition.xmin() - 1, partition.ymin() - 1);
		boundaryPoints[BoundaryRegions.X_MIN + BoundaryRegions.Y_CENTER] =
				new Point(partition.xmin() - 1, ymid);
		boundaryPoints[BoundaryRegions.X_MIN + BoundaryRegions.Y_MAX] =
				new Point(partition.xmin() - 1, partition.ymax() + 1);

		boundaryPoints[BoundaryRegions.X_CENTER + BoundaryRegions.Y_MIN] =
				new Point(xmid, partition.ymin() - 1);
		// The point for the center region remains null.
		boundaryPoints[BoundaryRegions.X_CENTER + BoundaryRegions.Y_MAX] =
				new Point(xmid, partition.ymax() + 1);

		boundaryPoints[BoundaryRegions.X_MAX + BoundaryRegions.Y_MIN] =
				new Point(partition.xmax() + 1, partition.ymin() - 1);
		boundaryPoints[BoundaryRegions.X_MAX + BoundaryRegions.Y_CENTER] =
				new Point(partition.xmax() + 1, ymid);
		boundaryPoints[BoundaryRegions.X_MAX + BoundaryRegions.Y_MAX] =
				new Point(partition.xmax() + 1, partition.ymax() + 1);
	}


	private void initBoundaryNeighborsDirections(IntBox partition) {

		boundaryNeighborsDirections[BoundaryRegions.X_MIN + BoundaryRegions.Y_MIN] =
				new Point(-1, -1);
		boundaryNeighborsDirections[BoundaryRegions.X_MIN + BoundaryRegions.Y_CENTER] =
				new Point(-1, 0);
		boundaryNeighborsDirections[BoundaryRegions.X_MIN + BoundaryRegions.Y_MAX] =
				new Point(-1, +1);

		boundaryNeighborsDirections[BoundaryRegions.X_CENTER + BoundaryRegions.Y_MIN] =
				new Point(0, -1);
		boundaryNeighborsDirections[BoundaryRegions.X_CENTER + BoundaryRegions.Y_MAX] =
				new Point(0, +1);

		boundaryNeighborsDirections[BoundaryRegions.X_MAX + BoundaryRegions.Y_MIN] =
				new Point(+1, -1);
		boundaryNeighborsDirections[BoundaryRegions.X_MAX + BoundaryRegions.Y_CENTER] =
				new Point(+1, 0);
		boundaryNeighborsDirections[BoundaryRegions.X_MAX + BoundaryRegions.Y_MAX] =
				new Point(+1, +1);
	}


	private void initBorderPoints(IntBox partition) {

		/*
		 * Corner points - can have up to 3 neighbors => 3 points to identify the neighbors.
		 * The first point always identifies the corner neighbor.
		 */

		borderPoints[BorderRegions.X_BORDER_MIN + BorderRegions.Y_BORDER_MIN] =
				new Point[] {
						new Point(partition.xmin() - 1, partition.ymin() - 1),
						new Point(partition.xmin(), partition.ymin() - 1),
						new Point(partition.xmin() - 1, partition.ymin())
				};

		borderPoints[BorderRegions.X_BORDER_MIN + BorderRegions.Y_BORDER_MAX] =
				new Point[] {
						new Point(partition.xmin() - 1, partition.ymax() + 1),
						new Point(partition.xmin(), partition.ymax() + 1),
						new Point(partition.xmin() - 1, partition.ymax())
				};

		borderPoints[BorderRegions.X_BORDER_MAX + BorderRegions.Y_BORDER_MIN] =
				new Point[] {
						new Point(partition.xmax() + 1, partition.ymin() - 1),
						new Point(partition.xmax(), partition.ymin() - 1),
						new Point(partition.xmax() + 1, partition.ymin())
				};

		borderPoints[BorderRegions.X_BORDER_MAX + BorderRegions.Y_BORDER_MAX] =
				new Point[] {
						new Point(partition.xmax() + 1, partition.ymax() + 1),
						new Point(partition.xmax(), partition.ymax() + 1),
						new Point(partition.xmax() + 1, partition.ymax())
				};

		/*
		 * Outside hardwall corner points (only needed at the edges of the global simulation).
		 * It is important to note that for region X_BORDER_MIN + Y_BOUNDARY_MIN (region 1)
		 * the point which identifies the neighbor is from region X_BOUNDARY_MIN + Y_BORDER_MIN
		 * (region 2). In another words, the data from region 1 needs to be sent to region 2.
		 */

		borderPoints[BorderRegions.X_BORDER_MIN + BorderRegions.Y_BOUNDARY_MIN] =
				new Point[] { new Point(partition.xmin() - 1, partition.ymin()) };
		borderPoints[BorderRegions.X_BORDER_MAX + BorderRegions.Y_BOUNDARY_MIN] =
				new Point[] { new Point(partition.xmax() + 1, partition.ymin()) };

		borderPoints[BorderRegions.X_BORDER_MIN + BorderRegions.Y_BOUNDARY_MAX] =
				new Point[] { new Point(partition.xmin() - 1, partition.ymax()) };
		borderPoints[BorderRegions.X_BORDER_MAX + BorderRegions.Y_BOUNDARY_MAX] =
				new Point[] { new Point(partition.xmax() + 1, partition.ymax()) };

		borderPoints[BorderRegions.X_BOUNDARY_MIN + BorderRegions.Y_BORDER_MIN] =
				new Point[] { new Point(partition.xmin(), partition.ymin() - 1) };
		borderPoints[BorderRegions.X_BOUNDARY_MIN + BorderRegions.Y_BORDER_MAX] =
				new Point[] { new Point(partition.xmin(), partition.ymax() + 1) };

		borderPoints[BorderRegions.X_BOUNDARY_MAX + BorderRegions.Y_BORDER_MIN] =
				new Point[] { new Point(partition.xmax(), partition.ymin() - 1) };
		borderPoints[BorderRegions.X_BOUNDARY_MAX + BorderRegions.Y_BORDER_MAX] =
				new Point[] { new Point(partition.xmax(), partition.ymax() + 1) };

		/*
		 * Edge points.
		 */

		int xmid = partition.xmin() + partition.xsize() / 2;
		int ymid = partition.ymin() + partition.ysize() / 2;

		borderPoints[BorderRegions.X_BORDER_MIN + BorderRegions.Y_CENTER] =
				new Point[] { new Point(partition.xmin() - 1, ymid) };
		borderPoints[BorderRegions.X_BORDER_MAX + BorderRegions.Y_CENTER] =
				new Point[] { new Point(partition.xmax() + 1, ymid) };
		borderPoints[BorderRegions.X_CENTER + BorderRegions.Y_BORDER_MIN] =
				new Point[] { new Point(xmid, partition.ymin() - 1) };
		borderPoints[BorderRegions.X_CENTER + BorderRegions.Y_BORDER_MAX] =
				new Point[] { new Point(xmid, partition.ymax() + 1) };
	}


	private void initBorderNeighborsDirections(IntBox partition) {

		// Corner neighbors directions

		borderNeighborsDirections[BorderRegions.X_BORDER_MIN + BorderRegions.Y_BORDER_MIN] =
				new Point[] {
						new Point(-1,  -1),
						new Point(0, -1),
						new Point(-1, 0)
				};

		borderNeighborsDirections[BorderRegions.X_BORDER_MIN + BorderRegions.Y_BORDER_MAX] =
				new Point[] {
						new Point(-1, +1),
						new Point(0, +1),
						new Point(-1, 0)
				};

		borderNeighborsDirections[BorderRegions.X_BORDER_MAX + BorderRegions.Y_BORDER_MIN] =
				new Point[] {
						new Point(+1, -1),
						new Point(0, -1),
						new Point(+1, 0)
				};

		borderNeighborsDirections[BorderRegions.X_BORDER_MAX + BorderRegions.Y_BORDER_MAX] =
				new Point[] {
						new Point(+1, +1),
						new Point(0, +1),
						new Point(+1, 0)
				};

		// Edge neighbors

		borderNeighborsDirections[BorderRegions.X_BORDER_MIN + BorderRegions.Y_CENTER] =
				new Point[] { new Point(-1, 0) };
		borderNeighborsDirections[BorderRegions.X_BORDER_MAX + BorderRegions.Y_CENTER] =
				new Point[] { new Point(+1, 0) };
		borderNeighborsDirections[BorderRegions.X_CENTER + BorderRegions.Y_BORDER_MIN] =
				new Point[] { new Point(0, -1) };
		borderNeighborsDirections[BorderRegions.X_CENTER + BorderRegions.Y_BORDER_MAX] =
				new Point[] { new Point(0, +1) };
	}


	private void setHardwallBoundaryNeighbors() {

		// Map edge neighbors
		for (int region: BoundaryRegions.EDGE_REGIONS) {
			Point p = boundaryPoints[region];
			int workerID = findNeighborByPoint(p);
			boundaryNeighbors[region] = workerID;
			}

		/*
		 * Map corner regions to neighbors.
		 * If the true corner neighbor does not exist the region should be mapped to
		 * left, right, bottom or top neighbor (if one of them exists;
		 * in the corner of the global simulation area there is no neighbor).
		 */

		for (int region: BoundaryRegions.CORNER_REGIONS) {
			Point p = boundaryPoints[region];
			int workerID = findNeighborByPoint(p);
			if (workerID == NO_NEIGHBOR) {
				workerID = findNeighborAroundPoint(p);
			}
			boundaryNeighbors[region] = workerID;
		}
	}


	private void setHardwallBorderNeighbors() {

		// Map edge neighbors
		for (int region: BorderRegions.EDGE_REGIONS) {
			Point[] points = borderPoints[region];
			assert points.length == 1;
			int workerID = findNeighborByPoint(points[0]);
			borderNeighbors[region] = new int[] {workerID};
			if (workerID == NO_NEIGHBOR) {
				borderNeighborsDirections[region] = new Point[] {};
			}
		}

		// Map corner neighbors
		for (int region: BorderRegions.CORNER_REGIONS) {
			Point[] points = borderPoints[region];
			assert points.length == 3;
			int[] allNeighbors = new int[points.length];
			for (int i = 0; i < points.length; ++i) {
				int workerID = findNeighborByPoint(points[i]);
				allNeighbors[i] = workerID;
				}
			borderNeighbors[region] = allNeighbors;

			// If there is no true corner neighbor we also need to map the border regions
			// lying outside of the simulation area.
			// If there is a true corner we map the outside corner regions to NO_NEIGHBOR.
			for (int i = 1; i < points.length; ++i) {
				int outsideRegion = findOutsideBorderRegionByPoint(points[i]);
				borderNeighbors[outsideRegion] = new int[] {NO_NEIGHBOR};
			}
			if (allNeighbors[0] == NO_NEIGHBOR) {
				for (int i = 1; i < points.length; ++i) {
					if (allNeighbors[i] != NO_NEIGHBOR) {
						int outsideRegion = findOutsideBorderRegionByPoint(points[i]);
						borderNeighbors[outsideRegion][0] = allNeighbors[i];
					}
				}
			}
		}
	}


	private void setPeriodicBoundaryNeighbors() {

		for (int region: BoundaryRegions.EDGE_REGIONS) {
			setSinglePeriodicBoundaryNeighbor(region);
		}

		for (int region: BoundaryRegions.CORNER_REGIONS) {
			setSinglePeriodicBoundaryNeighbor(region);
		}
	}


	private void setSinglePeriodicBoundaryNeighbor(int region) {
		Point p = boundaryPoints[region];
		applyPeriodicBoundary(p);

		int workerID = findNeighborByPoint(p);
		assert workerID != NO_NEIGHBOR;

		boundaryNeighbors[region] = workerID;
	}


	private void setPeriodicBorderNeighbors() {

		for (int region: BorderRegions.EDGE_REGIONS) {
			setSinglePeriodicBorderNeighbor(region);
		}

		for (int region: BorderRegions.CORNER_REGIONS) {
			setSinglePeriodicBorderNeighbor(region);
		}

		// Outside corner regions are not used under periodic boundaries
		for (int region: BorderRegions.OUTSIDE_CORNER_REGIONS) {
			borderNeighbors[region] = new int[] {NO_NEIGHBOR};
		}
	}


	private void setSinglePeriodicBorderNeighbor(int region) {
		Point[] points = borderPoints[region];
		int[] allNeighbors = new int[points.length];

		for (int i = 0; i < points.length; ++i) {
			applyPeriodicBoundary(points[i]);

			int workerID = findNeighborByPoint(points[i]);
			assert workerID != NO_NEIGHBOR;

			allNeighbors[i] = workerID;
		}
		borderNeighbors[region] = allNeighbors;
	}


	private int findOutsideBorderRegionByPoint(Point point) {
		for (int region: BorderRegions.OUTSIDE_CORNER_REGIONS) {
			for (Point p: borderPoints[region]) {
				if (p.equals(point)) {
					return region;
				}
			}
		}
		throw new RuntimeException("Could not find outside corner border region for point: "
				+ point);
	}


	private int findNeighborAroundPoint(Point p) {
		for (Point sp: getSurroundingPoints(p)) {
			int workerID = findNeighborByPoint(sp);
			if (workerID != NO_NEIGHBOR) {
				return workerID;
			}
		}
		return NO_NEIGHBOR;
	}


	private Point[] getSurroundingPoints(Point p) {
		return new Point[] {
				new Point(p.x - 1, p.y), new Point(p.x + 1, p.y),
				new Point(p.x, p.y - 1), new Point(p.x, p.y + 1)
		};
	}


	private int findNeighborByPoint(Point p) {
		for (int i = 0; i < partitions.length; ++i) {
			if (partitions[i].contains(p.x, p.y)) {
				return i;
			}
		}
		return NO_NEIGHBOR;
	}


	private Point getNeighborDirection(int neighbor) {
		if (neighbor == NO_NEIGHBOR) {
			return null;
		}

		IntBox myPart = partitions[thisWorkerID];
		IntBox neighborPart = partitions[neighbor];

		int xdirec = 0;
		int ydirec = 0;

		if (myPart.xmax() < neighborPart.xmin()) {
			xdirec = +1;
		}
		else if (neighborPart.xmax() < myPart.xmin()) {
			xdirec = -1;
		}

		if (myPart.ymax() < neighborPart.ymin()) {
			ydirec = +1;
		}
		else if (neighborPart.ymax() < myPart.ymin()) {
			ydirec = -1;
		}

		return new Point(xdirec, ydirec);
	}


	private void applyPeriodicBoundary(Point p) {

		if (p.x < globalSimArea.xmin()) {
			p.x += globalSimArea.xsize();
		} else if (p.x > globalSimArea.xmax()) {
			p.x -= globalSimArea.xsize();
		}

		if (p.y < globalSimArea.ymin()) {
			p.y += globalSimArea.ysize();
		} else if (p.y > globalSimArea.ymax()) {
			p.y -= globalSimArea.ysize();
		}
	}
}
