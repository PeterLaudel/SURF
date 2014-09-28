package de.htw.lcs.test;

import de.htw.lcs.cl.OCL;
import de.htw.lcs.math.CLFloatMatrix;


public class CLFloatMatrixFunctionTest implements Runnable{

	public static void main(String[] args) throws Exception {
		try(final OCL ocl = new OCL()) {
			// set up CLFloatMatrix to work
			CLFloatMatrix.setUpOCL(ocl);
			new CLFloatMatrixFunctionTest().run();
		}
	}

	private static final int MDIM1 = 32;
	private static final int MDIM2 = 2;

	@Override
	public void run() {
		System.out.println("Running CLFloatMatrixFunctionTest:");

		// Matrix-Multiplication tests expect matrices to match this
		// pattern of dimensions: a.rows = b.columns
		// don't forget to enqueue
		final CLFloatMatrix a = CLFloatMatrix.ones(MDIM1, MDIM2).enqueue();
		final CLFloatMatrix b = CLFloatMatrix.ones(MDIM2, MDIM1).enqueue();

		this.mmulFunctionTest(a, b);
		this.mmulrtFunctionTest(a);
		this.mmulltFunctionTest(a);
		this.naivMmulFunctionTest(a, b);
		this.rowsGlobalColumnsLokalElementWiseMmulFunctionTest(a, b);
		this.rowsGlobalColumnsLokalRowWiseMmulFunctionTest(a, b);
		this.rowsPrivateColumnsLokalRowWiseMmulFunctionTest(a, b);
		this.twoStageSumFunctionTest(a);
		this.multiStageSumFunctionTest(a);
		this.transposeFunctionTest(a);
		this.sigFuncFunctionTest();
		this.putColVecOnesFunctionTest();
		this.addFunctionTest();
		this.subFunctionTest();
		this.mulFunctionTest();
		this.sqrFunctionTest();

		System.out.println();
	}

	private void mmulFunctionTest(final CLFloatMatrix a, final CLFloatMatrix b) {
		final CLFloatMatrix c = CLFloatMatrix.zeros(a.rows, b.columns);
		final CLFloatMatrix d = CLFloatMatrix.zeros(b.rows, a.columns);

		c.mmul(a, b).dequeue();
		d.mmul(b, a).dequeue();

		for (float f : c.getElements()) {
			assert(f == a.columns);
		}
		for (float f : d.getElements()) {
			assert(f == a.rows);
		}
		System.out.println("	MmulFunctionTest		passed");
	}

	private void mmulrtFunctionTest(final CLFloatMatrix a) {
		final CLFloatMatrix c = CLFloatMatrix.zeros(a.rows, a.rows);

		c.mmulrt(a, a).dequeue();

		for (float f : c.getElements()) {
			assert(f == a.columns);
		}
		System.out.println("	MmulrtFunctionTest		passed");
	}

	private void mmulltFunctionTest(final CLFloatMatrix a) {
		final CLFloatMatrix c = CLFloatMatrix.zeros(a.columns, a.columns);

		c.mmullt(a, a).dequeue();

		for (float f : c.getElements()) {
			assert(f == a.rows);
		}
		System.out.println("	MmulltFunctionTest		passed");
	}

	private void naivMmulFunctionTest(final CLFloatMatrix a, final CLFloatMatrix b) {
		final CLFloatMatrix c = CLFloatMatrix.zeros(a.rows, b.columns);
		final CLFloatMatrix d = CLFloatMatrix.zeros(b.rows, a.columns);

		c.naivMmul(a, b).dequeue();
		d.naivMmul(b, a).dequeue();

		for (float f : c.getElements()) {
			assert(f == a.columns);
		}
		for (float f : d.getElements()) {
			assert(f == a.rows);
		}
		System.out.println("	NaivMmulFunctionTest		passed");
	}

	private void rowsGlobalColumnsLokalElementWiseMmulFunctionTest(final CLFloatMatrix a, final CLFloatMatrix b) {
		final CLFloatMatrix c = CLFloatMatrix.zeros(a.rows, b.columns);
		final CLFloatMatrix d = CLFloatMatrix.zeros(b.rows, a.columns);

		c.rowsGlobalColumnsLokalElementWiseMmul(a, b).dequeue();
		d.rowsGlobalColumnsLokalElementWiseMmul(b, a).dequeue();

		for (float f : c.getElements()) {
			assert(f == a.columns);
		}
		for (float f : d.getElements()) {
			assert(f == a.rows);
		}
		System.out.println("	RGCLEMmulFunctionTest		passed");
	}

	private void rowsGlobalColumnsLokalRowWiseMmulFunctionTest(final CLFloatMatrix a, final CLFloatMatrix b) {
		final CLFloatMatrix c = CLFloatMatrix.zeros(a.rows, b.columns);
		final CLFloatMatrix d = CLFloatMatrix.zeros(b.rows, a.columns);

		c.rowsGlobalColumnsLokalRowWiseMmul(a, b).dequeue();
		d.rowsGlobalColumnsLokalRowWiseMmul(b, a).dequeue();

		for (float f : c.getElements()) {
			assert(f == a.columns);
		}
		for (float f : d.getElements()) {
			assert(f == a.rows);
		}
		System.out.println("	RGCLRMmulFunctionTest		passed");
	}

