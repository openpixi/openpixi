//#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics : enable
//#pragma OPENCL EXTENSION cl_khr_fp64 : enable
//#if defined(cl_amd_fp64) || defined(cl_khr_fp64)
//
// 
//
//#if defined(cl_amd_fp64)
//
//#pragma OPENCL EXTENSION cl_amd_fp64 : enable
//
//#elif defined(cl_khr_fp64)
//
//#pragma OPENCL EXTENSION cl_khr_fp64 : enable
//
//#endif


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
#define boundSize EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID

//#define LOCK(a) atom_cmpxchg(a, 0, 1)
//#define UNLOCK(a) atom_xchg(a, 0)

//void GetSemaphor(__global int* semaphor) {
//   int occupied = atom_xchg(semaphor, 1);
//   while(occupied > 0)
//   {
//     occupied = atom_xchg(semaphor, 1);
//   }
//}
//
//void ReleaseSemaphor(__global int* semaphor)
//{
//   int prevVal = atom_xchg(semaphor, 0);
//}
//double atomicDoubleAdd(__global double* address, double val)
//{
//    ulong * address_as_ull =(ulong *)address;
//    ulong old = *address_as_ull, assumed;
//    do {
//        assumed = old;
//        old = atom_cmpxchg(address_as_ull, assumed,as_ulong(val +as_double(assumed)));
//    } while (assumed != old);
//
//    return as_double(old);
//}

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
//		return f;
}


void createBoundaryCell(int x, int y, int numCellsX, int numCellsY, __global double *cells, int boundaryCells[14][14], int mark) {
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
    
    if(boundaryCells[x][y] != 0)
        boundaryCells[refX][refY] = boundaryCells[x][y];
    else if(boundaryCells[refX][refY] != 0)
        boundaryCells[x][y] = boundaryCells[refX][refY];
    else{
        boundaryCells[x][y] = mark;
        boundaryCells[refX][refY] = mark;
    }
    
}

int isBoundaryCell(int x, int y, int numCellsXTotal, int numCellsYTotal){
     int numCellsX = numCellsXTotal - EXTRA_CELLS_BEFORE_GRID - EXTRA_CELLS_AFTER_GRID;
     int numCellsY = numCellsYTotal - EXTRA_CELLS_BEFORE_GRID - EXTRA_CELLS_AFTER_GRID;


     if((x < EXTRA_CELLS_BEFORE_GRID) ||
        (x >= EXTRA_CELLS_BEFORE_GRID + numCellsX) ||
        (y < EXTRA_CELLS_BEFORE_GRID && x >= EXTRA_CELLS_BEFORE_GRID && x < EXTRA_CELLS_BEFORE_GRID + numCellsX) ||
        (y >= EXTRA_CELLS_BEFORE_GRID + numCellsY && x >= EXTRA_CELLS_BEFORE_GRID && x < EXTRA_CELLS_BEFORE_GRID + numCellsX)){
     
        return 1;
     }            
    
     return 0;
}
    

void fourBoundaryMove(int lx, int ly, double x, double y, double deltaX, double deltaY, double pCharge, 
                      double tstep, __global double *cells, int numCellsY, __global int* semaphor, __global int* boundaries) {
    int mark;
    int i, j;
//    GetSemaphor(&semaphor[0]);              
    mark = boundaries[((lx + 2) * numCellsY) + ly - 1 + 2];
        for(i = 0; i < numCellsY*numCellsY; i++){
                if(boundaries[i] == mark){
                    cells[(C_SIZE * i) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - y);
            }
        }
//    ReleaseSemaphor(&semaphor[0]);
    
//    GetSemaphor(&semaphor[0]);
    mark = boundaries[((lx + 2) * numCellsY) + ly + 2];
         for(i = 0; i < numCellsY*numCellsY; i++){
                if(boundaries[i] == mark){
                    cells[(C_SIZE * i) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + y);
                }
        }
//    ReleaseSemaphor(&semaphor[0]);    
    
//    GetSemaphor(&semaphor[0]);
    mark = boundaries[((lx - 1 + 2) * numCellsY) + ly + 2];
         for(i = 0; i < numCellsY*numCellsY; i++){
                if(boundaries[i] == mark){
                    cells[(C_SIZE * i) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - x);
                }
        }
//    ReleaseSemaphor(&semaphor[0]);
    
    
//    GetSemaphor(&semaphor[0]);
    mark = boundaries[((lx + 2) * numCellsY) + ly + 2];
         for(i = 0; i < numCellsY*numCellsY; i++){
                if(boundaries[i] == mark){
                    cells[(C_SIZE * i) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + x);
                }
        }
//    ReleaseSemaphor(&semaphor[0]);

}

