package de.htw.lcs.test;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

public class JBlasFloatMatrixPerformanceTest implements Runnable{

	public static void main(String[] args) throws Exception {
		new JBlasFloatMatrixPerformanceTest().run();
	}

	private static final int MIN_MATRIX_DIM = 32;
	private static final int MAX_MATRIX_DIM = 1024;
	private static final int DEFAULT_EPOCHS = 10;

	private final int epochs;

	public JBlasFloatMatrixPerformanceTest() {
		this(DEFAULT_EPOCHS);
	}

	public JBlasFloatMatrixPerformanceTest(final int epochs) {
		this.epochs = epochs;
	}

	@Override
	public void run() {
		System.out.printf("Running JBlasFloatMatrixPerformanceTest with %d epochs\n", this.epochs);
		mmulPerformanceTest();
		sumPerformanceTest();
		transposePerformanceTest();
		sigFuncPerformanceTest();
		putColVecOnesPerformanceTest();
		addPerformanceTest();
		subPerformanceTest();
		mulPerformanceTest();
		sqrPerformanceTest();
	}

	private void mmulPerformanceTest() {
		System.out.println("	MmulPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final FloatMatrix a = FloatMatrix.ones(mdim, mdim);
			final FloatMatrix b = FloatMatrix.ones(mdim, mdim);

			long time = System.currentTimeMillis();
			for (int i = 0; i < this.epochs; i++) {
				a.mmul(b);
			}
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms\n", mdim, time);
		}
		System.out.println();
	}

	private void sumPerformanceTest() {
		System.out.println("	SumPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final FloatMatrix a = FloatMatrix.ones(mdim, mdim);

			long time = System.currentTimeMillis();
			for (int i = 0; i < this.epochs; i++) {
				a.sum();
			}
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms\n", mdim, time);
		}
		System.out.println();
	}

	private void transposePerformanceTest() {
		System.out.println("	TransposePerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final FloatMatrix a = FloatMatrix.ones(mdim, mdim);

			long time = System.currentTimeMillis();
			for (int i = 0; i < this.epochs; i++) {
				a.transpose();
			}
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms\n", mdim, time);
		}
		System.out.println();
	}

	private void sigFuncPerformanceTest() {
		System.out.println("	SigmoidFunctionPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final FloatMatrix a = FloatMatrix.ones(mdim, mdim);

			long time = System.currentTimeMillis();
			for (int i = 0; i < this.epochs; i++) {
				this.applySigmoidFunction(a);
			}
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms\n", mdim, time);
		}
		System.out.println();
	}

	private void putColVecOnesPerformanceTest() {
		System.out.println("	PutColVecOnesPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final FloatMatrix a = FloatMatrix.zeros(mdim, mdim);
			final FloatMatrix colVecOnes = FloatMatrix.ones(mdim, 1);

			long time = System.currentTimeMillis();
			for (int i = 0; i < this.epochs; i++) {
				a.putColumn(0, colVecOnes);
			}
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms\n", mdim, time);
		}
		System.out.println();
	}

	private void addPerformanceTest() {
		System.out.println("	AddPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final FloatMatrix a = FloatMatrix.ones(mdim, mdim);
			final FloatMatrix b = FloatMatrix.ones(mdim, mdim);

			long time = System.currentTimeMillis();
			for (int i = 0; i < this.epochs; i++) {
				a.add(b);
			}
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms\n", mdim, time);
		}
		System.out.println();
	}

	private void subPerformanceTest() {
		System.out.println("	SubPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final FloatMatrix a = FloatMatrix.ones(mdim, mdim);
			final FloatMatrix b = FloatMatrix.ones(mdim, mdim);

			long time = System.currentTimeMillis();
			for (int i = 0; i < this.epochs; i++) {
				a.sub(b);
			}
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms\n", mdim, time);
		}
		System.out.println();
	}

	private void mulPerformanceTest() {
		System.out.println("	MulPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final FloatMatrix a = FloatMatrix.ones(mdim, mdim);

			long time = System.currentTimeMillis();
			for (int i = 0; i < this.epochs; i++) {
				a.mul(2);
			}
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms\n", mdim, time);
		}
		System.out.println();
	}

	private void sqrPerformanceTest() {
		System.out.println("	SquarePerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final FloatMatrix a = FloatMatrix.ones(mdim, mdim);

			long time = System.currentTimeMillis();
			for (int i = 0; i < this.epochs; i++) {
				MatrixFunctions.pow(a, 2);
			}
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms\n", mdim, time);
		}
		System.out.println();
	}

	private FloatMatrix applySigmoidFunction(final FloatMatrix a) {
		final float[] data = a.data;
		for (int i = 0; i < data.length; i++)
			data[i] = (float) (1. / ( 1. + Math.exp(-data[i]) )); // 1 / (1 + e^-x)
		return a;
	}
}