	private void rowsPrivateColumnsLokalRowWiseMmulFunctionTest(final CLFloatMatrix a, final CLFloatMatrix b) {
		final CLFloatMatrix c = CLFloatMatrix.zeros(a.rows, b.columns);
		final CLFloatMatrix d = CLFloatMatrix.zeros(b.rows, a.columns);

		c.rowsPrivateColumnsLokalRowWiseMmul(a, b).dequeue();
		d.rowsPrivateColumnsLokalRowWiseMmul(b, a).dequeue();

		for (float f : c.getElements()) {
			assert(f == a.columns);
		}
		for (float f : d.getElements()) {
			assert(f == a.rows);
		}
		System.out.println("	RPCLRMmulFunctionTest		passed");
	}

	private void twoStageSumFunctionTest(final CLFloatMatrix a) {
		assert(a.twoStageSum() == a.rows * a.columns);
		System.out.println("	TwoStageSumFunctionTest		passed");
	}

	private void multiStageSumFunctionTest(final CLFloatMatrix a) {
		assert(a.multiStageSum() == a.rows * a.columns);
		System.out.println("	MultiStageSumFunctionTest	passed");
	}

	// You cannot use this like a.transpose(a); even for square matrices;
	private void transposeFunctionTest(final CLFloatMatrix a) {
		final CLFloatMatrix b = CLFloatMatrix.zeros(a.columns, a.rows);
		b.transpose(a).dequeue();

		for (float f : b.getElements()) {
			assert(f == 1.0f);
		}
		System.out.println("	TransposeFunctionTest		passed");
	}

	private void sigFuncFunctionTest() {
		final int mdim = 2; // make sure mdim % CLFloatMatrix.ocl.getBlockSize() != 0

		final CLFloatMatrix acl = CLFloatMatrix.zeros(mdim, mdim).enqueue();

		acl.putColVecOnes().sigfunc().dequeue();

		assert(Math.abs(acl.get(0, 0) - 0.7310586) < 0.001);
		assert(Math.abs(acl.get(1, 0) - 0.7310586) < 0.001);
		assert(acl.get(0, 1) == 0.5);
		assert(acl.get(1, 1) == 0.5);

		float[] aclElements = new float[acl.getCLBuffer().getCLCapacity()];
		acl.getCLBuffer().getBuffer().get(aclElements);

		// only for CLFloatMatrix in column-major order
		assert(aclElements[mdim] == 0);
		assert(aclElements[mdim * acl.getClRows()] == 0);
		System.out.println("	SigFuncFunctionTest		passed");
	}

	private void putColVecOnesFunctionTest() {
		final int mdim = 2; // make sure mdim % CLFloatMatrix.ocl.getBlockSize() != 0

		final CLFloatMatrix acl = CLFloatMatrix.ones(mdim, mdim).enqueue();

		acl.mul(2).putColVecOnes().dequeue();

		assert(acl.get(0, 0) == 1);
		assert(acl.get(1, 0) == 1);
		assert(acl.get(0, 1) == 2);
		assert(acl.get(1, 1) == 2);

		final float[] aclElements = new float[acl.getCLBuffer().getCLCapacity()];
		acl.getCLBuffer().getBuffer().get(aclElements);

		// only for CLFloatMatrix in column-major order
		assert(aclElements[mdim] == 0);
		assert(aclElements[mdim * acl.getClRows()] == 0);
		System.out.println("	PutColVecOnesFunctionTests	passed");
	}

	private void addFunctionTest() {
		final int mdim = 2;

		final CLFloatMatrix acl = CLFloatMatrix.ones(mdim, mdim).enqueue();
		final CLFloatMatrix bcl = CLFloatMatrix.ones(mdim, mdim).enqueue();
		final CLFloatMatrix ccl = CLFloatMatrix.zeros(mdim, mdim);

		ccl.add(acl, bcl).dequeue();

		for (float f : ccl.getElements()) {
			assert(f == 2);
		}
		System.out.println("	AddFunctionTest			passed");
	}

	private void subFunctionTest() {
		final int mdim = 2;

		final CLFloatMatrix acl = CLFloatMatrix.ones(mdim, mdim).enqueue();
		final CLFloatMatrix bcl = CLFloatMatrix.ones(mdim, mdim).enqueue();
		final CLFloatMatrix ccl = CLFloatMatrix.ones(mdim, mdim).enqueue();

		ccl.sub(acl, bcl).dequeue();

		for (float f : ccl.getElements()) {
			assert(f == 0.0);
		}
		System.out.println("	SubFunctionTest			passed");
	}

	private void mulFunctionTest() {
		final int mdim = 2;
		final float val = 4.5f;

		final CLFloatMatrix acl = CLFloatMatrix.ones(mdim, mdim).enqueue();

		acl.mul(val).dequeue();

		for (float f : acl.getElements()) {
			assert(f == val);
		}
		System.out.println("	MulFunctionTest			passed");
	}

	private void sqrFunctionTest() {
		final int mdim = 2;
		final float val = 4.5f;
		final float sqr = val * val;

		final CLFloatMatrix acl = CLFloatMatrix.ones(mdim, mdim).enqueue();

		acl.mul(val).sqr().dequeue();

		for (float f : acl.getElements()) {
			assert(f == sqr);
		}
		System.out.println("	SqrFunctionTest			passed");
	}
}