void sevenBoundaryMove(double x, double y, int xStart, int yStart, int xEnd, int yEnd,double deltaX,
                       double deltaY, double p, double tstep, __global double *cells, int numCellsY, __global int* semaphor, __global int* boundaries) {
    //7-boundary move with equal y?
    if (yStart == yEnd) {
            //particle moves right?
            if (xEnd > xStart) {

                    double deltaX1 = 0.5 - x;
                    double deltaY1 = (deltaY / deltaX) * deltaX1;
                    fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY, semaphor, boundaries);                                   

//                                        cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX1 * ((1 - deltaY1) / 2 - y);
//                                        cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX1 * ((1 + deltaY1) / 2 + y);
//                                        cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 - deltaX1) / 2 - x);
//                                        cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 + deltaX1) / 2 + x);

                    deltaX -= deltaX1;
                    deltaY -= deltaY1;
                    y += deltaY1;
                    fourBoundaryMove(xEnd, yEnd, - 0.5, y, deltaX, deltaY, p, tstep, cells, numCellsY, semaphor, boundaries);
//                                       
//                                        cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - y);
//                                        cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + y);
//                                        cells[(C_SIZE * (((xEnd - 1 + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - (-0.5));
//                                        cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + (-0.5));

            }
            //particle moves left
            else {

                    double deltaX1 = -(0.5 + x);
                    double deltaY1 = (deltaY / deltaX) * deltaX1;
                    fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                        cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX1 * ((1 - deltaY1) / 2 - y);
//                                        cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX1 * ((1 + deltaY1) / 2 + y);
//                                        cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 - deltaX1) / 2 - x);
//                                        cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 + deltaX1) / 2 + x);

                    deltaX -= deltaX1;
                    deltaY -= deltaY1;
                    y += deltaY1;
                    fourBoundaryMove(xEnd, yEnd, 0.5, y, deltaX, deltaY, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                        cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - y);
//                                        cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + y);
//                                        cells[(C_SIZE * (((xEnd - 1 + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - 0.5);
//                                        cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + 0.5);
            }
    }
    //7-boundary move with equal x?
    if (xStart == xEnd) {
            //particle moves up?
            if (yEnd > yStart) {

                    double deltaY1 = 0.5 - y;
                    double deltaX1 = deltaX  * (deltaY1 / deltaY);
                    fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY, semaphor, boundaries);
//                                        cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX1 * ((1 - deltaY1) / 2 - y);
//                                        cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX1 * ((1 + deltaY1) / 2 + y);
//                                        cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 - deltaX1) / 2 - x);
//                                        cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 + deltaX1) / 2 + x);

                    deltaX -= deltaX1;
                    deltaY -= deltaY1;
                    y += deltaY1;
                    fourBoundaryMove(xEnd, yEnd, x, -0.5, deltaX, deltaY, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                        cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - (-0.5));
//                                        cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + (-0.5));
//                                        cells[(C_SIZE * (((xEnd - 1 + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - x);
//                                        cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + x);
            }
            //particle moves down
            else {

                    double deltaY1 = -(0.5 + y);
                    double deltaX1 = (deltaX / deltaY) * deltaY1;
                    fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                        cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX1 * ((1 - deltaY1) / 2 - y);
//                                        cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX1 * ((1 + deltaY1) / 2 + y);
//                                        cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 - deltaX1) / 2 - x);
//                                        cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 + deltaX1) / 2 + x);

                    deltaX -= deltaX1;
                    deltaY -= deltaY1;
                    y += deltaY1;
                    fourBoundaryMove(xEnd, yEnd, x, 0.5, deltaX, deltaY, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                        cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - 0.5);
//                                        cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + 0.5);
//                                        cells[(C_SIZE * (((xEnd - 1 + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - x);
//                                        cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + x);

            }
    }
}

