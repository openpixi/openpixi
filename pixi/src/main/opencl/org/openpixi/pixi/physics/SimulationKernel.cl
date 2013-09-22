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

//#pragma OPENCL EXTENSION cl_khr_fp64 : enable
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
    

void fourBoundaryMove(int lx, int ly, double x, double y, double deltaX, double deltaY, double pCharge, 
                      double tstep, __global double *cells, int numCellsX, int numCellsY, __global int* boundaries) {
    int mark, nextIndex, originalIndex;
    int i, j;
    
    originalIndex =((lx + 2) * numCellsY) + ly - 1 + 2;
    cells[(C_SIZE * originalIndex) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - y);
    nextIndex = boundaries[originalIndex];
    while(nextIndex != originalIndex){
        cells[(C_SIZE * nextIndex) + Cjx] += pCharge * deltaX * ((1 - deltaY) / 2 - y);
        nextIndex = boundaries[nextIndex];
    }
    
    originalIndex =((lx + 2) * numCellsY) + ly + 2;
    cells[(C_SIZE * originalIndex) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + y);
    nextIndex = boundaries[originalIndex];
    while(nextIndex != originalIndex){
        cells[(C_SIZE * nextIndex) + Cjx] += pCharge * deltaX * ((1 + deltaY) / 2 + y);
        nextIndex = boundaries[nextIndex];
    }
    
    originalIndex =((lx - 1 + 2) * numCellsY) + ly + 2;
    cells[(C_SIZE * originalIndex) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - x);
    nextIndex = boundaries[originalIndex];
    while(nextIndex != originalIndex){
        cells[(C_SIZE * nextIndex) + Cjy] += pCharge * deltaY * ((1 - deltaX) / 2 - x);
        nextIndex = boundaries[nextIndex];
    }
   
    originalIndex =((lx + 2) * numCellsY) + ly + 2;
    cells[(C_SIZE * originalIndex) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + x);
    nextIndex = boundaries[originalIndex];
    while(nextIndex != originalIndex){
        cells[(C_SIZE * nextIndex) + Cjy] += pCharge * deltaY * ((1 + deltaX) / 2 + x);
        nextIndex = boundaries[nextIndex];
    }

}

void sevenBoundaryMove(double x, double y, int xStart, int yStart, int xEnd, int yEnd,double deltaX,
                       double deltaY, double p, double tstep, __global double *cells, int numCellsX, int numCellsY, __global int* boundaries) {
    //7-boundary move with equal y?
    if (yStart == yEnd) {
            //particle moves right?
            if (xEnd > xStart) {

                    double deltaX1 = 0.5 - x;
                    double deltaY1 = (deltaY / deltaX) * deltaX1;
                    fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsX, numCellsY, boundaries);                                   


                    deltaX -= deltaX1;
                    deltaY -= deltaY1;
                    y += deltaY1;
                    fourBoundaryMove(xEnd, yEnd, - 0.5, y, deltaX, deltaY, p, tstep, cells, numCellsX, numCellsY, boundaries);

            }
            //particle moves left
            else {

                    double deltaX1 = -(0.5 + x);
                    double deltaY1 = (deltaY / deltaX) * deltaX1;
                    fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsX, numCellsY, boundaries);


                    deltaX -= deltaX1;
                    deltaY -= deltaY1;
                    y += deltaY1;
                    fourBoundaryMove(xEnd, yEnd, 0.5, y, deltaX, deltaY, p, tstep, cells, numCellsX, numCellsY, boundaries);

            }
    }
    //7-boundary move with equal x?
    if (xStart == xEnd) {
            //particle moves up?
            if (yEnd > yStart) {

                    double deltaY1 = 0.5 - y;
                    double deltaX1 = deltaX  * (deltaY1 / deltaY);
                    fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsX, numCellsY, boundaries);

                    deltaX -= deltaX1;
                    deltaY -= deltaY1;
                    y += deltaY1;
                    fourBoundaryMove(xEnd, yEnd, x, -0.5, deltaX, deltaY, p, tstep, cells, numCellsX, numCellsY, boundaries);

            }
            //particle moves down
            else {

                    double deltaY1 = -(0.5 + y);
                    double deltaX1 = (deltaX / deltaY) * deltaY1;
                    fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsX, numCellsY, boundaries);

                    deltaX -= deltaX1;
                    deltaY -= deltaY1;
                    y += deltaY1;
                    fourBoundaryMove(xEnd, yEnd, x, 0.5, deltaX, deltaY, p, tstep, cells, numCellsX, numCellsY, boundaries);


            }
    }
}

