package de.htw.lcs.ml.trainer;

import de.htw.lcs.events.TrainingsEventhandler;
import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TargetDevice;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

@NetworkTrainer
public abstract class RBMTrainer extends TrainingsEventhandler {

	public static float DEFAULT_LEARNING_RATE = 0.5f;
	public static float DEFAULT_THRESHOLD = 0.001f;

	protected float learningRate;
	protected float threshold;
	protected NeuralNetwork rbm;

	/**
	 * speichert das netzwerk in seiner benötigten Form ab (biased oder unbiased)
	 *
	 * @param rbm
	 * @param learningRate
	 * @param threshold
	 */
	public RBMTrainer(final NeuralNetwork rbm, final float learningRate, final float threshold) {
		this.rbm = isBiasedTrainer() ? rbm.biased() : rbm.unbiased();
		this.learningRate = learningRate;
		this.threshold = threshold;
	}

	public abstract double train(final float[][] data, final int maxEpochs, final int updateInterval);

	public abstract TrainerType getTrainerType();
	public abstract TargetDevice getTargetDevice();
	public abstract boolean isBiasedTrainer();
}