void tenBoundaryMove(double x, double y, int xStart, int yStart, int xEnd, int yEnd, double deltaX,
                     double deltaY, double p, double tstep, __global double *cells, int numCellsY, __global int* semaphor, __global int* boundaries) {
    //moved right?
    if (xEnd == (xStart+1)) {
            //moved up?
            if (yEnd == (yStart+1)) {

                    double deltaX1 = 0.5 - x;

                    //lower local origin
                    if(((deltaY / deltaX) * deltaX1 + y) < 0.5) {

                            double deltaY1 = (deltaY / deltaX) * deltaX1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX1 * ((1 - deltaY1) / 2 - y);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX1 * ((1 + deltaY1) / 2 + y);
//                                                  cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 - deltaX1) / 2 - x);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 + deltaX1) / 2 + x);

                            double deltaY2 = 0.5 - y - deltaY1;
                            double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
                            y += deltaY1;
                            fourBoundaryMove(xStart+1, yStart, -0.5, y, deltaX2, deltaY2, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * ((((xStart + 1) + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX2 * ((1 - deltaY2) / 2 - y);
//                                                  cells[(C_SIZE * ((((xStart + 1) + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX2 * ((1 + deltaY2) / 2 + y);
//                                                  cells[(C_SIZE * ((((xStart + 1) - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY2 * ((1 - deltaX2) / 2 - (-0.5));
//                                                  cells[(C_SIZE * ((((xStart + 1) + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY2 * ((1 + deltaX2) / 2 + (-0.5));

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            x = deltaX2 - 0.5;
                            fourBoundaryMove(xEnd, yEnd, x, -0.5, deltaX, deltaY, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - (-0.5));
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + (-0.5));
//                                                  cells[(C_SIZE * (((xEnd - 1 + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - x);
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + x);

                    }
                    //upper local origin
                    else {

                            double deltaY1 = 0.5 - y;
                            deltaX1 = (deltaX / deltaY) * deltaY1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX1 * ((1 - deltaY1) / 2 - y);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX1 * ((1 + deltaY1) / 2 + y);
//                                                  cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 - deltaX1) / 2 - x);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 + deltaX1) / 2 + x);

                            double deltaX2 = 0.5 - x - deltaX1;
                            double deltaY2 = (deltaY1 / deltaX1) * deltaX2;
                            x += deltaX1;
                            fourBoundaryMove(xStart, yStart+1, x, -0.5, deltaX2, deltaY2, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + (yStart + 1) - 1 + 2)) + Cjx] += pCharge * deltaX2 * ((1 - deltaY2) / 2 - (-0.5));
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + (yStart + 1) + 2)) + Cjx] += pCharge * deltaX2 * ((1 + deltaY2) / 2 + (-0.5));
//                                                  cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + (yStart + 1) + 2)) + Cjy] += pCharge * deltaY2 * ((1 - deltaX2) / 2 - x);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + (yStart + 1) + 2)) + Cjy] += pCharge * deltaY2 * ((1 + deltaX2) / 2 + x);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            y = deltaY2 - 0.5;
                            fourBoundaryMove(xEnd, yEnd, -0.5, y, deltaX, deltaY, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - y);
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + y);
//                                                  cells[(C_SIZE * (((xEnd - 1 + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - (-0.5));
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + (-0.5));
                    }
            }
            //moved down
            else {

                    double deltaY1 = -(0.5 + y);

                    //lower local origin
                    if(((deltaX / deltaY) * deltaY1 + x) < 0.5) {

                            double deltaX1 = (deltaX / deltaY) * deltaY1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX1 * ((1 - deltaY1) / 2 - y);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX1 * ((1 + deltaY1) / 2 + y);
//                                                  cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 - deltaX1) / 2 - x);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 + deltaX1) / 2 + x);

                            double deltaX2 = 0.5 - x - deltaX1;
                            double deltaY2 = (deltaY / deltaX) * deltaX2;
                            x += deltaX1;
                            fourBoundaryMove(xStart, yStart-1, x, 0.5, deltaX2, deltaY2, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + (yStart - 1) - 1 + 2)) + Cjx] += pCharge * deltaX2 * ((1 - deltaY2) / 2 - 0.5);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + (yStart - 1) + 2)) + Cjx] += pCharge * deltaX2 * ((1 + deltaY2) / 2 + 0.5);
//                                                  cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + (yStart - 1) + 2)) + Cjy] += pCharge * deltaY2 * ((1 - deltaX2) / 2 - x);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + (yStart - 1) + 2)) + Cjy] += pCharge * deltaY2 * ((1 + deltaX2) / 2 + x);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            y = 0.5 + deltaY2;
                            fourBoundaryMove(xEnd, yEnd, -0.5, y, deltaX, deltaY, p, tstep, cells, numCellsY, semaphor, boundaries);
