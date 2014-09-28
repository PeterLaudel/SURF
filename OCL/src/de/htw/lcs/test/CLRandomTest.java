package de.htw.lcs.test;

import java.util.Random;

import de.htw.lcs.cl.OCL;
import de.htw.lcs.math.CLFloatMatrix;


public class CLRandomTest implements Runnable{

	public static void main(String[] args) throws Exception {
		try(final OCL ocl = new OCL()) {
			// set up CLFloatMatrix to work
			CLFloatMatrix.setUpOCL(ocl);
			new CLRandomTest().run();
		}
	}

	private static final int MDIM1 = 32;
	private static final int MDIM2 = 32;

	@Override
	public void run() {
		System.out.println("Running CLFloatMatrixFunctionTest:");
		//Random rand = new Random(8);
		Random rand = new Random();

		final CLFloatMatrix a = CLFloatMatrix.rand(MDIM1, MDIM2, 1.0f, rand).enqueue();
		a.print();

		// http://developer.amd.com/resources/documentation-articles/videos/ati-stream-opencl-technical-overview-video-series/
		System.out.println("vorher--------------------------------------------");
//		a.hardQuantisation(0.5f);
		a.randomQuantisation();
//		a.rand(rand.nextInt(), rand.nextInt());
//		a.rand(7, 11);

		a.dequeue();
		a.print();
	}

}
