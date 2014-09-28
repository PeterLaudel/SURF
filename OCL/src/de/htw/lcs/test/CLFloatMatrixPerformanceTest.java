package de.htw.lcs.test;

import java.nio.FloatBuffer;
import java.util.Arrays;

import com.jogamp.opencl.CLBuffer;

import de.htw.lcs.cl.OCL;
import de.htw.lcs.math.CLFloatMatrix;


public class CLFloatMatrixPerformanceTest{

	public static void main(String[] args) throws Exception {

		for (int i = 0; i < OVERALL_RUNS; i++) {
			long start = System.currentTimeMillis();
			try(final OCL ocl = new OCL()) {
				// set up CLFloatMatrix to work
				CLFloatMatrix.setUpOCL(ocl);
				CLFloatMatrixPerformanceTest test = new CLFloatMatrixPerformanceTest();
				test.run();
			}
			System.out.println("Finished run after "+(System.currentTimeMillis()-start)+"ms");
		}
	}

	private static final int OVERALL_RUNS   = 30;
	private static final int MIN_MATRIX_DIM = 32;
	private static final int MAX_MATRIX_DIM = 12288; // 4096 2024 12288
	private static final int DEFAULT_EPOCHS = 1; //1000


	public void run() {
		System.out.printf("Running CLFloatMatrixPerformanceTest with %d epochs\n", DEFAULT_EPOCHS);

//		this.readPerformanceTest();
//		this.writePerformanceTest();

		this.memoryMmmulPerformanceTest();
//		this.blockMmmulPerformanceTest();
//		this.naivMmmulPerformanceTest();
//		this.rowsGlobalColumnsLokalElementWiseMmulPerformanceTest();
//		this.rowsGlobalColumnsLokalRowWiseMmulPerformanceTest();
//		this.rowsPrivateColumnsLokalRowWiseMmulPerformanceTest();
//		this.twoStageSumPerformanceTest();
//		this.multiStageSumPerformanceTest();
//		this.transposePerformanceTest();
//		this.sigFuncPerformanceTest();
//		this.putColVecOnesPerformanceTest();
//		this.addPerformanceTest();
//		this.subPerformanceTest();
//		this.mulPerformanceTest();
//		this.sqrPerformanceTest();
//		this.readWritePerformanceTest();
//		this.writePerformanceTest();
//		this.readPerformanceTest();
//		this.overheadPerformanceTest();

		System.out.println();
	}

	private void memoryMmmulPerformanceTest() {
		System.out.println("	memoryMmmulPerformanceTest:");

		float[] data = new float[MAX_MATRIX_DIM*MAX_MATRIX_DIM];
		int mmulMatrixSize = 1000;
		Arrays.fill(data, 1);

		for (int i = 4; i < 13; i++) {
			long preptime = System.currentTimeMillis();
			int MiniBatchSize = (int)Math.pow(2, i);
			int numMiniBatches = (int)Math.floor((double)(data.length/MAX_MATRIX_DIM) / MiniBatchSize);
			CLFloatMatrix[] batchedData = new CLFloatMatrix[numMiniBatches];
			CLFloatMatrix b = CLFloatMatrix.ones(MAX_MATRIX_DIM, mmulMatrixSize).enqueue();
			CLFloatMatrix c = CLFloatMatrix.ones(MiniBatchSize, mmulMatrixSize).enqueue();

			// besorge die Datenzeilen und kopiere sie auf die Grafikkarte
			for (int miniBatchIndex = 0; miniBatchIndex < numMiniBatches; miniBatchIndex++) {
				int fromRow = miniBatchIndex * MiniBatchSize, toRow = (miniBatchIndex+1) * MiniBatchSize;
				float[] buff = Arrays.copyOfRange(data, fromRow*MAX_MATRIX_DIM, toRow*MAX_MATRIX_DIM);
				batchedData[miniBatchIndex] = new CLFloatMatrix(MiniBatchSize, MAX_MATRIX_DIM, buff).enqueue();
			}
			preptime = System.currentTimeMillis() - preptime;

			long time = System.currentTimeMillis();
			for (int e = 0; e < DEFAULT_EPOCHS; e++) {
				for (int batchIndex = 0; batchIndex < batchedData.length; batchIndex++) {
					CLFloatMatrix a = batchedData[batchIndex];
					c.mmul(a, b);
				}
			}
			CLFloatMatrix.getOCL().queue.finish();
			time = System.currentTimeMillis() - time;
			System.out.printf("	BatchCount %4d (%4dx%4d mmul %4dx%4d)	%7dms (avg: %7dms) prep: %7dms\n", numMiniBatches, MiniBatchSize, MAX_MATRIX_DIM, MAX_MATRIX_DIM, mmulMatrixSize, time, time/DEFAULT_EPOCHS, preptime);
		}
		System.out.println();
	}