//
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - y);
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + y);
//                                                  cells[(C_SIZE * (((xEnd - 1 + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - (-0.5));
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + (-0.5));

                    }
                    //upper local origin
                    else {

                            double deltaX1 = 0.5 - x;
                            deltaY1 = (deltaY / deltaX) * deltaX1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX1 * ((1 - deltaY1) / 2 - y);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX1 * ((1 + deltaY1) / 2 + y);
//                                                  cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 - deltaX1) / 2 - x);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 + deltaX1) / 2 + x);

                            double deltaY2 = -(0.5 + y + deltaY1);
                            double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
                            y += deltaY1;
                            fourBoundaryMove(xStart+1, yStart, -0.5, y, deltaX2, deltaY2, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * ((((xStart + 1) + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX2 * ((1 - deltaY2) / 2 - y);
//                                                  cells[(C_SIZE * ((((xStart + 1) + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX2 * ((1 + deltaY2) / 2 + y);
//                                                  cells[(C_SIZE * ((((xStart + 1) - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY2 * ((1 - deltaX2) / 2 - (-0.5));
//                                                  cells[(C_SIZE * ((((xStart + 1) + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY2 * ((1 + deltaX2) / 2 + (-0.5));

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            x = deltaX2 - 0.5;
                            fourBoundaryMove(xEnd, yEnd, x, 0.5, deltaX, deltaY, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - 0.5);
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + 0.5);
//                                                  cells[(C_SIZE * (((xEnd - 1 + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - x);
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + x);

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
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX1 * ((1 - deltaY1) / 2 - y);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX1 * ((1 + deltaY1) / 2 + y);
//                                                  cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 - deltaX1) / 2 - x);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 + deltaX1) / 2 + x);

                            double deltaY2 = 0.5 - y - deltaY1;
                            double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
                            y += deltaY1;
                            fourBoundaryMove(xStart-1, yStart, 0.5, y, deltaX2, deltaY2, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * ((((xStart - 1) + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX2 * ((1 - deltaY2) / 2 - y);
//                                                  cells[(C_SIZE * ((((xStart - 1) + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX2 * ((1 + deltaY2) / 2 + y);
//                                                  cells[(C_SIZE * ((((xStart - 1) - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY2 * ((1 - deltaX2) / 2 - 0.5);
//                                                  cells[(C_SIZE * ((((xStart - 1) + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY2 * ((1 + deltaX2) / 2 + 0.5);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            x = 0.5 + deltaX2;
                            fourBoundaryMove(xEnd, yEnd, x, -0.5, deltaX, deltaY, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - (-0.5));
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + (-0.5));
//                                                  cells[(C_SIZE * (((xEnd - 1 + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - x);
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + x);

                    }
                    //upper local origin
                    else {

                            double deltaY1 = 0.5 - y;
                            deltaX1 = (deltaX / deltaY) * deltaY1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX1 * ((1 - deltaY1) / 2 - y);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX1 * ((1 + deltaY1) / 2 + y);
//                                                  cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 - deltaX1) / 2 - x);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 + deltaX1) / 2 + x);

                            double deltaX2 = -(0.5 + x + deltaX1);
                            double deltaY2 = (deltaY1 / deltaX1) * deltaX2;
                            x += deltaX1;
                            fourBoundaryMove(xStart, yStart+1, x, -0.5, deltaX2, deltaY2, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + (yStart + 1) - 1 + 2)) + Cjx] += pCharge * deltaX2 * ((1 - deltaY2) / 2 - (-0.5));
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + (yStart + 1) + 2)) + Cjx] += pCharge * deltaX2 * ((1 + deltaY2) / 2 + (-0.5));
//                                                  cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + (yStart + 1) + 2)) + Cjy] += pCharge * deltaY2 * ((1 - deltaX2) / 2 - x);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + (yStart + 1) + 2)) + Cjy] += pCharge * deltaY2 * ((1 + deltaX2) / 2 + x);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            y = deltaY2 - 0.5;
                            fourBoundaryMove(xEnd, yEnd, 0.5, y, deltaX, deltaY,p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - y);
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + y);
//                                                  cells[(C_SIZE * (((xEnd - 1 + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - 0.5);
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + 0.5);

                    }
            }
            //moved down
            else {

                    double deltaY1 = -(0.5 + y);
                    //lower local origin
                    if((-(deltaX / deltaY) * deltaY1 - x) < 0.5) {

                            double deltaX1 = (deltaX / deltaY) * deltaY1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1,p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX1 * ((1 - deltaY1) / 2 - y);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX1 * ((1 + deltaY1) / 2 + y);
//                                                  cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 - deltaX1) / 2 - x);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 + deltaX1) / 2 + x);

                            double deltaX2 = -(0.5 + x + deltaX1);
                            double deltaY2 = (deltaY / deltaX) * deltaX2;
                            x += deltaX1;
                            fourBoundaryMove(xStart, yStart-1, x, 0.5, deltaX2, deltaY2, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + (yStart - 1) - 1 + 2)) + Cjx] += pCharge * deltaX2 * ((1 - deltaY2) / 2 - 0.5);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + (yStart - 1) + 2)) + Cjx] += pCharge * deltaX2 * ((1 + deltaY2) / 2 + 0.5);
