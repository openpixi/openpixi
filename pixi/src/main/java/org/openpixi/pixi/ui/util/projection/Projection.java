package org.openpixi.pixi.ui.util.projection;

public class Projection {
	public double phi;
	public double theta;

	/** Scale factor */
	public double scale;

	/** Distance from Viewer to center */
	public double distance;

	/** Translation of original object */
	public double deltaX, deltaY, deltaZ;

	/** Translation of screen */
	public double screenDeltaX, screenDeltaY;

	/** Screen zoom factor */
	public double screenZoom;

	/** Rotation matrix */
	private Matrix3D m;

	/** 3D vector */
	private double x, y, z;

	/** Screen coordinates */
	public double screenX, screenY, screenZ;

	/** Screen scale factor */
	public double screenScale;

	/** Is object visible? */
	public boolean onscreen;

	public Projection() {
		phi = 0;
		theta = 0;
		scale = 1;
		distance = 1;
		deltaX = 0;
		deltaY = 0;
		deltaZ = 0;
		screenDeltaX = 0;
		screenDeltaY = 0;
		screenZoom = 1;
	}

	public void updateRotationMatrix() {
		Matrix3D rotationX = Matrix3D.getRotationMatrixX(theta);
		Matrix3D rotationY = Matrix3D.getRotationMatrixY(phi);
		m = rotationX.multiply(rotationY);
		scaleMatrix(scale);
	}

	public void project(double x, double y, double z) {
		multiplyMatrixVector(x + deltaX, y + deltaY, z + deltaZ);

		if (this.z + distance > 0) {
			screenScale = screenZoom * distance / (this.z + distance);
			screenX = this.x * screenScale + screenDeltaX;
			screenY = this.y * screenScale + screenDeltaY;
			screenZ = this.z; // arbitrary scale in z-direction
			onscreen = true;
		} else {
			onscreen = false;
		}
	}

	private void multiplyMatrixVector(double x, double y, double z) {
		this.x = m.m11 * x + m.m12 * y + m.m13 * z;
		this.y = m.m21 * x + m.m22 * y + m.m23 * z;
		this.z = m.m31 * x + m.m32 * y + m.m33 * z;
	}

	private void scaleMatrix(double f) {
		m.m11 *= f;
		m.m12 *= f;
		m.m13 *= f;
		m.m21 *= f;
		m.m22 *= f;
		m.m23 *= f;
		m.m31 *= f;
		m.m32 *= f;
		m.m33 *= f;
	}

}