	private void blockMmmulPerformanceTest() {
		System.out.println("	BlockMmulPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix c = new CLFloatMatrix(mdim, mdim);
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue();
			final CLFloatMatrix b = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				c.mmul(a, b);
			}
			c.dequeue();
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void naivMmmulPerformanceTest() {
		System.out.println("	NaivMmulPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix c = new CLFloatMatrix(mdim, mdim);
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue();
			final CLFloatMatrix b = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				c.naivMmul(a, b);
			}
			c.dequeue();
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void rowsGlobalColumnsLokalElementWiseMmulPerformanceTest() {
		System.out.println("	RGCLEMmulPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix c = new CLFloatMatrix(mdim, mdim);
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue();
			final CLFloatMatrix b = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				c.rowsGlobalColumnsLokalElementWiseMmul(a, b);
			}
			c.dequeue();
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void rowsGlobalColumnsLokalRowWiseMmulPerformanceTest() {
		System.out.println("	RGCLRMmulPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix c = new CLFloatMatrix(mdim, mdim);
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue();
			final CLFloatMatrix b = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				c.rowsGlobalColumnsLokalRowWiseMmul(a, b);
			}
			c.dequeue();
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void rowsPrivateColumnsLokalRowWiseMmulPerformanceTest() {
		System.out.println("	RPCLRMmulPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix c = new CLFloatMatrix(mdim, mdim);
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue();
			final CLFloatMatrix b = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				c.rowsPrivateColumnsLokalRowWiseMmul(a, b);
			}
			c.dequeue();
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void twoStageSumPerformanceTest() {

		System.out.println("	TwoStageSumPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			float result = 0;
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				result = a.twoStageSumJavaCL();
			}
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms) - Result: %f\n", mdim, time, time/DEFAULT_EPOCHS, result);
		}
		System.out.println();
	}

	private void multiStageSumPerformanceTest() {
		System.out.println("	MultiStageSumPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLBuffer<FloatBuffer> b = CLFloatMatrix.zeros(mdim, mdim).clBuffer;
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				a.multiStageSum(b);
			}
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void transposePerformanceTest() {
		System.out.println("	TransposePerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix b = CLFloatMatrix.zeros(mdim, mdim);
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				b.transpose(a);
			}
			b.dequeue();
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void sigFuncPerformanceTest() {
		System.out.println("	SigmoidFunctionPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				a.sigfunc();
			}
			a.dequeue();
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void putColVecOnesPerformanceTest() {
		System.out.println("	PutColVecOnesPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				a.putColVecOnes();
			}
			a.dequeue();
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void addPerformanceTest() {
		System.out.println("	AddPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix c = new CLFloatMatrix(mdim, mdim);
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue();
			final CLFloatMatrix b = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				c.add(a, b);
			}
			c.dequeue();
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void subPerformanceTest() {
		System.out.println("	SubPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix c = new CLFloatMatrix(mdim, mdim);
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue();
			final CLFloatMatrix b = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				c.sub(a, b);
			}
			c.dequeue();
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void mulPerformanceTest() {
		System.out.println("	MulPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				a.mul2d(2);
			}
//			a.dequeue();
			a.getOCL().queue.finish();
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void sqrPerformanceTest() {
		System.out.println("	SquarePerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim).enqueue(true);

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				a.sqr();
			}
			a.dequeue();
			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void readWritePerformanceTest() {
		System.out.println("	ReadWritePerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim);
			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				a.enqueue();
				a.dequeue();
			}

			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void writePerformanceTest() {
		System.out.println("	WritePerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim);
			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				a.enqueue();
			}

			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void readPerformanceTest() {
		System.out.println("	ReadPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim);
			a.enqueue();
			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				a.dequeue();
			}

			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}

	private void overheadPerformanceTest() {
		System.out.println("	OverheadPerformanceTest:");
		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			final CLFloatMatrix a = CLFloatMatrix.ones(mdim, mdim);
			final CLFloatMatrix b = CLFloatMatrix.ones(mdim, mdim);
			final CLFloatMatrix c = CLFloatMatrix.ones(mdim, mdim); // call price is 10 = 2ms

			long time = System.currentTimeMillis();
			for (int i = 0; i < DEFAULT_EPOCHS; i++) {
				c.emptyCall(a, b); // 100 = 2ms
			}

			time = System.currentTimeMillis() - time;
			System.out.printf("	Mdim:%4d	%7dms (avg: %7dms)\n", mdim, time, time/DEFAULT_EPOCHS);
		}
		System.out.println();
	}
}
