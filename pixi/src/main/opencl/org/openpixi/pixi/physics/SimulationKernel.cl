//Particle attributes indexes
#define P_SIZE 22

#define X 0
#define Y 1
#define Radius 2
#define Vx 3
#define Vy 4
#define Ax 5
#define Ay 6
#define Mass 7
#define Charge 8
#define PrevX 9
#define PrevY 10
#define Ex 11
#define Ey 12
#define PBz 13
#define PrevPositionComponentForceX 14
#define PrevPositionComponentForceY 15
#define PrevTangentVelocityComponentOfForceX 16
#define PrevTangentVelocityComponentOfForceY 17
#define PrevNormalVelocityComponentOfForceX 18
#define PrevNormalVelocityComponentOfForceY 19
#define PrevBz 20
#define PrevLinearDragCoefficient 21


//Force attributes indexes
#define ForceX 0
#define ForceY 1
#define PositionComponentofForceX 2
#define PositionComponentofForceY 3
#define TangentVelocityComponentOfForceX 4
#define TangentVelocityComponentOfForceY 5
#define NormalVelocityComponentofForceX 6
#define NormalVelocityComponentofForceY 7
#define Bz 8
#define LinearDragCoefficient 9

//boundary
#define X_MIN 0
#define X_CENTER 1
#define X_MAX 2
#define Y_MIN 0
#define Y_CENTER 3
#define Y_MAX 6

//Grid details
#define EXTRA_CELLS_BEFORE_GRID 2
#define EXTRA_CELLS_AFTER_GRID 2

//Cell attributes indexes
#define C_SIZE 8

#define Cjx 0
#define Cjy 1
#define Crho 2
#define Cphi 3
#define Cex 4
#define Cey 5
#define Cbz 6
#define Cbzo 7


int get_region(double xmin, double xmax, double ymin, double ymax, double width, double height) {
    int xidx;
    int yidx;

    if (xmin < 0) {
            xidx  = X_MIN;
    } else if (xmax >= width) {
            xidx = X_MAX;
    } else {
            xidx = X_CENTER;
    }

    if (ymin < 0) {
            yidx = Y_MIN;
    } else if (ymax >= height) {
            yidx = Y_MAX;
    } else {
            yidx = Y_CENTER;
    }

    return xidx + yidx;
}

double formFactor(double A, double B, double C, double D,
			double a, double b, double c, double d){
		return A*b*d + B*b*c + C*a*c + D*a*d;
}


void createBoundaryCell(int x, int y, int numCellsX, int numCellsY, __global double *cells) {
    int xmin = EXTRA_CELLS_BEFORE_GRID;
    int xmax = numCellsX + EXTRA_CELLS_BEFORE_GRID - 1;
    int ymin = EXTRA_CELLS_BEFORE_GRID;
    int ymax = numCellsY + EXTRA_CELLS_BEFORE_GRID - 1;

    int refX = x;
    int refY = y;
    if (x < xmin) {
            refX += numCellsX;
    } else if (x > xmax) {
            refX -= numCellsX;
    }
    if (y < ymin) {
            refY += numCellsY;
    } else if (y > ymax) {
            refY -= numCellsY;
    }
    
    cells[(C_SIZE * ((x * numCellsY) + y)) + Cjx] += cells[(C_SIZE * ((refX * numCellsY) + refY)) + Cjx];
    cells[(C_SIZE * ((refX * numCellsY) + refY)) + Cjx] = cells[(C_SIZE * ((x * numCellsY) + y)) + Cjx];
    
    cells[(C_SIZE * ((x * numCellsY) + y)) + Cjy] += cells[(C_SIZE * ((refX * numCellsY) + refY)) + Cjy];
    cells[(C_SIZE * ((refX * numCellsY) + refY)) + Cjy] = cells[(C_SIZE * ((x * numCellsY) + y)) + Cjy];
    
}