void tenBoundaryMove(double x, double y, int xStart, int yStart, int xEnd, int yEnd, double deltaX,
                     double deltaY, double p, double tstep, __global double *cells, int numCellsX, int numCellsY, __global int* boundaries) {
    //moved right?
    if (xEnd == (xStart+1)) {
            //moved up?
            if (yEnd == (yStart+1)) {

                    double deltaX1 = 0.5 - x;

                    //lower local origin
                    if(((deltaY / deltaX) * deltaX1 + y) < 0.5) {

                            double deltaY1 = (deltaY / deltaX) * deltaX1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            double deltaY2 = 0.5 - y - deltaY1;
                            double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
                            y += deltaY1;
                            fourBoundaryMove(xStart+1, yStart, -0.5, y, deltaX2, deltaY2, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            x = deltaX2 - 0.5;
                            fourBoundaryMove(xEnd, yEnd, x, -0.5, deltaX, deltaY, p, tstep, cells, numCellsX, numCellsY, boundaries);

                    }
                    //upper local origin
                    else {

                            double deltaY1 = 0.5 - y;
                            deltaX1 = (deltaX / deltaY) * deltaY1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            double deltaX2 = 0.5 - x - deltaX1;
                            double deltaY2 = (deltaY1 / deltaX1) * deltaX2;
                            x += deltaX1;
                            fourBoundaryMove(xStart, yStart+1, x, -0.5, deltaX2, deltaY2, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            y = deltaY2 - 0.5;
                            fourBoundaryMove(xEnd, yEnd, -0.5, y, deltaX, deltaY, p, tstep, cells, numCellsX, numCellsY, boundaries);

                    }
            }
            //moved down
            else {

                    double deltaY1 = -(0.5 + y);

                    //lower local origin
                    if(((deltaX / deltaY) * deltaY1 + x) < 0.5) {

                            double deltaX1 = (deltaX / deltaY) * deltaY1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            double deltaX2 = 0.5 - x - deltaX1;
                            double deltaY2 = (deltaY / deltaX) * deltaX2;
                            x += deltaX1;
                            fourBoundaryMove(xStart, yStart-1, x, 0.5, deltaX2, deltaY2, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            y = 0.5 + deltaY2;
                            fourBoundaryMove(xEnd, yEnd, -0.5, y, deltaX, deltaY, p, tstep, cells, numCellsX, numCellsY, boundaries);

                    }
                    //upper local origin
                    else {

                            double deltaX1 = 0.5 - x;
                            deltaY1 = (deltaY / deltaX) * deltaX1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            double deltaY2 = -(0.5 + y + deltaY1);
                            double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
                            y += deltaY1;
                            fourBoundaryMove(xStart+1, yStart, -0.5, y, deltaX2, deltaY2, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            x = deltaX2 - 0.5;
                            fourBoundaryMove(xEnd, yEnd, x, 0.5, deltaX, deltaY, p, tstep, cells, numCellsX, numCellsY, boundaries);

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
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            double deltaY2 = 0.5 - y - deltaY1;
                            double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
                            y += deltaY1;
                            fourBoundaryMove(xStart-1, yStart, 0.5, y, deltaX2, deltaY2, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            x = 0.5 + deltaX2;
                            fourBoundaryMove(xEnd, yEnd, x, -0.5, deltaX, deltaY, p, tstep, cells, numCellsX, numCellsY, boundaries);

                    }
                    //upper local origin
                    else {

                            double deltaY1 = 0.5 - y;
                            deltaX1 = (deltaX / deltaY) * deltaY1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            double deltaX2 = -(0.5 + x + deltaX1);
                            double deltaY2 = (deltaY1 / deltaX1) * deltaX2;
                            x += deltaX1;
                            fourBoundaryMove(xStart, yStart+1, x, -0.5, deltaX2, deltaY2, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            y = deltaY2 - 0.5;
                            fourBoundaryMove(xEnd, yEnd, 0.5, y, deltaX, deltaY,p, tstep, cells, numCellsX, numCellsY, boundaries);

                    }
            }
            //moved down
            else {

                    double deltaY1 = -(0.5 + y);
                    //lower local origin
                    if((-(deltaX / deltaY) * deltaY1 - x) < 0.5) {

                            double deltaX1 = (deltaX / deltaY) * deltaY1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1,p, tstep, cells, numCellsX, numCellsY, boundaries);

                            double deltaX2 = -(0.5 + x + deltaX1);
                            double deltaY2 = (deltaY / deltaX) * deltaX2;
                            x += deltaX1;
                            fourBoundaryMove(xStart, yStart-1, x, 0.5, deltaX2, deltaY2, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            y = 0.5 + deltaY2;
                            fourBoundaryMove(xEnd, yEnd, 0.5, y, deltaX, deltaY, p, tstep, cells, numCellsX, numCellsY, boundaries);

                    }
                    //upper local origin
                    else {

                            double deltaX1 = -(0.5 + x);
                            deltaY1 = (deltaY / deltaX) * deltaX1;
                            fourBoundaryMove(xStart, yStart, x, y, deltaX1, deltaY1, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            double deltaY2 = -(0.5 + y + deltaY1);
                            double deltaX2 = (deltaX1 / deltaY1) * deltaY2;
                            y += deltaY1;
                            fourBoundaryMove(xStart+1, yStart, 0.5, y, deltaX2, deltaY2, p, tstep, cells, numCellsX, numCellsY, boundaries);

                            deltaX -= (deltaX1 + deltaX2);
                            deltaY -= (deltaY1 + deltaY2);
                            x = 0.5 + deltaX2;
                            fourBoundaryMove(xEnd, yEnd, x, 0.5, deltaX, deltaY, p, tstep, cells, numCellsX, numCellsY, boundaries);
                    }
            }
    }
}

/*-----------------------------------------------------------------------------/
/------------------- Step 1: particlePush()------------------------------------/
/-----------------------------------------------------------------------------*/
//##################################################################################################################/
__kernel void particle_push_boris( __global double* particles,                      
                                            double timeStep,
                                            int n,
                                            int particlesSize,
                                            int start,
                                            double width,
                                            double height)
{
   int i = get_global_id(0);
   if(i >= n)
        return;
   i = i * P_SIZE + start * P_SIZE;
   if(i > particlesSize * P_SIZE)
        return;
        
   
    //a) particle.storePosition() 
         particles[i + PrevX] = particles[i + X];
         particles[i + PrevY] = particles[i + Y];

    //b)solver.step(particle, force, timeStep)/----Boris solver-----/
         int j = i;//(int)(i/P_SIZE);
        
         double getPositionComponentofForceX        = particles[i + Charge] * particles[i + Ex];
         double getPositionComponentofForceY        = particles[i + Charge] * particles[i + Ey];
         double getBz                               = particles[i + PBz];
         double getTangentVelocityComponentOfForceX = 0;
         double getTangentVelocityComponentOfForceY = 0;
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

//##################################################################################################################/
__kernel void particle_push_boris_damped( __global double* particles,                      
                                                   double timeStep,
                                                   int n,
                                                   int particlesSize,
                                                   int start,
                                                   double width,
                                                   double height)
{
   int i = get_global_id(0);
   if(i >= n)
        return;
   i = i * P_SIZE + start * P_SIZE;
   if(i > particlesSize * P_SIZE)
        return;
    
    //a) particle.storePosition() 
         particles[i + PrevX] = particles[i + X];
         particles[i + PrevY] = particles[i + Y];

    //b)solver.step(particle, force, timeStep)/----Boris solver-----/
         int j = i;//(int)(i/P_SIZE);

         double getPositionComponentofForceX        = particles[i + Charge] * particles[i + Ex];
         double getPositionComponentofForceY        = particles[i + Charge] * particles[i + Ey];
         double getBz                               = particles[i + PBz];
         double getLinearDragCoefficient            = 0;
         double getMass                             = particles[i + Mass];

         // remember for complete()
         particles[i + PrevPositionComponentForceX]          = getPositionComponentofForceX;
         particles[i + PrevPositionComponentForceY]          = getPositionComponentofForceY;
         particles[i + PrevBz]                               = getBz;
         particles[i + PrevLinearDragCoefficient]            = getLinearDragCoefficient;

         //help coefficients for the dragging
         double help1_coef = 1 - getLinearDragCoefficient * timeStep / (2 * getMass);
         double help2_coef = 1 + getLinearDragCoefficient * timeStep / (2 * getMass);

         double vxminus = help1_coef * particles[i + Vx] / help2_coef + getPositionComponentofForceX * timeStep / (2.0 * getMass * help2_coef);
         double vyminus = help1_coef * particles[i + Vy] / help2_coef + getPositionComponentofForceY * timeStep / (2.0 * getMass * help2_coef);

         double t_z = particles[i + Charge] * getBz * timeStep / (2.0 * getMass * help2_coef);   //t vector

         double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector

         double kappa = - 4 * getMass * getLinearDragCoefficient * timeStep / (4 * getMass * getMass - 
                        getLinearDragCoefficient * getLinearDragCoefficient * timeStep * timeStep);

         double vxprime = vxminus + help2_coef * vyminus * t_z / help1_coef + kappa * timeStep * getPositionComponentofForceY * t_z / (2.0 * getMass);
         double vyprime = vyminus - help2_coef * vxminus * t_z / help1_coef - kappa * timeStep * getPositionComponentofForceX * t_z / (2.0 * getMass);

         double vxplus = vxminus + vyprime * s_z + (help2_coef / help1_coef - 1) * (vyminus * t_z + vxminus * t_z * t_z) / (1 + t_z * t_z) +
                         kappa * timeStep * (getPositionComponentofForceY + getPositionComponentofForceX * t_z) * s_z / (4.0 * getMass);
 
         double vyplus = vyminus - vxprime * s_z + (help2_coef / help1_coef - 1) * (- vxminus * t_z + vyminus * t_z * t_z) / (1 + t_z * t_z) -
                         kappa * timeStep * (getPositionComponentofForceX - getPositionComponentofForceY * t_z) * s_z / (4.0 * getMass);

         particles[i + Vx] = vxplus + getPositionComponentofForceX * timeStep / (2.0 * getMass * help2_coef);
         particles[i + Vy] = vyplus + getPositionComponentofForceY * timeStep / (2.0 * getMass * help2_coef);

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
}

//##################################################################################################################/
__kernel void particle_push_euler( __global double* particles,                      
                                                   double timeStep,
                                                   int n,
                                                   int particlesSize,
                                                   int start,
                                                   double width,
                                                   double height)
{
   int i = get_global_id(0);
   if(i >= n)
        return;
   i = i * P_SIZE + start * P_SIZE;
   if(i > particlesSize * P_SIZE)
        return;
    
    //a) particle.storePosition() 
         particles[i + PrevX] = particles[i + X];
         particles[i + PrevY] = particles[i + Y];

    //b)solver.step(particle, force, timeStep)/----Boris solver-----/
         //a(t) = F(v(t), x(t)) / m
         particles[i + Ax] = (particles[i + Charge] * (particles[i + Ex] + particles[i + Vy] * particles[i + PBz]))/particles[i + Mass];
         particles[i + Ay] = (particles[i + Charge] * (particles[i + Ey] + particles[i + Vx] * particles[i + PBz]))/particles[i + Mass];

         // x(t+dt) = x(t) + v(t)*dt
         particles[i + X] = particles[i + X] + particles[i + Vx] * timeStep;
         particles[i + Y] = particles[i + Y] + particles[i + Vy] * timeStep;

         // v(t+dt) = v(t) + a(t)*dt
         particles[i + Vx] = particles[i + Vx] + particles[i + Ax] * timeStep;
         particles[i + Vy] = particles[i + Vy] + particles[i + Ay] * timeStep;
        
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
}

//##################################################################################################################/
__kernel void particle_push_euler_richardson( __global double* particles,                      
                                                   double timeStep,
                                                   int n,
                                                   int particlesSize,
                                                   int start,
                                                   double width,
                                                   double height)
{
   int i = get_global_id(0);
   if(i >= n)
        return;
   i = i * P_SIZE + start * P_SIZE;
   if(i > particlesSize * P_SIZE)
        return;
    
    //a) particle.storePosition() 
         particles[i + PrevX] = particles[i + X];
         particles[i + PrevY] = particles[i + Y];

    //b)solver.step(particle, force, timeStep)/----Boris solver-----/
         //saving the starting value of the position & velocity
         double xstart  = particles[i + X];
         double ystart  = particles[i + Y];
         double vxstart = particles[i + Vx];
         double vystart = particles[i + Vy];

         //a(t) = F(v(t), x(t)) / m
         particles[i + Ax] = (particles[i + Charge] * (particles[i + Ex] + particles[i + Vy] * particles[i + PBz]))/particles[i + Mass];
         particles[i + Ay] = (particles[i + Charge] * (particles[i + Ey] + particles[i + Vx] * particles[i + PBz]))/particles[i + Mass];


         //starting the Euler-Richardson algorithm (the equations correspond with the ones on the above mentioned website)
         //v(t + dt / 2) = v(t) + a(t) * dt / 2
         particles[i + Vx] = particles[i + Vx] + particles[i + Ax] * timeStep/2;
         particles[i + Vy] = particles[i + Vy] + particles[i + Ay] * timeStep/2;

         //x(t + dt / 2) = x(t) + v(t) * dt / 2
         particles[i + X] = particles[i + X] + particles[i + Vx] * timeStep/2;
         particles[i + Y] = particles[i + Y] + particles[i + Vy] * timeStep/2;

         //a(t + dt / 2) = F(v(t + dt / 2), x(t + dt / 2)) / m
         particles[i + Ax] = (particles[i + Charge] * (particles[i + Ex] + particles[i + Vy] * particles[i + PBz]))/particles[i + Mass];
         particles[i + Ay] = (particles[i + Charge] * (particles[i + Ey] + particles[i + Vx] * particles[i + PBz]))/particles[i + Mass];

         //x(t + dt) = x(t) + v(t + dt / 2) * dt
         particles[i + X] = xstart + particles[i + Vx] * timeStep;
         particles[i + Y] = ystart + particles[i + Vy] * timeStep;
        
         //v(t + dt) = v(t) + a(t + dt / 2) * dt
         particles[i + Vx] = vxstart + particles[i + Ax] * timeStep;
         particles[i + Vy] = vystart + particles[i + Ay] * timeStep;
        
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
}

//##################################################################################################################/
__kernel void particle_push_semiimplicit_euler( __global double* particles,                      
                                                   double timeStep,
                                                   int n,
                                                   int particlesSize,
                                                   int start,
                                                   double width,
                                                   double height)
{
   int i = get_global_id(0);
   if(i >= n)
        return;
   i = i * P_SIZE + start * P_SIZE;
   if(i > particlesSize * P_SIZE)
        return;
    
    //a) particle.storePosition() 
         particles[i + PrevX] = particles[i + X];
         particles[i + PrevY] = particles[i + Y];

    //b)solver.step(particle, force, timeStep)/----Boris solver-----/
         //a(t) = F(v(t), x(t)) / m
         particles[i + Ax] = (particles[i + Charge] * (particles[i + Ex] + particles[i + Vy] * particles[i + PBz]))/particles[i + Mass];
         particles[i + Ay] = (particles[i + Charge] * (particles[i + Ey] + particles[i + Vx] * particles[i + PBz]))/particles[i + Mass];

         // v(t+dt) = v(t) + a(t)*dt
         particles[i + Vx] = particles[i + Vx] + particles[i + Ax] * timeStep;
         particles[i + Vy] = particles[i + Vy] + particles[i + Ay] * timeStep;

         // x(t+dt) = x(t) + v(t)*dt
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
}

//##################################################################################################################/
__kernel void particle_push_leap_frog( __global double* particles,                      
                                                   double timeStep,
                                                   int n,
                                                   int particlesSize,
                                                   int start,
                                                   double width,
                                                   double height)
{
   int i = get_global_id(0);
   if(i >= n)
        return;
   i = i * P_SIZE + start * P_SIZE;
   if(i > particlesSize * P_SIZE)
        return;
    
    //a) particle.storePosition() 
         particles[i + PrevX] = particles[i + X];
         particles[i + PrevY] = particles[i + Y];

    //b)solver.step(particle, force, timeStep)/----Boris solver-----/
         particles[i + X] = particles[i + X] + particles[i + Vx] * timeStep;
         particles[i + Y] = particles[i + Y] + particles[i + Vy] * timeStep; 
        
         particles[i + Ax] = (particles[i + Charge] * (particles[i + Ex] + particles[i + Vy] * particles[i + PBz]))/particles[i + Mass];
         particles[i + Ay] = (particles[i + Charge] * (particles[i + Ey] + particles[i + Vx] * particles[i + PBz]))/particles[i + Mass];

         particles[i + Vx] = particles[i + Vx] + particles[i + Ax] * timeStep;
         particles[i + Vy] = particles[i + Vy] + particles[i + Ay] * timeStep;
        
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
}

//##################################################################################################################/
__kernel void particle_push_leap_frog_damped( __global double* particles,                      
                                                   double timeStep,
                                                   int n,
                                                   int particlesSize,
                                                   int start,
                                                   double width,
                                                   double height)
{
   int i = get_global_id(0);
   if(i >= n)
        return;
   i = i * P_SIZE + start * P_SIZE;
   if(i > particlesSize * P_SIZE)
        return;
    
    //a) particle.storePosition() 
         particles[i + PrevX] = particles[i + X];
         particles[i + PrevY] = particles[i + Y];

    //b)solver.step(particle, force, timeStep)/----Boris solver-----/
         double getPositionComponentofForceX        = particles[i + Charge] * particles[i + Ex];
         double getPositionComponentofForceY        = particles[i + Charge] * particles[i + Ey];
         double getNormalVelocityComponentofForceX  = particles[i + Charge] * particles[i + Vy] * particles[i+ PBz];
         double getNormalVelocityComponentofForceY  = -particles[i + Charge] * particles[i + Vx] * particles[i+ PBz];
         double getLinearDragCoefficient            = 0;
         double getMass = particles[i + Mass];

         // remember for complete()
         particles[i + PrevPositionComponentForceX]          = getPositionComponentofForceX;
         particles[i + PrevPositionComponentForceY]          = getPositionComponentofForceY;
         particles[i + PrevNormalVelocityComponentOfForceX]  = getNormalVelocityComponentofForceX;
         particles[i + PrevNormalVelocityComponentOfForceY]  = getNormalVelocityComponentofForceY;
        
         //help coefficients for the dragging
         double help1_coef = 1 - getLinearDragCoefficient * timeStep / (2 * getMass);
         double help2_coef = 1 + getLinearDragCoefficient * timeStep / (2 * getMass);

         // v(t+dt/2) = v(t-dt/2) + a(t)*dt
         particles[i + Vx] = (particles[i + Vx] * help1_coef + particles[i + Ax] * timeStep) / help2_coef;
         particles[i + Vy] = (particles[i + Vy] * help1_coef + particles[i + Ay] * timeStep) / help2_coef;


         // x(t+dt) = x(t) + v(t+dt/2)*dt
         particles[i + X] = particles[i + X] + particles[i + Vx] * timeStep;
         particles[i + Y] = particles[i + Y] + particles[i + Vy] * timeStep; 

         // a(t+dt) = F(v(t+dt/2), x(t+dt)) / m
         // WARNING: Force is evaluated at two different times t+dt/2 and t+dt!
         particles[i + Ax] = (getPositionComponentofForceX + getNormalVelocityComponentofForceX) / getMass;
         particles[i + Ay] = (getPositionComponentofForceY + getNormalVelocityComponentofForceY) / getMass;
        
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
}

//##################################################################################################################/
__kernel void particle_push_leap_frog_half_step( __global double* particles,                      
                                                   double timeStep,
                                                   int n,
                                                   int particlesSize,
                                                   int start,
                                                   double width,
                                                   double height)
{
   int i = get_global_id(0);
   if(i >= n)
        return;
   i = i * P_SIZE + start * P_SIZE;
   if(i > particlesSize * P_SIZE)
        return;
    
    //a) particle.storePosition() 
         particles[i + PrevX] = particles[i + X];
         particles[i + PrevY] = particles[i + Y];

    //b)solver.step(particle, force, timeStep)/----Boris solver-----/
         particles[i + Vx] = particles[i + Vx] + particles[i + Ax] * timeStep / 2.0;
         particles[i + Vy] = particles[i + Vy] + particles[i + Ay] * timeStep / 2.0;
    
         particles[i + X] = particles[i + X] + particles[i + Vx] * timeStep;
         particles[i + Y] = particles[i + Y] + particles[i + Vy] * timeStep; 
        
         particles[i + Ax] = (particles[i + Charge] * (particles[i + Ex] + particles[i + Vy] * particles[i + PBz]))/particles[i + Mass];
         particles[i + Ay] = (particles[i + Charge] * (particles[i + Ey] + particles[i + Vx] * particles[i + PBz]))/particles[i + Mass];

         particles[i + Vx] = particles[i + Vx] + particles[i + Ax] * timeStep / 2.0;
         particles[i + Vy] = particles[i + Vy] + particles[i + Ay] * timeStep / 2.0;
        
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
}


 /*-----------------------------------------------------------------------------/
 /------Step 4: interpolation.interpolateToGrid(particles, grid, tstep)---------/
 /-----------------------------------------------------------------------------*/
//##################################################################################################################/
__kernel void reset_current(__global double* cells,                                     
                                     int n,                                 
                                     int numCellsXTotal,
                                     int numCellsYTotal
                                     )
{
        int i = get_global_id(0);
        if(i > 0)
             return;

        int h, k;
        for(h = -2; h < numCellsXTotal - 2; h++){
             for(k = -2; k < numCellsYTotal - 2; k++){
                cells[( C_SIZE * (((h + 2) * numCellsYTotal) + k + 2)) + Cjx] = 0;
                cells[( C_SIZE * (((h + 2) * numCellsYTotal) + k + 2)) + Cjy] = 0;
             }   
        }
}



//##################################################################################################################/
__kernel void charge_conserving_CIC(__global double* particles,                                 
                                    __global double* cells,                                                                  
                                    __global int* boundaries,
                                             double timeStep,
                                             int n,  
                                             int particlesSize,
                                             int start,
                                             int numCellsX,
                                             int numCellsY,
                                             double cellWidth,
                                             double cellHeight)
{
         int i = get_global_id(0);
         if(i >= n)
             return;
         i = i * P_SIZE + start * P_SIZE;
         if(i >= particlesSize * P_SIZE)
             return;
        
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
         int h, k, mark;
        
         //4-boundary move?
         if (xStart == xEnd && yStart == yEnd) {
                    fourBoundaryMove(xStart, yStart, x, y, deltaX, deltaY, pCharge, timeStep, cells, numCellsX, numCellsY, boundaries); 
                 }
         //7-boundary move?
         else if (xStart == xEnd || yStart == yEnd) {                       
                    sevenBoundaryMove(x, y, xStart, yStart, xEnd, yEnd, deltaX, deltaY, pCharge, timeStep, cells, numCellsX, numCellsY, boundaries);                       
                 }
                 // 10-boundary move
                else {                       
                            tenBoundaryMove(x, y, xStart, yStart, xEnd, yEnd, deltaX, deltaY, pCharge, timeStep, cells, numCellsX, numCellsY, boundaries);
                    }
}


//##################################################################################################################/
__kernel void cloud_in_cell(__global double* particles,                                 
                            __global double* cells,                                                                  
                            __global int* boundaries,
                                     double timeStep,
                                     int n,     
                                     int particlesSize,
                                     int start,
                                     int numCellsX,
                                     int numCellsY,
                                     double cellWidth,
                                     double cellHeight) 
{
        int i = get_global_id(0);
        if(i >= n)
            return;
        i = i * P_SIZE + start * P_SIZE;
        if(i >= particlesSize * P_SIZE)
             return;
        
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

        int mark, nextIndex, originalIndex;
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

                originalIndex =((ii + 2) * numCellsY) + jj + 2;
                cells[(C_SIZE * originalIndex) + Cjx] += pCharge * pVx * b * d;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjx] += pCharge * pVx * b * d;
                    nextIndex = boundaries[nextIndex];
                }
                
                originalIndex = ((ii + 2) * numCellsY) + jj + 1 + 2;
                cells[(C_SIZE * originalIndex) + Cjx] += pCharge * pVx * b * c;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjx] += pCharge * pVx * b * c;
                    nextIndex = boundaries[nextIndex];
                }
                
                originalIndex = ((ii + 1 + 2) * numCellsY) + jj + 1 + 2;
                cells[(C_SIZE * originalIndex) + Cjx] += pCharge * pVx * a * c;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjx] += pCharge * pVx * a * c;
                    nextIndex = boundaries[nextIndex];
                }
              
                originalIndex = ((ii + 1 + 2) * numCellsY) + jj + 2;
                cells[(C_SIZE * originalIndex) + Cjx] += pCharge * pVx * a * d;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjx] += pCharge * pVx * a * d;
                    nextIndex = boundaries[nextIndex];
                }
                
                c -= 0.5;
                d += 0.5;
                jj += 1;
        } else {
                c -= 0.5;
                d += 0.5;

                originalIndex = ((ii + 2) * numCellsY) + jj + 2;
                cells[(C_SIZE * originalIndex) + Cjx] += pCharge * pVx * b * d;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjx] += pCharge * pVx * b * d;
                    nextIndex = boundaries[nextIndex];
                }
        
                originalIndex = ((ii + 2) * numCellsY) + jj + 1 + 2;
                cells[(C_SIZE * originalIndex) + Cjx] += pCharge * pVx * b * c;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjx] += pCharge * pVx * b * c;
                    nextIndex = boundaries[nextIndex];
                }
 
                originalIndex = ((ii + 1 + 2) * numCellsY) + jj + 1 + 2;
                cells[(C_SIZE * originalIndex) + Cjx] += pCharge * pVx * a * c;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjx] += pCharge * pVx * a * c;
                    nextIndex = boundaries[nextIndex];
                }
                
                originalIndex = ((ii + 1 + 2) * numCellsY) + jj + 2;
                cells[(C_SIZE * originalIndex) + Cjx] += pCharge * pVx * a * d;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjx] += pCharge * pVx * a * d;
                    nextIndex = boundaries[nextIndex];
                }
                
                c += 0.5;
                d -= 0.5;
        }
     
        if( a < 0.5 ){
                ii -= 1;
                a += 0.5;
                b -= 0.5;

                originalIndex = ((ii + 2) * numCellsY) + jj + 2;
                cells[(C_SIZE * originalIndex) + Cjy] += pCharge * pVy * b * d;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjy] += pCharge * pVy * b * d;
                    nextIndex = boundaries[nextIndex];
                }
                
                originalIndex = ((ii + 2) * numCellsY) + jj + 1 + 2;
                cells[(C_SIZE * originalIndex) + Cjy] += pCharge * pVy * b * c;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjy] += pCharge * pVy * b * c;
                    nextIndex = boundaries[nextIndex];
                }
                
                originalIndex = ((ii + 1 + 2) * numCellsY) + jj + 1 + 2;
                cells[(C_SIZE * originalIndex) + Cjy] += pCharge * pVy * a * c;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjy] += pCharge * pVy * a * c;
                    nextIndex = boundaries[nextIndex];
                }
                
                originalIndex = ((ii + 1 + 2) * numCellsY) + jj + 2;
                cells[(C_SIZE * originalIndex) + Cjy] += pCharge * pVy * a * d;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjy] += pCharge * pVy * a * d;
                    nextIndex = boundaries[nextIndex];
                }
                
        } else {
                a -= 0.5;
                b += 0.5;

                originalIndex = ((ii + 2) * numCellsY) + jj + 2;
                cells[(C_SIZE * originalIndex) + Cjy] += pCharge * pVy * b * d;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjy] += pCharge * pVy * b * d;
                    nextIndex = boundaries[nextIndex];
                }
                
                originalIndex = ((ii + 2) * numCellsY) + jj + 1 + 2;
                cells[(C_SIZE * originalIndex) + Cjy] += pCharge * pVy * b * c;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjy] += pCharge * pVy * b * c;
                    nextIndex = boundaries[nextIndex];
                }
                
                originalIndex = ((ii + 1 + 2) * numCellsY) + jj + 1 + 2;
                cells[(C_SIZE * originalIndex) + Cjy] += pCharge * pVy * a * c;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjy] += pCharge * pVy * a * c;
                    nextIndex = boundaries[nextIndex];
                }
                
                originalIndex = ((ii + 1 + 2) * numCellsY) + jj + 2;
                cells[(C_SIZE * originalIndex) + Cjy] += pCharge * pVy * a * d;
                nextIndex = boundaries[originalIndex];
                while(nextIndex != originalIndex){
                    cells[(C_SIZE * nextIndex) + Cjy] += pCharge * pVy * a * d;
                    nextIndex = boundaries[nextIndex];
                }
                
        }
}