//                                                  cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + (yStart - 1) + 2)) + Cjy] += pCharge * deltaY2 * ((1 - deltaX2) / 2 - x);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + (yStart - 1) + 2)) + Cjy] += pCharge * deltaY2 * ((1 + deltaX2) / 2 + x);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            y = 0.5 + deltaY2;
                            fourBoundaryMove(xEnd, yEnd, 0.5, y, deltaX, deltaY, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - y);
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + y);
//                                                  cells[(C_SIZE * (((xEnd - 1 + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - 0.5);
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + 0.5);

                    }
                    //upper local origin
                    else {

                            double deltaX1 = -(0.5 + x);
                            deltaY1 = (deltaY / deltaX) * deltaX1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX1 * ((1 - deltaY1) / 2 - y);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX1 * ((1 + deltaY1) / 2 + y);
//                                                  cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 - deltaX1) / 2 - x);
//                                                  cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY1 * ((1 + deltaX1) / 2 + x);

                            double deltaY2 = -(0.5 + y + deltaY1);
                            double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
                            y += deltaY1;
                            fourBoundaryMove(xStart+1, yStart, 0.5, y, deltaX2, deltaY2, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * ((((xStart + 1) + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX2 * ((1 - deltaY2) / 2 - y);
//                                                  cells[(C_SIZE * ((((xStart + 1) + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX2 * ((1 + deltaY2) / 2 + y);
//                                                  cells[(C_SIZE * ((((xStart + 1) - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY2 * ((1 - deltaX2) / 2 - 0.5);
//                                                  cells[(C_SIZE * ((((xStart + 1) + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY2 * ((1 + deltaX2) / 2 + 0.5);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            x = 0.5 + deltaX2;
                            fourBoundaryMove(xEnd, yEnd, x, 0.5, deltaX, deltaY, p, tstep, cells, numCellsY, semaphor, boundaries);

//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - 0.5);
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + 0.5);
//                                                  cells[(C_SIZE * (((xEnd - 1 + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - x);
//                                                  cells[(C_SIZE * (((xEnd + 2) * numCellsY) + yEnd + 2)) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + x);

                    }
            }
    }
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
    
    //a) particle.storePosition() 
         particles[i + PrevX] = particles[i + X];
         particles[i + PrevY] = particles[i + Y];

    //b)solver.step(particle, force, timeStep)/----Boris solver-----/
         int j = i;//(int)(i/P_SIZE);

         double getPositionComponentofForceX        = particles[i + Charge] * particles[i + Ex];//force[j + PositionComponentofForceX];
         double getPositionComponentofForceY        = particles[i + Charge] * particles[i + Ey];//force[j + PositionComponentofForceY];
         double getBz                               = particles[i + PBz];//force[j + Bz];
         double getTangentVelocityComponentOfForceX = 0;//force[j + TangentVelocityComponentOfForceX];
         double getTangentVelocityComponentOfForceY = 0;//force[j + TangentVelocityComponentOfForceY];
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
                                  int numCellsX,
                                  int numCellsY,
                                  double cellWidth,
                                  double cellHeight,
                                  int iterations) 
{
        int i = get_global_id(0);
        if(i > 0)
             return;

        int j;
        for(j = 0; j < numCellsX * numCellsY; j += C_SIZE){
            cells[j + Cjx] = 0;
            cells[j + Cjy] = 0;
        }
}



