package org.openpixi.pixi.ui.util.projection;

/**
 * Simple 3D matrix class.
 * Avoid overhead of lists.
 *
 */
public class Matrix3D {

	public double m11, m12, m13;
	public double m21, m22, m23;
	public double m31, m32, m33;

	public Matrix3D() {
	}

	public void reset() {
		m11 = 0;
		m12 = 0;
		m13 = 0;
		m21 = 0;
		m22 = 0;
		m23 = 0;
		m31 = 0;
		m32 = 0;
		m33 = 0;
	}

	public Matrix3D multiply(Matrix3D B) {
		Matrix3D A = this;
		Matrix3D C = new Matrix3D();
		C.m11 = A.m11 * B.m11 + A.m12 * B.m21 + A.m13 * B.m31;
		C.m12 = A.m11 * B.m12 + A.m12 * B.m22 + A.m13 * B.m32;
		C.m13 = A.m11 * B.m13 + A.m12 * B.m23 + A.m13 * B.m33;
		C.m21 = A.m21 * B.m11 + A.m22 * B.m21 + A.m23 * B.m31;
		C.m22 = A.m21 * B.m12 + A.m22 * B.m22 + A.m23 * B.m32;
		C.m23 = A.m21 * B.m13 + A.m22 * B.m23 + A.m23 * B.m33;
		C.m31 = A.m31 * B.m11 + A.m32 * B.m21 + A.m33 * B.m31;
		C.m32 = A.m31 * B.m12 + A.m32 * B.m22 + A.m33 * B.m32;
		C.m33 = A.m31 * B.m13 + A.m32 * B.m23 + A.m33 * B.m33;
		return C;
	}

	public static Matrix3D getRotationMatrixZ(double angle) {
		Matrix3D m = new Matrix3D();
		m.reset();
		m.m11 = Math.cos(angle);
		m.m12 = -Math.sin(angle);
		m.m21 = -m.m12; // +sin(phi)
		m.m22 = m.m11; // cos(phi)
		m.m33 = 1;
		return m;
	}

	public static Matrix3D getRotationMatrixY(double angle) {
		Matrix3D m = new Matrix3D();
		m.reset();
		m.m11 = Math.cos(angle);
		m.m13 = Math.sin(angle);
		m.m22 = 1;
		m.m31 = -m.m13; // -sin(phi)
		m.m33 = m.m11; // cos(phi)
		return m;
	}

	public static Matrix3D getRotationMatrixX(double angle) {
		Matrix3D m = new Matrix3D();
		m.reset();
		m.m11 = 1;
		m.m22 = Math.cos(angle);
		m.m23 = -Math.sin(angle);
		m.m32 = -m.m23; // +sin(phi)
		m.m33 = m.m22; // cos(phi)
		return m;
	}
}
