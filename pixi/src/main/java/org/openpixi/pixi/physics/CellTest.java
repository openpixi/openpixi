/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.grid.Cell;

/**
 *
 * @author Petcool
 */
public class CellTest {
    static void shit(double x){
        x = 5;
        System.out.println(x);
    }
    public static void main(String[] args) {
            Cell a = new Cell();
            Cell b = new Cell();
            a = b;
            
            a.resetCurrent();
            b.resetCurrent();
            
            b.addJx(1.5);
            System.out.println(a.getJx());
            System.out.println(b.getJx());
            
            double x = 9;
            shit(x);
            System.out.println(x);
    }
}