//##################################################################################################################/
__kernel void charge_conserving_CIC(__global double* particles,                                 
                                 __global double* cells,
                                 __global double* force,
                                 __global int* semaphor,
                                 __global int* boundaries,
                                  double timeStep,
                                  int n,
                                  double width,
                                  double height,
                                  int numCellsX,
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
         //4-boundary move?
         if (xStart == xEnd && yStart == yEnd) {
                    fourBoundaryMove(xStart, yStart, x, y, deltaX, deltaY, pCharge, timeStep, cells, numCellsX, semaphor, boundaries);
                        
//                      cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart - 1 + 2)) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - y);
//                      cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + y);
//                      cells[(C_SIZE * (((xStart - 1 + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - x);
//                      cells[(C_SIZE * (((xStart + 2) * numCellsY) + yStart + 2)) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + x);
//                        
                 }
         //7-boundary move?
         else if (xStart == xEnd || yStart == yEnd) {                       
                    sevenBoundaryMove(x, y, xStart, yStart, xEnd, yEnd, deltaX, deltaY, pCharge, timeStep, cells, numCellsX, semaphor, boundaries);
                 }
                 // 10-boundary move
                else {                              
                            tenBoundaryMove(x, y, xStart, yStart, xEnd, yEnd, deltaX, deltaY, pCharge, timeStep, cells, numCellsX, semaphor, boundaries);                             
                }

}


//##################################################################################################################/
__kernel void cloud_in_cell(__global double* particles,                                 
                            __global double* cells,
                            __global double* force,
                            __global int* semaphor,
                            __global int* boundaries,
                             double timeStep,
                             int n,
                             double width,
                             double height,
                             int numCellsX,
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

        int mark;
        int k;
        double pCharge = particles[i + Charge];
        double pVx     = particles[i + Vx];
        double pVy     = particles[i + Vy];
        
        a = particles[i + X]/cellWidth;
        ii = (int) floor(a);
        a -= ii;
        b = 1 - a;

        c = particles[i + Y]/cellHeight;
        jj = (int)floor(c);
        c -= jj;
        d = 1 - c;
       
        if( c < 0.5 ){
                jj -= 1;
                c += 0.5;
                d -= 0.5;

                mark = boundaries[((ii + 2) * numCellsY) + jj + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjx] += pCharge * pVx * b * d;
                    }
                }
                
                mark = boundaries[((ii + 2) * numCellsY) + jj + 1 + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjx] += pCharge * pVx * b * c;
                    }
                }
                
                mark = boundaries[((ii + 1 + 2) * numCellsY) + jj + 1 + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjx] += pCharge * pVx * a * c;
                    }
                }
                
                mark = boundaries[((ii + 1 + 2) * numCellsY) + jj + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjx] += pCharge * pVx * a * d;
                    }
                }

                c -= 0.5;
                d += 0.5;
                jj += 1;
        } else {
                c -= 0.5;
                d += 0.5;

                mark = boundaries[((ii + 2) * numCellsY) + jj + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjx] += pCharge * pVx * b * d;
                    }
                }
                
                mark = boundaries[((ii + 2) * numCellsY) + jj + 1 + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjx] += pCharge * pVx * b * c;
                    }
                }
                
                mark = boundaries[((ii + 1 + 2) * numCellsY) + jj + 1 + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjx] += pCharge * pVx * a * c;
                    }
                }
                
                mark = boundaries[((ii + 1 + 2) * numCellsY) + jj + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjx] += pCharge * pVx * a * d;
                    }
                }

                c += 0.5;
                d -= 0.5;
        }
     
        if( a < 0.5 ){
                ii -= 1;
                a += 0.5;
                b -= 0.5;

                mark = boundaries[((ii + 2) * numCellsY) + jj + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjy] += pCharge * pVy * b * d;
                    }
                }
                
                mark = boundaries[((ii + 2) * numCellsY) + jj + 1 + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjy] += pCharge * pVy * b * c;
                    }
                }
                
                mark = boundaries[((ii + 1 + 2) * numCellsY) + jj + 1 + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjy] += pCharge * pVy * a * c;
                    }
                }
                
                mark = boundaries[((ii + 1 + 2) * numCellsY) + jj + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjy] += pCharge * pVy * a * d;
                    }
                }

        } else {
                a -= 0.5;
                b += 0.5;

                mark = boundaries[((ii + 2) * numCellsY) + jj + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjy] += pCharge * pVy * b * d;
                    }
                }
                
                mark = boundaries[((ii + 2) * numCellsY) + jj + 1 + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjy] += pCharge * pVy * b * c;
                    }
                }
                
                mark = boundaries[((ii + 1 + 2) * numCellsY) + jj + 1 + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjy] += pCharge * pVy * a * c;
                    }
                }
                
                mark = boundaries[((ii + 1 + 2) * numCellsY) + jj + 2];
                for(k = 0; k < numCellsY*numCellsY; k++){
                        if(boundaries[k] == mark){
                            cells[(C_SIZE * k) + Cjy] += pCharge * pVy * a * d;
                    }
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
                            __global int* boundaries,
                               double timeStep,
                               int n,
                               double width,
                               double height,
                               int numCellsX,
                               int numCellsY,
                               double cellWidth,
                               double cellHeight,
                               int iterations) 
{
        
         int i = get_global_id(0);
         if(i > 0)
              return;
        
         int numCellsXTotal = numCellsX + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;
         int numCellsYTotal = numCellsY + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;

         int  k;
         for(k = 0; k < numCellsXTotal * numCellsYTotal * C_SIZE; k+=C_SIZE){
             cells[k + Cbzo] = cells[k + Cbz];
         }
}