/*-----------------------------------------------------------------------------/
/----------------Step 5: grid.updateGrid(tstep)--------------------------------/
/-----------------------------------------------------------------------------*/
//##################################################################################################################/
__kernel void store_fields(__global double* cells,   
                                    int n,
                                    int numCellsXTotal,
                                    int numCellsYTotal)
{
        
         int i = get_global_id(0);
         if(i > 0)
              return;
         
        int h, k;
        for(h = -2; h < numCellsXTotal - 2; h++){
             for(k = -2; k < numCellsYTotal - 2; k++){
                cells[( C_SIZE * (((h + 2) * numCellsYTotal) + k + 2)) + Cbzo] =  cells[( C_SIZE * (((h + 2) * numCellsYTotal) + k + 2)) + Cbz];
             }   
        }
}

//##################################################################################################################/
__kernel void solve_for_e(__global double* cells,   
                          __global int* boundaries,
                                   double timeStep,
                                   int n,
                                   int numCellsX,
                                   int numCellsY,
                                   int numCellsXTotal,
                                   int numCellsYTotal,
                                   double cellWidth,
                                   double cellHeight)
{
        
         int i = get_global_id(0);
         if(i > 0)
              return;

         int h, k, l, m;
         int mark = 0, nextIndex, originalIndex;
         double cx, cy;
         double a, b; 
         
         for(h = 0; h <= numCellsX - 1; h++){
             for(k = 0; k <= numCellsY - 1; k++){
                 cx = (cells[( C_SIZE * (((h + 2) * numCellsYTotal) + k + 1 + 2)) + Cbz] - 
                       cells[(C_SIZE * (((h + 2) * numCellsYTotal) + k + 2)) + Cbz])/cellHeight;
                 cy = -(cells[(C_SIZE * (((h + 1 + 2) * numCellsYTotal) + k + 2)) + Cbz] - 
                        cells[(C_SIZE * (((h + 2) * numCellsYTotal) + k + 2)) + Cbz])/cellWidth;
                 
                 a = timeStep * (cx - cells[(C_SIZE * (((h + 2) * numCellsYTotal) + k + 2)) + Cjx]);
                 b = timeStep * (cy - cells[(C_SIZE * (((h + 2) * numCellsYTotal) + k + 2)) + Cjy]);
                
                 originalIndex =((h + 2) * numCellsYTotal) + k + 2;
                 cells[(C_SIZE * originalIndex) + Cex] += a;
                 nextIndex = boundaries[originalIndex];
                 while(nextIndex != originalIndex){
                     cells[(C_SIZE * nextIndex) + Cex] += a;
                     nextIndex = boundaries[nextIndex];
                 }
                 
                 originalIndex =((h + 2) * numCellsYTotal) + k + 2;
                 cells[(C_SIZE * originalIndex) + Cey] += b;
                 nextIndex = boundaries[originalIndex];
                 while(nextIndex != originalIndex){
                     cells[(C_SIZE * nextIndex) + Cey] += b;
                     nextIndex = boundaries[nextIndex];
                 }
             }
         }
        
}
    