void fourBoundaryMove(int lx, int ly, double x, double y, double deltaX, 
                      double deltaY, double pCharge, double tstep, __global double *cells, int numCellsY) {

    cells[(C_SIZE * (((lx + 2) * numCellsY) + ly - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - y);
    cells[(C_SIZE * (((lx + 2) * numCellsY) + ly + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - y);

    cells[(C_SIZE * (((lx - 1 + 2) * numCellsY) + ly + 2)) + Cjy] += pCharge * deltaX * ((1 - deltaY) / 2 - y);
    cells[(C_SIZE * (((lx + 2) * numCellsY) + ly + 2)) + Cjy] += pCharge * deltaX * ((1 - deltaY) / 2 - y);
    
}

/*-----------------------------------------------------------------------------/
/------------------- Step 1: particlePush()------------------------------------/
/-----------------------------------------------------------------------------*/
//##################################################################################################################/
__kernel void particle_push( __global double* particles,
                             __global const double* force, 
                             __global double* cells,   
                                  double timeStep,
                                  int n,
                                  double width,
                                  double height,
                                  int numCellsX,//total!!!!!!!!!!!!!!!
                                  int numCellsY,
                                  double cellWidth,
                                  double cellHeight,
                                  int iterations) 
{
   int i = get_global_id(0);
   if(i >= n)
        return;
   i = i * P_SIZE;
//   int it;

    //a) particle.storePosition() 
         particles[i + PrevX] = particles[i + X];
         particles[i + PrevY] = particles[i + Y];

    //b)solver.step(particle, force, timeStep)/----Boris solver-----/
         int j = (int)(i/P_SIZE);

         double getPositionComponentofForceX        = force[j + PositionComponentofForceX];
         double getPositionComponentofForceY        = force[j + PositionComponentofForceY];
         double getBz                               = force[j + Bz];
         double getTangentVelocityComponentOfForceX = force[j + TangentVelocityComponentOfForceX];
         double getTangentVelocityComponentOfForceY = force[j + TangentVelocityComponentOfForceY];
         double getMass                             = particles[i + Mass];

         particles[i + PrevPositionComponentForceX]          = getPositionComponentofForceX;
         particles[i + PrevPositionComponentForceY]          = getPositionComponentofForceY;
         particles[i + PrevBz]                               = getBz;
         particles[i + PrevTangentVelocityComponentOfForceX] = getTangentVelocityComponentOfForceX;
         particles[i + PrevTangentVelocityComponentOfForceY] = getTangentVelocityComponentOfForceY;

         double vxminus = particles[i + Vx] + getPositionComponentofForceX * timeStep / (2.0 * getMass);
         double vyminus = particles[i + Vy] + getPositionComponentofForceY * timeStep / (2.0 * getMass);

         double t_z = particles[i + Charge] * getBz * timeStep / (2.0 * getMass);
         double s_z = 2 * t_z / (1 + t_z * t_z);

         double vxprime = vxminus + vyminus * t_z;
         double vyprime = vyminus - vxminus * t_z;

         double vxplus = vxminus + vyprime * s_z;
         double vyplus = vyminus - vxprime * s_z;

         particles[i + Vx] = vxplus + getPositionComponentofForceX * timeStep / (2.0 * getMass) + getTangentVelocityComponentOfForceX * timeStep / getMass;
         particles[i + Vy] = vyplus + getPositionComponentofForceY * timeStep / (2.0 * getMass) + getTangentVelocityComponentOfForceY * timeStep / getMass;

         particles[i + X] = particles[i + X] + particles[i + Vx] * timeStep;
         particles[i + Y] = particles[i + Y] + particles[i + Vy] * timeStep;

    //c) boundaries.applyOnParticleCenter(solver, force, particle, timeStep)
         double regionBoundaryMapX[9];
         double regionBoundaryMapY[9];

         regionBoundaryMapX[X_MIN + Y_MIN] = -width;
         regionBoundaryMapY[X_MIN + Y_MIN] = -height;

         regionBoundaryMapX[X_CENTER + Y_MIN] = 0;
         regionBoundaryMapY[X_CENTER + Y_MIN] = -height;

         regionBoundaryMapX[X_MAX + Y_MIN] = width;
         regionBoundaryMapY[X_MAX + Y_MIN] = -height;

         regionBoundaryMapX[X_MIN + Y_CENTER] = -width;
         regionBoundaryMapY[X_MIN + Y_CENTER] = 0;

         regionBoundaryMapX[X_CENTER + Y_CENTER] = 0;
         regionBoundaryMapY[X_CENTER + Y_CENTER] = 0;

         regionBoundaryMapX[X_MAX + Y_CENTER] = width;
         regionBoundaryMapY[X_MAX + Y_CENTER] = 0;

         regionBoundaryMapX[X_MIN + Y_MAX] = -width;
         regionBoundaryMapY[X_MIN + Y_MAX] = height;

         regionBoundaryMapX[X_CENTER + Y_MAX] = 0;
         regionBoundaryMapY[X_CENTER + Y_MAX] = height;

         regionBoundaryMapX[X_MAX + Y_MAX] = width;
         regionBoundaryMapY[X_MAX + Y_MAX] = height;

         int regi; 
         regi = get_region(particles[i + X], particles[i + X], particles[i + Y], particles[i + Y], width, height);

         particles[i + X]     = particles[i + X] - regionBoundaryMapX[regi];
         particles[i + PrevX] = particles[i + PrevX] - regionBoundaryMapX[regi];

         particles[i + Y]     = particles[i + Y] - regionBoundaryMapY[regi];
         particles[i + PrevY] = particles[i + PrevY] - regionBoundaryMapY[regi];



    /*--------------------------------------------------------------------------/
    /---------------- Step 2: detector.run()------------------------------------/
    /--------------------------------------------------------------------------*/
    /**Turned off
         int pairsSize = (int)(n/P_SIZE);
         double Pair_a[pairsSize];     //the indexes of the overlapped particles
         double Pair_b[pairsSize];
         int h, k, p = 0;

         for(h = 0; h < n; h += P_SIZE){
             for(k = 0; k < n; k += P_SIZE){
                 if(abs(particles[h + X] - particles[k + X]) <= (particles[h + Radius] + particles[k + radius])){
                     if(abs(particles[h + Y] - particles[k + Y]) <= (particles[h + Radius] + particles[k + radius])){
                         Pair_a[p]   = h;
                         Pair_b[p++] = k;
                     }
                 }
             }
         }
    */
    /*--------------------------------------------------------------------------/
    /--- Step 3: collisionalgorithm.collide(detector.getOverlappedPairs().....)-/
    /--------------------------------------------------------------------------*/
    /**Turned off-> will get back to this!!!
         int p1, p2;
         double distanceSquare;
         for(k = 0; k < p; k++){
             p1 = Pair_a[k];
             p2 = Pair_b[k];

             distanceSquare = ((particles[p1 + X] - particles[p2 + X]) * (particles[p1 + X] - particles[p2 + X]) +
                               (particles[p1 + Y] - particles[p2 + Y]) * (particles[p1 + Y] - particles[p2 + Y]));

             if(distanceSquare <= ((particles[p1 + Radius] + particles[p2 + Radius]) * (particles[p1 + Radius] + particles[p2 + Radius]))){
                 s.complete(p1, f, step);
                 s.complete(p2, f, step);
                 doCollision(p1, p2);
                 s.prepare(p1, f, step);
                 s.prepare(p2, f, step);
             }
         }
     */

   
}

 /*-----------------------------------------------------------------------------/
 /------Step 4: interpolation.interpolateToGrid(particles, grid, tstep)---------/
 /-----------------------------------------------------------------------------*/
//##################################################################################################################/
__kernel void reset_current( __global double* particles,
                             __global const double* force, 
                             __global double* cells,   
                                  double timeStep,
                                  int n,
                                  double width,
                                  double height,
                                  int numCellsX,//total!!!!!!!!!!!!!!!
                                  int numCellsY,
                                  double cellWidth,
                                  double cellHeight,
                                  int iterations) 
{
        int i = get_global_id(0);
        if(i > 0)
             return;
    //    i = i * P_SIZE;

        int j;
        for(j = 0; j < numCellsX * numCellsY; j += C_SIZE){
            cells[j + Cjx] = 0;
            cells[j + Cjy] = 0;
        }
}



//##################################################################################################################/
__kernel void grid_interpolation(__global double* particles,
                                 __global const double* force, 
                                 __global double* cells,
                                  double timeStep,
                                  int n,
                                  double width,
                                  double height,
                                  int numCellsX,//not total
                                  int numCellsY,
                                  double cellWidth,
                                  double cellHeight,
                                  int iterations) 
{
         int i = get_global_id(0);
         if(i >= n)
             return;
         i = i * P_SIZE;
        
         /**X index of local origin i.e. nearest grid point BEFORE particle push*/
         int xStart;
         /**Y index of local origin i.e. nearest grid point BEFORE particle push*/
         int yStart;
         /**X index of local origin i.e. nearest grid point AFTER particle push*/
         int xEnd;
         /**Y index of local origin i.e. nearest grid point AFTER particle push*/
         int yEnd;
         /**Normalized local x coordinate BEFORE particle push*/
         double x;
         /**Normalized local y coordinate BEFORE particle push*/
         double y;
         /**Normalized distance covered in X direction*/
         double deltaX;
         /**Normalized distance covered in X direction*/
         double deltaY;

         x = particles[i + PrevX]/cellWidth;
         y = particles[i + PrevY]/cellHeight;

         xStart = (int) floor(x + 0.5);
         yStart = (int) floor(y + 0.5);

         deltaX = particles[i + X]/cellWidth;
         deltaY = particles[i + Y]/cellHeight;

         xEnd = (int) floor(deltaX + 0.5);
         yEnd = (int) floor(deltaY + 0.5);

         deltaX -= x;
         deltaY -= y;

         x -= xStart;
         y -= yStart;

         double pCharge = particles[i + Charge];
         double p = pCharge;
         double tstep = timeStep;
        
//         cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += 10;

         //4-boundary move?
         if (xStart == xEnd && yStart == yEnd) {
                    fourBoundaryMove(xStart, yStart, x, y, deltaX, deltaY, pCharge, timeStep, cells, numCellsX);
                 }
         //7-boundary move?
         else if (xStart == xEnd || yStart == yEnd) {                       
                        //7-boundary move with equal y?
                        if (yStart == yEnd) {
                                //particle moves right?
                                if (xEnd > xStart) {

                                        double deltaX1 = 0.5 - x;
                                        double deltaY1 = (deltaY / deltaX) * deltaX1;
                                        fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY);

                                        deltaX -= deltaX1;
                                        deltaY -= deltaY1;
                                        y += deltaY1;
                                        fourBoundaryMove(xEnd, yEnd, - 0.5, y, deltaX, deltaY, p, tstep, cells, numCellsY);

                                }
                                //particle moves left
                                else {

                                        double deltaX1 = -(0.5 + x);
                                        double deltaY1 = (deltaY / deltaX) * deltaX1;
                                        fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY);

                                        deltaX -= deltaX1;
                                        deltaY -= deltaY1;
                                        y += deltaY1;
                                        fourBoundaryMove(xEnd, yEnd, 0.5, y, deltaX, deltaY, p, tstep, cells, numCellsY);

                                }
                        }
                        //7-boundary move with equal x?
                        if (xStart == xEnd) {
                                //particle moves up?
                                if (yEnd > yStart) {

                                        double deltaY1 = 0.5 - y;
                                        double deltaX1 = deltaX  * (deltaY1 / deltaY);
                                        fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY);

                                        deltaX -= deltaX1;
                                        deltaY -= deltaY1;
                                        y += deltaY1;
                                        fourBoundaryMove(xEnd, yEnd, x, -0.5, deltaX, deltaY, p, tstep, cells, numCellsY);

                                }
                                //particle moves down
                                else {

                                        double deltaY1 = -(0.5 + y);
                                        double deltaX1 = (deltaX / deltaY) * deltaY1;
                                        fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY);

                                        deltaX -= deltaX1;
                                        deltaY -= deltaY1;
                                        y += deltaY1;
                                        fourBoundaryMove(xEnd, yEnd, x, 0.5, deltaX, deltaY, p, tstep, cells, numCellsY);

                                }
                        }
                 }
                 // 10-boundary move
                else {                              
                          //moved right?
                          if (xEnd == (xStart+1)) {
                                  //moved up?
                                  if (yEnd == (yStart+1)) {

                                          double deltaX1 = 0.5 - x;

                                          //lower local origin
                                          if(((deltaY / deltaX) * deltaX1 + y) < 0.5) {

                                                  double deltaY1 = (deltaY / deltaX) * deltaX1;
                                                  fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY);

                                                  double deltaY2 = 0.5 - y - deltaY1;
                                                  double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
                                                  y += deltaY1;
                                                  fourBoundaryMove(xStart+1, yStart, -0.5, y, deltaX2, deltaY2, p, tstep, cells, numCellsY);

                                                  deltaX -= (deltaX1 + deltaX2);
                                                  deltaY -= (deltaY1 + deltaY2);
                                                  x = deltaX2 - 0.5;
                                                  fourBoundaryMove(xEnd, yEnd, x, -0.5, deltaX, deltaY, p, tstep, cells, numCellsY);

                                          }
                                          //upper local origin
                                          else {

                                                  double deltaY1 = 0.5 - y;
                                                  deltaX1 = (deltaX / deltaY) * deltaY1;
                                                  fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY);

                                                  double deltaX2 = 0.5 - x - deltaX1;
                                                  double deltaY2 = (deltaY1 / deltaX1) * deltaX2;
                                                  x += deltaX1;
                                                  fourBoundaryMove(xStart, yStart+1, x, -0.5, deltaX2, deltaY2, p, tstep, cells, numCellsY);

                                                  deltaX -= (deltaX1 + deltaX2);
                                                  deltaY -= (deltaY1 + deltaY2);
                                                  y = deltaY2 - 0.5;
                                                  fourBoundaryMove(xEnd, yEnd, -0.5, y, deltaX, deltaY, p, tstep, cells, numCellsY);

                                          }
                                  }
                                  //moved down
                                  else {

                                          double deltaY1 = -(0.5 + y);

                                          //lower local origin
                                          if(((deltaX / deltaY) * deltaY1 + x) < 0.5) {

                                                  double deltaX1 = (deltaX / deltaY) * deltaY1;
                                                  fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY);

                                                  double deltaX2 = 0.5 - x - deltaX1;
                                                  double deltaY2 = (deltaY / deltaX) * deltaX2;
                                                  x += deltaX1;
                                                  fourBoundaryMove(xStart, yStart-1, x, 0.5, deltaX2, deltaY2, p, tstep, cells, numCellsY);

                                                  deltaX -= (deltaX1 + deltaX2);
                                                  deltaY -= (deltaY1 + deltaY2);
                                                  y = 0.5 + deltaY2;
                                                  fourBoundaryMove(xEnd, yEnd, -0.5, y, deltaX, deltaY, p, tstep, cells, numCellsY);

                                          }
                                          //upper local origin
                                          else {

                                                  double deltaX1 = 0.5 - x;
                                                  deltaY1 = (deltaY / deltaX) * deltaX1;
                                                  fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY);

                                                  double deltaY2 = -(0.5 + y + deltaY1);
                                                  double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
                                                  y += deltaY1;
                                                  fourBoundaryMove(xStart+1, yStart, -0.5, y, deltaX2, deltaY2, p, tstep, cells, numCellsY);

                                                  deltaX -= (deltaX1 + deltaX2);
                                                  deltaY -= (deltaY1 + deltaY2);
                                                  x = deltaX2 - 0.5;
                                                  fourBoundaryMove(xEnd, yEnd, x, 0.5, deltaX, deltaY, p, tstep, cells, numCellsY);

                                          }
                                  }

                          }
                          //moved left
                          else {
                                  //moved up?
                                  if (yEnd == (yStart+1)) {

                                          double deltaX1 = -(0.5 + x);
                                          //lower local origin
                                          if(((deltaY / deltaX) * deltaX1 + y) < 0.5) {

                                                  double deltaY1 = (deltaY / deltaX) * deltaX1;
                                                  fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY);

                                                  double deltaY2 = 0.5 - y - deltaY1;
                                                  double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
                                                  y += deltaY1;
                                                  fourBoundaryMove(xStart-1, yStart, 0.5, y, deltaX2, deltaY2, p, tstep, cells, numCellsY);

                                                  deltaX -= (deltaX1 + deltaX2);
                                                  deltaY -= (deltaY1 + deltaY2);
                                                  x = 0.5 + deltaX2;
                                                  fourBoundaryMove(xEnd, yEnd, x, -0.5, deltaX, deltaY, p, tstep, cells, numCellsY);

                                          }
                                          //upper local origin
                                          else {

                                                  double deltaY1 = 0.5 - y;
                                                  deltaX1 = (deltaX / deltaY) * deltaY1;
                                                  fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY);

                                                  double deltaX2 = -(0.5 + x + deltaX1);
                                                  double deltaY2 = (deltaY1 / deltaX1) * deltaX2;
                                                  x += deltaX1;
                                                  fourBoundaryMove(xStart, yStart+1, x, -0.5, deltaX2, deltaY2, p, tstep, cells, numCellsY);

                                                  deltaX -= (deltaX1 + deltaX2);
                                                  deltaY -= (deltaY1 + deltaY2);
                                                  y = deltaY2 - 0.5;
                                                  fourBoundaryMove(xEnd, yEnd, 0.5, y, deltaX, deltaY,p, tstep, cells, numCellsY);

                                          }
                                  }
                                  //moved down
                                  else {

                                          double deltaY1 = -(0.5 + y);
                                          //lower local origin
                                          if((-(deltaX / deltaY) * deltaY1 - x) < 0.5) {

                                                  double deltaX1 = (deltaX / deltaY) * deltaY1;
                                                  fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1,p, tstep, cells, numCellsY);

                                                  double deltaX2 = -(0.5 + x + deltaX1);
                                                  double deltaY2 = (deltaY / deltaX) * deltaX2;
                                                  x += deltaX1;
                                                  fourBoundaryMove(xStart, yStart-1, x, 0.5, deltaX2, deltaY2, p, tstep, cells, numCellsY);

                                                  deltaX -= (deltaX1 + deltaX2);
                                                  deltaY -= (deltaY1 + deltaY2);
                                                  y = 0.5 + deltaY2;
                                                  fourBoundaryMove(xEnd, yEnd, 0.5, y, deltaX, deltaY, p, tstep, cells, numCellsY);

                                          }
                                          //upper local origin
                                          else {

                                                  double deltaX1 = -(0.5 + x);
                                                  deltaY1 = (deltaY / deltaX) * deltaX1;
                                                  fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY);

                                                  double deltaY2 = -(0.5 + y + deltaY1);
                                                  double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
                                                  y += deltaY1;
                                                  fourBoundaryMove(xStart+1, yStart, 0.5, y, deltaX2, deltaY2, p, tstep, cells, numCellsY);

                                                  deltaX -= (deltaX1 + deltaX2);
                                                  deltaY -= (deltaY1 + deltaY2);
                                                  x = 0.5 + deltaX2;
                                                  fourBoundaryMove(xEnd, yEnd, x, 0.5, deltaX, deltaY, p, tstep, cells, numCellsY);

                                          }
                                  }
                          }                               
                }

