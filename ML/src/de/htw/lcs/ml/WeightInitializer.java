package de.htw.lcs.ml;

import java.util.Random;

public class WeightInitializer {

	protected static long seed;
	protected static float variance;
	protected static Random rnd;

	static {
		seed = (long)(Math.random() * Long.MAX_VALUE);
		rnd = new Random(seed);
		variance = 0.1f;
	}

	public static void initSeed(long seedValue) {
		seed = seedValue;
		rnd = new Random(seedValue);
	}

	public static long getSeed() {
		return seed;
	}

	public static float getVariance() {
		return variance;
	}

	public static void setVariance(final float varianceValue) {
		variance = varianceValue;
	}

	public static Random getRandom() {
		return rnd;
	}

	public static float[] randFloats(final int size, final float varianceValue) {
		final Random random = new Random(seed);
		final float[] elements = new float[size];
		for (int i = 0; i < elements.length; i++)
			elements[i] = (float) random.nextGaussian() * varianceValue;
		return elements;
	}

	/**
	 * erzeugt Gauss Verteilte Zufallswerte mit einer Variance,
	 * die als Member Variable dieser Klasse angegeben ist.
	 *
	 * @param size
	 * @return
	 */
	public static float[] randFloats(final int size) {
		return randFloats(size, variance);
	}
}
