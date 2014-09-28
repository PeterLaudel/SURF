package de.htw.lcs.ml.trainer;

import java.util.Arrays;
import java.util.Collections;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TargetDevice;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

public class RBMTrainerJBlas extends RBMTrainer {

	protected final FloatMatrix weights;

	public RBMTrainerJBlas(NeuralNetwork rbm) {
		this(rbm, DEFAULT_LEARNING_RATE, DEFAULT_THRESHOLD);
	}

	public RBMTrainerJBlas(final NeuralNetwork rbm, final float learningRate, final float threshold) {
		super(rbm, learningRate, threshold);
		weights = new FloatMatrix(this.rbm.getNumVisible(), this.rbm.getNumHidden(), this.rbm.getWeights());
	}

	public double train(final float[][] data, final int maxEpochs, final int updateInterval)
	{
//		Collections.shuffle(Arrays.asList(data));

		return train(new FloatMatrix(data), maxEpochs, updateInterval);
	}

	// ----------------------------------------------------------------------------------------------
	// -------------------------- Methoden die JBlas benötigen --------------------------------------
	// ----------------------------------------------------------------------------------------------
	public double train(final FloatMatrix data, final int maxEpochs, final int updateInterval)
	{
		final float updateFactor = this.learningRate / data.length;
		final FloatMatrix dataMatrixT = data.transpose();

		double currentError = 0;
		for (int i = 1; i <= maxEpochs; i++) {

			// positive phase
			final FloatMatrix posHiddenProbs = runVisible(data);

			// reconstruction
			final FloatMatrix reconstructedDataProbs = runHidden(posHiddenProbs);

			// negative phase
			final FloatMatrix negHiddenProbs = runVisible(reconstructedDataProbs);

			// associations for both phases
			final FloatMatrix posAssociations = dataMatrixT.mmul(posHiddenProbs);
			final FloatMatrix negAssociations = reconstructedDataProbs.transpose().mmul(negHiddenProbs);

			// update weights
			posAssociations.subi(negAssociations);
			posAssociations.muli(updateFactor);
			weights.addi(posAssociations);

			// calculate error only once in an updateInterval
			if (i % updateInterval == 0) {
				currentError = calcError(data, reconstructedDataProbs);
				this.fireTrainingsEvent(new NeuralNetwork(weights.rows, weights.columns, weights.data.clone(), false), i, currentError);

				if(currentError < this.threshold) {
					System.out.println("Breaking at loop " + i);
					break;
				}
			}
		}

		return currentError;
	}

	public double calcError(FloatMatrix data) {
		// positive phase
		final FloatMatrix posHiddenProbs = runVisible(data);

		// reconstruction
		final FloatMatrix reconstructedDataProbs = runHidden(posHiddenProbs);

		// calculate error only once in an updateInterval
		return calcError(data, reconstructedDataProbs);
	}

	/**
	 * TODO: Fehlerberechnung nur für MNIST Set
	 *
	 * @param data
	 * @param reconstruction
	 * @return
	 */
	public double calcError(final FloatMatrix data, final FloatMatrix reconstruction) {
		final int numExamples = data.getRows();
		final int numVisible = rbm.getNumVisible();
		final FloatMatrix difference = data.sub(reconstruction);
		return (float)Math.sqrt(MatrixFunctions.pow(difference, 2.0f).sum() / (numExamples * numVisible)) * 255;
	}

	public FloatMatrix runVisible(final FloatMatrix data) {
		final FloatMatrix posHiddenActivations = data.mmul(weights);
		return this.applySigmoidFunction(posHiddenActivations);
	}

	public FloatMatrix runHidden(final FloatMatrix hiddenProbs) {
		final FloatMatrix negVisibleActivations = hiddenProbs.mmul(weights.transpose());
		return this.applySigmoidFunction(negVisibleActivations);
	}

	public FloatMatrix applySigmoidFunction(final FloatMatrix m) {
		final float[] data = m.data;
		for (int i = 0; i < data.length; i++)
			data[i] = logistic(data[i]); // 1 / (1 + e^-x)
		return m;
	}

	public static float logistic(float val) {
		return (float) (1. / ( 1. + Math.exp(-val) ));
	}

	@Override
	public TrainerType getTrainerType() {
		return TrainerType.NORMAL;
	}

	@Override
	public TargetDevice getTargetDevice() {
		return TargetDevice.CPU;
	}

	@Override
	public boolean isBiasedTrainer() {
		return false;
	}
}