//         createBoundaryCells(numCellsX, numCellsY, cells);

}


//##################################################################################################################/
__kernel void create_boundary_cells( __global double* particles,
                                     __global const double* force, 
                                     __global double* cells,   
                                        double timeStep,
                                        int n,
                                        double width,
                                        double height,
                                        int numCellsX,//not total
                                        int numCellsY,
                                        double cellWidth,
                                        double cellHeight,
                                        int iterations) 
{
        int i = get_global_id(0);
        if(i > 0)
             return;
        i = i * P_SIZE;

        int x, y;
        int numCellsXTotal = numCellsX + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;
        int numCellsYTotal = numCellsY + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;

        for (x = 0; x < EXTRA_CELLS_BEFORE_GRID; x++) {
                for (y = 0; y < numCellsXTotal; y++) {
                        createBoundaryCell(x, y, numCellsX, numCellsY, cells);
                }
        }
        // right boundary (with corner cells)
        for (x = EXTRA_CELLS_BEFORE_GRID + numCellsX; x < numCellsXTotal; x++) {
                for (y = 0; y < numCellsYTotal; y++) {
                        createBoundaryCell(x, y, numCellsX, numCellsY, cells);
                }
        }
        // top boundary (without corner cells)
        for (x = EXTRA_CELLS_BEFORE_GRID; x < EXTRA_CELLS_BEFORE_GRID + numCellsX; x++) {
                for (y = 0; y < EXTRA_CELLS_BEFORE_GRID; y++) {
                        createBoundaryCell(x, y, numCellsX, numCellsY, cells);
                }
        }
        // bottom boundary (without corner cells)
        for (x = EXTRA_CELLS_BEFORE_GRID; x < EXTRA_CELLS_BEFORE_GRID + numCellsX; x++) {
                for (y = EXTRA_CELLS_BEFORE_GRID + numCellsY; y < numCellsYTotal; y++) {
                        createBoundaryCell(x, y, numCellsX, numCellsY, cells);
                }
        }
}