//##################################################################################################################/
__kernel void solve_for_e(  __global double* particles,
                            __global const double* force, 
                            __global double* cells,   
                            __global int* boundaries,
                               double timeStep,
                               int n,
                               double width,
                               double height,
                               int numCellsX,
                               int numCellsY,
                               double cellWidth,
                               double cellHeight,
                               int iterations) 
{
        
         int i = get_global_id(0);
         if(i > 0)
              return;

         int h, k, l, m;
         int mark;
         double cx, cy;
         double a, b; 
        
         
         for(h = 0; h <= 9; h++){
             for(k = 0; k <= 9; k++){
                 cx = (cells[( C_SIZE * (((h + 2) * numCellsY) + k + 1 + 2)) + Cbz] - 
                       cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cbz])/cellHeight;
                 cy = -(cells[(C_SIZE * (((h + 1 + 2) * numCellsY) + k + 2)) + Cbz] - 
                        cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cbz])/cellWidth;
                 a = timeStep * (cx - cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cjx]);
                 b = timeStep * (cy - cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cjy]);
                
                 mark = boundaries[((h + 2) * numCellsY) + k + 2];
                 for(l = 0; l < numCellsY*numCellsY; l++){
                        if(boundaries[l] == mark){
                            cells[(C_SIZE * l) + Cex] += a;
                        }
                 }
                
                 mark = boundaries[((h + 2) * numCellsY) + k + 2];
                 for(l = 0; l < numCellsY*numCellsY; l++){
                        if(boundaries[l] == mark){
                            cells[(C_SIZE * l) + Cey] += b;
                        }
                 }
             }
         }
        
}
    
//##################################################################################################################/
__kernel void solve_for_b(  __global double* particles,
                            __global const double* force, 
                            __global double* cells,   
                            __global int* boundaries,
                               double timeStep,
                               int n,
                               double width,
                               double height,
                               int numCellsX,
                               int numCellsY,
                               double cellWidth,
                               double cellHeight,
                               int iterations) 
{
        
         int i = get_global_id(0);
         if(i > 0)
              return;

         int h, k, l;
         int mark;
         double cz;

         for(h = 0; h <= 9; h++){
             for(k = 0; k <= 9; k++){
                cz = (cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cey] - 
                      cells[(C_SIZE * (((h - 1 + 2) * numCellsY) + k + 2)) + Cey])/cellWidth -
                     (cells[(C_SIZE * (((h + 2) * numCellsY) + k + 2)) + Cex] - 
                      cells[(C_SIZE * (((h + 2) * numCellsY) + k - 1 + 2)) + Cex])/cellHeight;

                 mark = boundaries[((h + 2) * numCellsY) + k + 2];
                 for(l = 0; l < numCellsY*numCellsY; l++){
                        if(boundaries[l] == mark){
                            cells[(C_SIZE * l) + Cbz] += (-timeStep * cz);
                        }
                 }
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
                                  int numCellsX,
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
         a  -= ii;
         b  = 1 - a;

         c  = particles[i + Y] / cellHeight;
         jj = (int)floor(c);
         c  -= jj;
         d  = 1 - c;

//         printf("%g %g %g %g\n", a, b, c, d);
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

//#endif