/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openpixi.pixi.physics.collision.util;

import java.util.ArrayList;
import org.openpixi.pixi.physics.Particle;

/**
 *
 * @author Clemens
 */


public class KdTree {
    private ParticleBoundingBoxPoint root;
    private KdTree left, right;
    
    public KdTree(ArrayList<ParticleBoundingBoxPoint> list, int dim)
    {
        //System.out.println("Listengröße: " + list.size());
        if(list.isEmpty())
        {
            root = null;
            left = null;
            right = null;
        }
        else
        {
            root = getMedian2(list,dim,3);
            
            
            ArrayList<ParticleBoundingBoxPoint> l1,l2;
            l1 = new ArrayList<ParticleBoundingBoxPoint>();
            l2 = new ArrayList<ParticleBoundingBoxPoint>();

            //again l1 contains the points less or equal to the root and l2 the bigger ones

            if(dim==0)
            {
                double median = root.getX();
                list.remove(root);
                for(ParticleBoundingBoxPoint p:list)
                {
                    if(p.getX()<=median)                  
                        l1.add(p);
                    else
                        l2.add(p);
                }
                if(l1.isEmpty())
                    this.left = null;
                else
                    this.left= new KdTree(l1,1);
                
                if(l2.isEmpty())
                    this.right = null;
                else
                    this.right = new KdTree(l2,1);
            }
            else
            {
                //dim = 1
                double median = root.getY();
                list.remove(root);
                for(int i = 0; i<list.size();i++)
                {
                    if(list.get(i).getY()<=median)                  
                        l1.add(list.get(i));
                    else
                        l2.add(list.get(i));
                }
                if(l1.isEmpty())
                    this.left = null;
                else
                    this.left= new KdTree(l1,0);
                
                if(l2.isEmpty())
                    this.right = null;
                else
                    this.right = new KdTree(l2,0);
            }
        }
        
    }
    
    public ArrayList<Particle> Search(double xmin, double xmax, double ymin, double ymax, int dim)
    {
        //dim gibt an in welcher dimension man gerade sucht
        
        ArrayList<Particle> l1 = new ArrayList<Particle>();
        ArrayList<Particle> l2 = new ArrayList<Particle>();
        ArrayList<Particle> l = new ArrayList<Particle>();
        
        if(dim==0)
        {
            if(root.getX()>=xmin)
            {
                if(left!=null)
                    l1 = left.Search(xmin, xmax, ymin, ymax, 1);
            }
            if(root.getX()<=xmax)
            {
                if(right!=null)
                    l2 = right.Search(xmin, xmax, ymin, ymax, 1);
            }
            
            if(root.getX()>xmin && root.getX()<xmax && root.getY()>ymin && root.getY()<ymax)
                l.add(root.getPar());
            
            for(Particle p: l1)
                l.add(p);
            
            for(Particle p:l2)
                l.add(p);
        }
        
        else
        {
            //dim = 1
            if(root.getY()>=ymin)
            {
                if(left!=null)
                    l1 = left.Search(xmin, xmax, ymin, ymax, 0);
            }
            if(root.getY()<=ymax)
            {
                if(right!=null)
                    l2 = right.Search(xmin, xmax, ymin, ymax, 0);
            }
            
            if(root.getX()>xmin && root.getX()<xmax && root.getY()>ymin && root.getY()<ymax)
                l.add(root.getPar());
            
            for(Particle p: l1)
                l.add(p);
            
            for(Particle p:l2)
                l.add(p);
        }
            
        return l;
    }
    
    
    ParticleBoundingBoxPoint getMedian(ArrayList<ParticleBoundingBoxPoint> list, int dim)
    {
        int median = (list.size()+1)/2;
        return getK(list,median,dim);
    }
    
    ParticleBoundingBoxPoint getMedian2(ArrayList<ParticleBoundingBoxPoint> list, int dim, double k)
    {
        if(list.size()<=k)
        {
            return getMedian(list,dim);
        }
        else
        {
            ArrayList<ParticleBoundingBoxPoint> l2 = new ArrayList<ParticleBoundingBoxPoint>();
            for(int i = 0;i<k;i++)
            {
                l2.add(list.get(i));
            }
            return getMedian(l2,dim);
        }
    }
    