/*-----------------------------------------------------------------------------/
/----------------Step 5: grid.updateGrid(tstep)--------------------------------/
/-----------------------------------------------------------------------------*/
//##################################################################################################################/
__kernel void store_fields( __global double* particles,
                            __global const double* force, 
                            __global double* cells,   
                               double timeStep,
                               int n,
                               double width,
                               double height,
                               int numCellsX,//not total
                               int numCellsY,
                               double cellWidth,
                               double cellHeight,
                               int iterations) 
{
        
         int i = get_global_id(0);
         if(i > 0)
              return;
//         i = i * P_SIZE;
        
         int numCellsXTotal = numCellsX + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;
         int numCellsYTotal = numCellsY + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;

         int  k;
         for(k = 0; k < numCellsXTotal * numCellsYTotal; k+=C_SIZE){
             cells[k + Cbzo] = cells[k + Cbz];
         }
}

//##################################################################################################################/
__kernel void solve_for_e(  __global double* particles,
                            __global const double* force, 
                            __global double* cells,   
                               double timeStep,
                               int n,
                               double width,
                               double height,
                               int numCellsX,//not total
                               int numCellsY,
                               double cellWidth,
                               double cellHeight,
                               int iterations) 
{
        
         int i = get_global_id(0);
         if(i > 0)
              return;
//         i = i * P_SIZE;
        
         int numCellsXTotal = numCellsX + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;
         int numCellsYTotal = numCellsY + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;

         int h, k;
         double cx, cy;

         for(h = 0; h < numCellsXTotal; h++){
             for(k = 0; k < numCellsYTotal; k++){
                 cx = (cells[(C_SIZE * (((h + 2) * numCellsY) + k + 1 + 2)) + Cbz] - 
                       cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cbz])/cellHeight;
                 cy = -(cells[(C_SIZE * (((h + 1 + 2) * numCellsY) + k + 2)) + Cbz] - 
                        cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cbz])/cellWidth;

                 cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cex] += timeStep * (cx - cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cjx]);
                 cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cey] += timeStep * (cy - cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cjy]);
             }
         }
        
}
    
