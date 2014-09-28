package de.htw.lcs.test;

import java.util.Random;

import org.jblas.FloatMatrix;

import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.BiasRBMTrainerJBlas;


public class RBMTrainerJBlasTest implements Runnable {

	public static void main(String[] args) throws Exception {
		new RBMTrainerJBlasTest().run();
	}

	private static final int MIN_MATRIX_DIM = 32;
	private static final int MAX_MATRIX_DIM = 1024;
	private static final int DEFAULT_EPOCHS = 1000;
	private static final int DEFAULT_UPDATE_INTERVAL = 1000;

	private final int epochs;
	private final int updateInterval;

	public RBMTrainerJBlasTest() {
		this(DEFAULT_EPOCHS, DEFAULT_UPDATE_INTERVAL);
	}

	public RBMTrainerJBlasTest(final int epochs, final int updateInterval) {
		this.epochs = epochs;
		this.updateInterval = updateInterval;
	}

	@Override
	public void run() {
		System.out.println("Running RBMTrainerJBlasTest: with " + this.epochs + " epochs");

		final Random random = new Random();

		for (int mdim = MIN_MATRIX_DIM; mdim <= MAX_MATRIX_DIM; mdim <<= 1) {
			// MyJavaSTFloatMatrix is correct because we only need the elements array here
			final NeuralNetwork rbmModel = new NeuralNetwork(mdim, mdim, FloatMatrix.randn(mdim, mdim).data, true);

			// threshold 0.0f so the trainer doesn't stop prior to maturity
			final BiasRBMTrainerJBlas trainer = new BiasRBMTrainerJBlas(rbmModel, 0.2f, 0.0f);

			// Random data doesn't matter here because we just observe performance
			final float[][] data = new float[mdim][mdim-1];

			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[0].length; j++) {
					data[i][j] = (float) random.nextGaussian();
				}
			}

			System.out.printf("	Mdim:%4d	", mdim);
			trainer.train(data, this.epochs, this.updateInterval);
		}
		System.out.println();
	}

}