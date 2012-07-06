package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.assigning.PartitionAssigner;
import org.openpixi.pixi.distributed.assigning.SimplePartitionAssigner;
import org.openpixi.pixi.distributed.ibis.MasterCommunicator;
import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.partitioning.Partitioner;
import org.openpixi.pixi.distributed.partitioning.SimplePartitioner;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.IntBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Distributes the problem and collects the results.
 */
public class Master {

	private MasterCommunicator communicator;
	private int numCellsX;
	private int numCellsY;
	private int numNodes;
	private List<Particle> particles;
	private Grid grid;


	// TODO replace parameters with settings class
	public Master(IbisRegistry registry, int numCellsX, int numCellsY, int numNodes) throws Exception {
		this.numCellsX = numCellsX;
		this.numCellsY = numCellsY;
		this.numNodes = numNodes;

		communicator = new MasterCommunicator(registry);
	}


	public void distributeProblem() throws IOException {
		Partitioner partitioner = new SimplePartitioner();
		IntBox[] partitions = partitioner.partition(numCellsX, numCellsY, numNodes);

		PartitionAssigner assigner = new SimplePartitionAssigner();
		int[] assignment = assigner.assign(partitions, numNodes);

		List<List<Particle>> particlePartitions = partitionParticles(partitions, particles);
		Cell[][][] gridPartitions = partitionGrid(partitions, grid);

		communicator.distribute(partitions, assignment, particlePartitions, gridPartitions);
	}


	public void collectResults() {
	}


	/**
	 * Divides cells according to partitions.
	 */
	private Cell[][][] partitionGrid(IntBox[] partitions, Grid grid) {
		Cell[][][] gridPartitions = new Cell[partitions.length][][];
		for (int i = 0; i < partitions.length; ++i) {
			gridPartitions[i] = getSubgrid(partitions[i], grid);
		}
		return gridPartitions;
	}


	/**
	 * At the borders returns also the extra cells!
	 * Since there is some initialization done on the grid
	 * (density charge interpolation + poisson solver)
	 * we need to distribute also the extra cells.
	 */
	private Cell[][] getSubgrid(IntBox partition, Grid grid) {
		int startX = partition.xmin();
		int startY = partition.xmax();
		int endX = partition.ymin();
		int endY = partition.ymax();
		if (startX == 0) {
			startX -= grid.EXTRA_CELLS_BEFORE_GRID;
		}
		if (endX == numCellsX - 1) {
			endX += grid.EXTRA_CELLS_AFTER_GRID;
		}
		if (startY == 0) {
			startY -= grid.EXTRA_CELLS_BEFORE_GRID;
		}
		if (endY == numCellsY - 1) {
			endY += grid.EXTRA_CELLS_AFTER_GRID;
		}

		Cell[][] subGrid = new Cell[endX - startX + 1][endY - startY + 1];
		for (int x = startX; x <= endX ; x++) {
			for (int y = startY; y < endY; y++) {
				subGrid[x - startX][y - startY] = grid.getCell(x,y);
			}
		}
		return  subGrid;
	}


	/**
	 * Divides particles according to partitions they belong to.
	 */
	private List<List<Particle>> partitionParticles(IntBox[] partitions, List<Particle> particles) {
		List<List<Particle>> particlePartitions = new ArrayList<List<Particle>>();
		for (int i = 0; i < partitions.length; ++i) {
			particlePartitions.add(new ArrayList<Particle>());
		}
		for (Particle p: particles) {
			int partitionIndex = -1;
			for  (int i = 0; i < partitions.length; ++i) {
				if (partitions[i].contains(p.getX(), p.getY())) {
					partitionIndex = i;
				}
			}
			assert partitionIndex != -1;
			particlePartitions.get(partitionIndex).add(p);
		}
		return particlePartitions;
	}
}