//##################################################################################################################/
__kernel void solve_for_b(  __global double* particles,
                            __global const double* force, 
                            __global double* cells,   
                               double timeStep,
                               int n,
                               double width,
                               double height,
                               int numCellsX,//not total
                               int numCellsY,
                               double cellWidth,
                               double cellHeight,
                               int iterations) 
{
        
         int i = get_global_id(0);
         if(i > 0)
              return;
//         i = i * P_SIZE;
        
         int numCellsXTotal = numCellsX + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;
         int numCellsYTotal = numCellsY + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;

         int h, k;
         double cz;
        
         for(h = 0; h < numCellsXTotal; h++){
             for(k = 0; k < numCellsYTotal; k++){
                cz = (cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cey] - 
                      cells[(C_SIZE * (((h - 1 + 2) * numCellsY) + k + 2)) + Cey])/cellWidth -
                     (cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cex] - 
                      cells[(C_SIZE * (((h - 1 + 2) * numCellsY) + k + 2)) + Cex])/cellHeight;

                cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cbz] += (-timeStep * cz);
             }
         }
}
        

/*-----------------------------------------------------------------------------/
/--------Step 6: interpolation.interpolateToParticle(particles, grid)----------/
/-----------------------------------------------------------------------------*/
//##################################################################################################################/
__kernel void particle_interpolation( __global double* particles,
                             __global const double* force, 
                             __global double* cells,   
                                  double timeStep,
                                  int n,
                                  double width,
                                  double height,
                                  int numCellsX,//total!!!!!!!!!!!!!!!
                                  int numCellsY,
                                  double cellWidth,
                                  double cellHeight,
                                  int iterations) 
{
         int i = get_global_id(0);
         if(i >= n)
             return;
         i = i * P_SIZE;

         /**X index of the grid point that is left from or at the x position of the particle*/
         int ii;
         /**Y index of the grid point that is below or at the y position of the particle*/
         int jj;		
         /**Normalized distance to the left cell boundary*/
         double a;
         /**Normalized distance to the right cell boundary*/
         double b;
         /**Normalized distance to the lower cell boundary*/
         double c;
         /**Normalized distance to the upper cell boundary*/
         double d;

         a  = particles[i + X] / cellWidth;
         ii = (int)floor(a);
         a  = -ii;
         b  = 1 - a;

         c  = particles[i + Y] / cellHeight;
         jj = (int)floor(c);
         c  = -jj;
         d  = 1 - c;

         //Bz as given by the FDTD field solver is defined half a timestep ahead of particle
         //time. Therefore we have to average over the old Bz (that is half a timestep behind)
         //and the current Bz. The magnetic field is located at the grid points. 
         //No adjustments to the grid are necessary.
         particles[i + PBz] = ((formFactor(
                                          cells[(C_SIZE * (((ii + 2) * numCellsY) + jj + 2)) + Cbzo],
                                          cells[(C_SIZE * (((ii + 2) * numCellsY) + jj + 1 + 2)) + Cbzo],
                                          cells[(C_SIZE * (((ii + 1 + 2) * numCellsY) + jj + 1 + 2)) + Cbzo],
                                          cells[(C_SIZE * (((ii + 1 + 2) * numCellsY) + jj + 2)) + Cbzo],
                                          a, b, c, d)
                                         +
                               formFactor(
                                          cells[(C_SIZE * (((ii + 2) * numCellsY) + jj + 2)) + Cbz],
                                          cells[(C_SIZE * (((ii + 2) * numCellsY) + jj + 1 + 2)) + Cbz],
                                          cells[(C_SIZE * (((ii + 1 + 2) * numCellsY) + jj + 1 + 2)) + Cbz],
                                          cells[(C_SIZE * (((ii + 1 + 2) * numCellsY) + jj + 2)) + Cbz],
                                          a, b, c, d))/2);

         //The Ex-field is located in the middle of the left cell boundary.
         //This means that the Ex-field-grid is shifted upwards by half a cell height.
         //The adjustments are made to calculate the distance to the shifted grid. The
         //only changes to be made are in the vertical plane. All changes are reversed
         //after the calculation.
         if( c < 0.5 ){
                 jj -= 1;
                 c  += 0.5;
                 d  -= 0.5;

                 particles[i + Ex] = formFactor(
                                                cells[(C_SIZE * (((ii + 2) * numCellsY) + jj + 2)) + Cex],
                                                cells[(C_SIZE * (((ii + 2) * numCellsY) + jj + 1 + 2)) + Cex],
                                                cells[(C_SIZE * (((ii + 1 + 2) * numCellsY) + jj + 1 + 2)) + Cex],
                                                cells[(C_SIZE * (((ii + 1 + 2) * numCellsY) + jj + 2)) + Cex],
                                                a, b, c, d);
                 c -= 0.5;
                 d += 0.5;
                 jj += 1;
         } else {
                 c -= 0.5;
                 d += 0.5;

                 particles[i + Ex] = formFactor(
                                                cells[(C_SIZE * (((ii + 2) * numCellsY) + jj + 2)) + Cex],
                                                cells[(C_SIZE * (((ii + 2) * numCellsY) + jj + 1 + 2)) + Cex],
                                                cells[(C_SIZE * (((ii + 1 + 2) * numCellsY) + jj + 1 + 2)) + Cex],
                                                cells[(C_SIZE * (((ii + 1 + 2) * numCellsY) + jj + 2)) + Cex],
                                                a, b, c, d);
                 c += 0.5;
                 d -= 0.5;
         }
         //The Ey-field is located in the middle of the lower cell boundary.
         //This means that the Ey-field-grid is shifted to the right by half a cell width.
         //The adjustments are made to calculate the distance to the shifted grid. The
         //only changes to be made are in the horizontal plane.
         if( a < 0.5 ){
                 ii -= 1;
                 a  += 0.5;
                 b  -= 0.5;

                 particles[i + Ey] = formFactor(
                                                cells[(C_SIZE * (((ii + 2) * numCellsY) + jj + 2)) + Cey],
                                                cells[(C_SIZE * (((ii + 2) * numCellsY) + jj + 1 + 2)) + Cey],
                                                cells[(C_SIZE * (((ii + 1 + 2) * numCellsY) + jj + 1 + 2)) + Cey],
                                                cells[(C_SIZE * (((ii + 1 + 2) * numCellsY) + jj + 2)) + Cey],
                                                a, b, c, d);               
         } else {
                 a -= 0.5;
                 b += 0.5;

                particles[i + Ey] = formFactor(
                                                cells[(C_SIZE * (((ii + 2) * numCellsY) + jj + 2)) + Cey],
                                                cells[(C_SIZE * (((ii + 2) * numCellsY) + jj + 1 + 2)) + Cey],
                                                cells[(C_SIZE * (((ii + 1 + 2) * numCellsY) + jj + 1 + 2)) + Cey],
                                                cells[(C_SIZE * (((ii + 1 + 2) * numCellsY) + jj + 2)) + Cey],
                                                a, b, c, d);               
         }	
   }     
