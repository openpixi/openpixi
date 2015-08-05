package org.openpixi.pixi.math;

/**
 * Factory class to generate static SU(n) algebra and group elements.
 */
public class ElementFactory {

    public ElementFactory() {}

    public GroupElement groupZero(int colors) {
        switch (colors) {
            case 2:
                return new SU2GroupElement(0, 0, 0, 0);
            case 3:
                return new SU3GroupElement(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
            default: System.out.println("Constructor for SU(" + colors + ") not defined.\n");
                return new SU2GroupElement(0, 0, 0, 0);
        }
    }

    public GroupElement groupIdentity(int colors) {
        switch (colors) {
            case 2:
                return new SU2GroupElement(1, 0, 0, 0);
            case 3:
                return new SU3GroupElement(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0});
            default: System.out.println("Constructor for SU(" + colors + ") not defined.\n");
                return new SU2GroupElement(1, 0, 0, 0);
        }
    }

    public AlgebraElement algebraZero(int colors) {
        switch (colors) {
            case 2:
                return new SU2AlgebraElement(0, 0, 0);
            case 3:
                return new SU3AlgebraElement(new double[]{0, 0, 0, 0, 0, 0, 0, 0});
            default: System.out.println("Constructor for SU(" + colors + ") not defined.\n");
                return new SU2AlgebraElement(0, 0, 0);
        }
    }
}