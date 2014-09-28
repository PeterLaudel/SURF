package de.htw.lcs.ml;

import org.jblas.FloatMatrix;

public class NeuralNetwork {

	final private float[] weights;
	final private int numVisible;
	final private int numHidden;
	final private boolean withBias;


	final private float[] PersistentVisibleUnits;
	final private float[] B;
	final private float[] C;
	final private float[] GradW;
	final private float[] GradB;
	final private float[] GradC;


	/**
	 * Speichert die Gewichte
	 *
	 * @param numVisible
	 * @param numHidden
	 * @param weights
	 * @param withBias
	 */
	public NeuralNetwork(final int numVisible, final int numHidden, final float[] weights, final boolean withBias) {
		this.weights = weights;
		this.numVisible = numVisible;
		this.numHidden = numHidden;
		this.withBias = withBias;

		this.PersistentVisibleUnits = new float[weights.length];
		this.B = new float[numVisible];
		this.C = new float[numHidden];
		this.GradW = new float[weights.length];
		this.GradB = new float[numVisible];
		this.GradC = new float[numHidden];
	}

	/**
	 * Column-Major
	 *
	 * @return
	 */
	public float[] getWeights() {
		return weights;
	}

	public FloatMatrix getWeightMatrix() {
		return new FloatMatrix(numVisible, numHidden, weights);
	}


	public float[][] getWeightMatrix2D() {
		return  new FloatMatrix(numVisible, numHidden, weights).toArray2();
	}

	public int getNumVisible() {
		return numVisible;
	}

	public int getNumHidden() {
		return numHidden;
	}

	public boolean isWithBias() {
		return withBias;
	}



	public float[] getPersistentVisibleUnits() {
		return PersistentVisibleUnits;
	}

	public float[] getB() {
		return B;
	}

	public float[] getC() {
		return C;
	}

	public float[] getGradW() {
		return GradW;
	}

	public float[] getGradB() {
		return GradB;
	}

	public float[] getGradC() {
		return GradC;
	}

	public NeuralNetwork biased() {
		if(withBias)
			return this;
		else {
			int numVis = (numVisible+1);
			int numHid = (numHidden+1);
			float[] w = new float[numVis * numHid];
			for (int i = 0; i < numHidden; i++)
				System.arraycopy(weights, i * numVisible, w, (i+1) * numVis + 1, numVisible);
			return new NeuralNetwork(numVis, numHid, w, true);
		}
	}

	public NeuralNetwork unbiased() {
		if(!withBias)
			return this;
		else {
			int numVis = (numVisible-1);
			int numHid = (numHidden-1);
			float[] w = new float[numVis * numHid];
			for (int i = 1; i < numHidden; i++)
				System.arraycopy(weights, i * numVisible + 1, w, (i-1) * numVis, numVis);
			return new NeuralNetwork(numVis, numHid, w, false);
		}
	}

}