//##################################################################################################################/
__kernel void solve_for_b(__global double* cells,   
                          __global int* boundaries,
                                   double timeStep,
                                   int n,
                                   int numCellsX,
                                   int numCellsY,
                                   int numCellsXTotal, 
                                   int numCellsYTotal,
                                   double cellWidth,
                                   double cellHeight)
{
        
         int i = get_global_id(0);
         if(i > 0)
              return;

         int h, k, l;
         int mark, nextIndex, originalIndex;
         double cz;
         
         for(h = 0; h <= numCellsX - 1; h++){
             for(k = 0; k <= numCellsY - 1; k++){
                cz = (cells[(C_SIZE * (((h + 2) * numCellsYTotal) + k + 2)) + Cey] - 
                      cells[(C_SIZE * (((h - 1 + 2) * numCellsYTotal) + k + 2)) + Cey])/cellWidth -
                     (cells[(C_SIZE * (((h + 2) * numCellsYTotal) + k + 2)) + Cex] - 
                      cells[(C_SIZE * (((h + 2) * numCellsYTotal) + k - 1 + 2)) + Cex])/cellHeight;

                 originalIndex =((h + 2) * numCellsYTotal) + k + 2;
                 cells[(C_SIZE * originalIndex) + Cbz] += (-timeStep * cz);
                 nextIndex = boundaries[originalIndex];
                 while(nextIndex != originalIndex){
                     cells[(C_SIZE * nextIndex) + Cbz] += (-timeStep * cz);
                     nextIndex = boundaries[nextIndex];
                 }
             }
         }
}
        

/*-----------------------------------------------------------------------------/
/--------Step 6: interpolation.interpolateToParticle(particles, grid)----------/
/-----------------------------------------------------------------------------*/
//##################################################################################################################/
__kernel void particle_interpolation(__global double* particles,
                                     __global double* cells,   
                                              double timeStep,
                                              int n,
                                              int particlesSize,
                                              int start,
                                              int numCellsX,
                                              int numCellsY,
                                              double cellWidth,
                                              double cellHeight)
{
         int i = get_global_id(0); 
         if(i >= n)
             return;
         i = i * P_SIZE + start * P_SIZE;
         if(i >= particlesSize * P_SIZE)
             return;

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