    //Quickselect
    ParticleBoundingBoxPoint getK(ArrayList<ParticleBoundingBoxPoint> list, int k, int dim)
    {
        //returns the Particle with the k-th value in the required dimension
        //1<=k<=list.size()
        //dim represents the dimension which is sorted
        //0-x
        //1-y
        //(2-z)
        if(dim==0)
        {
            if(list.size()<k||k<1)
            {
                //not possible
                return null;
            }
            //take median of 3 for increase of efficiency, therefore the cases with less than 3 elements have to be covered:
            if(list.size()<3)
            {
                //sort the list and get the entry k
                if(list.size()==2)
                {
                    if(k==1)
                    {
                        if(list.get(0).getX()<=list.get(1).getX())
                            return list.get(0);
                        else
                            return list.get(1);
                    }
                    else
                    {
                        //k=1
                        if(list.get(0).getX()<=list.get(1).getX())
                            return list.get(1);
                        else
                            return list.get(0);
                    }
                }
                else
                {
                    //list.size=1
                    return list.get(0);
                }
            }
            else
            {
                //split at the median of the first 3 elements
                int index;
                double x0 = list.get(0).getX();
                double x1 = list.get(1).getX();
                double x2 = list.get(2).getX();
                if(x0>=x1)
                {
                    if(x0<=x2)
                        index = 0;
                    else
                    {
                        if(x1>= x2)
                            index=1;
                        else
                            index=2;
                    }
                }
                else
                {
                    if(x0>=x2)
                        index=0;
                    else
                    {
                        if(x1>=x2)
                            index=2;
                        else
                            index=1;
                    }
                }
                
                double median = list.get(index).getX();
                
                ArrayList<ParticleBoundingBoxPoint> l1,l2;
                l1 = new ArrayList<ParticleBoundingBoxPoint>();
                l2 = new ArrayList<ParticleBoundingBoxPoint>();
                //l1 contains the elements with x-coordinate less or equal to the median
                //l2 contains the elements with x-coortinate greater than the median
                
                //first part of the list
                for(int i=0;i<index;i++)
                {
                    if(list.get(i).getX()<=median)
                        l1.add(list.get(i));
                    else
                        l2.add(list.get(i));
                }
                
                //second part of the list
                int s = list.size();
                for(int i = index+1;i<s;i++)
                {
                    if(list.get(i).getX()<=median)
                        l1.add(list.get(i));
                    else
                        l2.add(list.get(i));
                }
                
                if(l1.size()==k-1)
                    return list.get(index);
                else if(l1.size()>k-1)
                    return getK(l1,k,dim);
                else
                    return getK(l2,k-l1.size()-1,dim);
                
            }
        }
        
        
        else if(dim==1)
        {
            
            if(list.size()<k||k<1)
            {
                //not possible
                return null;
            }
            //take median of 3 for increase of efficiency, therefore the cases with less than 3 elements have to be covered:
            if(list.size()<3)
            {
                //sort the list and get the entry k
                if(list.size()==2)
                {
                    if(k==1)
                    {
                        if(list.get(0).getY()<=list.get(1).getY())
                            return list.get(0);
                        else
                            return list.get(1);
                    }
                    else
                    {
                        //k=1
                        if(list.get(0).getY()<=list.get(1).getY())
                            return list.get(1);
                        else
                            return list.get(0);
                    }
                }
                else
                {
                    //list.size=1
                    return list.get(0);
                }
            }
            else
            {
                //split at the median of the first 3 elements
                int index;
                double y0 = list.get(0).getY();
                double y1 = list.get(1).getY();
                double y2 = list.get(2).getY();
                if(y0>=y1)
                {
                    if(y0<=y2)
                        index = 0;
                    else
                    {
                        if(y1>= y2)
                            index=1;
                        else
                            index=2;
                    }
                }
                else
                {
                    if(y0>=y2)
                        index=0;
                    else
                    {
                        if(y1>=y2)
                            index=2;
                        else
                            index=1;
                    }
                }
                
                double median = list.get(index).getX();
                
                ArrayList<ParticleBoundingBoxPoint> l1,l2;
                l1 = new ArrayList<ParticleBoundingBoxPoint>();
                l2 = new ArrayList<ParticleBoundingBoxPoint>();
                //l1 contains the elements with x-coordinate less or equal to the median
                //l2 contains the elements with x-coortinate greater than the median
                
                //first part of the list
                for(int i=0;i<index;i++)
                {
                    if(list.get(i).getY()<=median)
                        l1.add(list.get(i));
                    else
                        l2.add(list.get(i));
                }
                
                //second part of the list
                int s = list.size();
                for(int i = index+1;i<s;i++)
                {
                    if(list.get(i).getY()<=median)
                        l1.add(list.get(i));
                    else
                        l2.add(list.get(i));
                }
                
                if(l1.size()==k-1)
                    return list.get(index);
                else if(l1.size()>k-1)
                    return getK(l1,k,dim);
                else
                    return getK(l2,k-l1.size()-1,dim);
                
            }
        }
        else
            return null;
    }
    
}


