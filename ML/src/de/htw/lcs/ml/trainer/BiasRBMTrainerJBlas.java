package de.htw.lcs.ml.trainer;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TargetDevice;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

@NetworkTrainer
public class BiasRBMTrainerJBlas extends RBMTrainerJBlas {

	public BiasRBMTrainerJBlas(NeuralNetwork rbm) {
		this(rbm, DEFAULT_LEARNING_RATE, DEFAULT_THRESHOLD);
	}

	public static String getTrainer() {
		return "bias hallo";
	}

	public BiasRBMTrainerJBlas(final NeuralNetwork rbm, final float learningRate, final float threshold) {
		super(rbm, learningRate, threshold);
	}

	public double train(final FloatMatrix data, final int maxEpochs, final int updateInterval)
	{
		final int numExamples = data.getRows();
		final float updateFactor = this.learningRate / numExamples;

		final FloatMatrix columnVectorOnes = FloatMatrix.ones(numExamples, 1);
		final FloatMatrix biasedData = FloatMatrix.concatHorizontally(columnVectorOnes, data);
		final FloatMatrix biasedDataT = biasedData.transpose();

		double currentError = 0;
		for (int i = 1; i <= maxEpochs; i++) {

			// positive phase
			final FloatMatrix posHiddenProbs = super.runVisible(biasedData);
			final FloatMatrix posAssociations = biasedDataT.mmul(posHiddenProbs);
			posHiddenProbs.putColumn(0, columnVectorOnes);

			// reconstruction
			final FloatMatrix reconstructedDataProbs = super.runHidden(posHiddenProbs);
			reconstructedDataProbs.putColumn(0, columnVectorOnes);

			// negative phase
			final FloatMatrix negHiddenProbs = super.runVisible(reconstructedDataProbs);

			// associations for both phases
			final FloatMatrix negAssociations = reconstructedDataProbs.transpose().mmul(negHiddenProbs);

			// update weights
			posAssociations.subi(negAssociations);
			posAssociations.muli(updateFactor);
			weights.addi(posAssociations);

			// calculate error only once in an updateInterval
			if (i % updateInterval == 0) {
				currentError = calcError(biasedData, reconstructedDataProbs);
				this.fireTrainingsEvent(new NeuralNetwork(weights.rows, weights.columns, weights.data, true), i, currentError);

				if(currentError < this.threshold) {
					System.out.println("Breaking at loop " + i);
					break;
				}
			}
		}

		return currentError;
	}

	public FloatMatrix runVisible(final FloatMatrix data) {

		// Insert bias units of 1 into the first column of data.
		final FloatMatrix columnVectorOnes = FloatMatrix.ones(data.getRows(), 1);
		final FloatMatrix biasedData = FloatMatrix.concatHorizontally(columnVectorOnes, data);

		// Calculate the probabilities of turning the hidden units on.
		final FloatMatrix hiddenProbs = super.runVisible(biasedData);

		// Ignore the bias units.
		final FloatMatrix hiddenProbsWithoutBias = hiddenProbs.getRange(0, hiddenProbs.getRows(), 1, hiddenProbs.getColumns());

		return hiddenProbsWithoutBias;
	}

	public FloatMatrix runHidden(final FloatMatrix data) {

		// Insert bias units of 1 into the first column of data.
		final FloatMatrix columnVectorOnes = FloatMatrix.ones(data.getRows(), 1);
		final FloatMatrix biasedData = FloatMatrix.concatHorizontally(columnVectorOnes, data);

		// Calculate the probabilities of turning the visible units on.
		final FloatMatrix visibleProbs = super.runHidden(biasedData);

		// Ignore bias TODO: recht teuer wenn man eigentlich nur eine Column weglassen möchte
		final FloatMatrix visibleProbsWithoutBias = visibleProbs.getRange(0,visibleProbs.getRows(), 1, visibleProbs.getColumns());

		return visibleProbsWithoutBias;
	}

	public FloatMatrix runHiddenWithoutBias(final FloatMatrix data) {

//		final FloatMatrix weights = new FloatMatrix(rbm.getNumVisible(), rbm.getNumHidden(), rbm.getWeights());

		// Insert bias units of 1 into the first column of data.
		final FloatMatrix columnVectorOnes = FloatMatrix.zeros(data.getRows(), 1);
		final FloatMatrix biasedData = FloatMatrix.concatHorizontally(columnVectorOnes, data);

		// Calculate the activations of the visible units.
		final FloatMatrix visibleProbs = super.runHidden(biasedData);

		// Ignore bias TODO: recht teuer wenn man eigentlich nur eine Column weglassen möchte
		final FloatMatrix visibleProbsWithoutBias = visibleProbs.getRange(0,visibleProbs.getRows(), 1, visibleProbs.getColumns());

		return visibleProbsWithoutBias;
	}

	public static float logistic(float val) {
		return (float) (1. / ( 1. + Math.exp(-val) ));
	}

	@Override
	public TrainerType getTrainerType() {
		return TrainerType.BIAS;
	}

	@Override
	public TargetDevice getTargetDevice() {
		return TargetDevice.CPU;
	}

	@Override
	public boolean isBiasedTrainer() {
		return true;
	}
}
