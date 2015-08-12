package org.openpixi.pixi.math;

/**
 * Factory class to generate static SU(n) algebra and group elements.
 */
public class ElementFactory {

    public ElementFactory() {}

    double[] SU3GroupZero = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    double[] SU3GroupIdentity = new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    double[] SU3AlgebraZero = new double[]{0, 0, 0, 0, 0, 0, 0, 0};

    public GroupElement groupZero(int colors) {
        switch (colors) {
            case 2:
                return new SU2GroupElement(0, 0, 0, 0);
            case 3:
                return new SU3GroupElement(SU3GroupZero);
            default: System.out.println("Constructor for SU(" + colors + ") not defined.\n");
                return new SU2GroupElement(0, 0, 0, 0);
        }
    }

    public GroupElement groupIdentity(int colors) {
        switch (colors) {
            case 2:
                return new SU2GroupElement(1, 0, 0, 0);
            case 3:
                return new SU3GroupElement(SU3GroupIdentity);
            default: System.out.println("Constructor for SU(" + colors + ") not defined.\n");
                return new SU2GroupElement(1, 0, 0, 0);
        }
    }

    public AlgebraElement algebraZero(int colors) {
        switch (colors) {
            case 2:
                return new SU2AlgebraElement(0, 0, 0);
            case 3:
                return new SU3AlgebraElement(SU3AlgebraZero);
            default: System.out.println("Constructor for SU(" + colors + ") not defined.\n");
                return new SU2AlgebraElement(0, 0, 0);
        }
    }
}