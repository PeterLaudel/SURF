package de.htw.lcs.ml.trainer;

import de.htw.lcs.math.CLFloatMatrix;
import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TargetDevice;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

public class RBMTrainerOCL extends RBMTrainer {

	protected CLFloatMatrix weights;

	public RBMTrainerOCL(NeuralNetwork rbm) {
		this(rbm, DEFAULT_LEARNING_RATE, DEFAULT_THRESHOLD);
	}

	public RBMTrainerOCL(NeuralNetwork rbm, final float learningRate, final float threshold) {
		super(rbm, learningRate, threshold);
		weights = new CLFloatMatrix(this.rbm.getNumVisible(), this.rbm.getNumHidden(), this.rbm.getWeights()).enqueue();
	}

	public double train(final float[][] data, final int maxEpochs, final int updateInterval)
	{
		final int numExamples = data.length;
		final int numVisible = rbm.getNumVisible();
		CLFloatMatrix dataMatrix = new CLFloatMatrix(numExamples, numVisible, this.to1D(data)).enqueue();
		return train(dataMatrix, maxEpochs, updateInterval);
	}

	protected double train(CLFloatMatrix data, final int maxEpochs, final int updateInterval)
	{
		final int numExamples = data.getRows();
		final int numVisible = rbm.getNumVisible();
		final int numHidden = rbm.getNumHidden();
		final float updateFactor = this.learningRate / data.getRows();

		final CLFloatMatrix posHiddenProbs = new CLFloatMatrix(numExamples, numHidden);
		final CLFloatMatrix posAssociations = new CLFloatMatrix(numVisible, numHidden);
		final CLFloatMatrix reconstructedDataProbs = new CLFloatMatrix(numExamples, numVisible);
		final CLFloatMatrix negHiddenProbs = new CLFloatMatrix(numExamples, numHidden);
		final CLFloatMatrix negAssociations = new CLFloatMatrix(numVisible, numHidden);
		final CLFloatMatrix gradient = new CLFloatMatrix(numVisible, numHidden);

		double currentError = 0;
		for (int i = 1; i <= maxEpochs; i++) {

			// positive phase
			runVisible(posHiddenProbs, data);

			// reconstruction
			runHidden(reconstructedDataProbs, posHiddenProbs);

			// negative phase
			runVisible(negHiddenProbs, reconstructedDataProbs);

			// associations for both phases
			posAssociations.mmullt(data, posHiddenProbs);
			negAssociations.mmullt(reconstructedDataProbs, negHiddenProbs);

			// update weights
			gradient.sub(posAssociations, negAssociations).mul(updateFactor);
			weights.add(weights, gradient);

			// calculate error only once in an updateInterval
			if (i % updateInterval == 0) {
				currentError = calcError(data, reconstructedDataProbs);

				this.fireTrainingsEvent(new NeuralNetwork(weights.getRows(), weights.getColumns(), weights.dequeue().getElements(), false), i, currentError);

				if(currentError < this.threshold) {
					System.out.println("Breaking at loop " + i);
					break;
				}
			}
		}

		return currentError;
	}


	public double calcError(CLFloatMatrix data) {

		final int numExamples = data.getRows();
		final int numVisible = rbm.getNumVisible();
		final int numHidden = rbm.getNumHidden();

		final CLFloatMatrix posHiddenProbs = new CLFloatMatrix(numExamples, numHidden);
		final CLFloatMatrix reconstructedDataProbs = new CLFloatMatrix(numExamples, numVisible);

		// positive phase
		runVisible(posHiddenProbs, data);

		// reconstruction
		runHidden(reconstructedDataProbs, posHiddenProbs);

		// calculate error
		return calcError(data, reconstructedDataProbs);
	}

	/**
	 * TODO: Fehlerberechnung nur für MNIST Set
	 *
	 * manipuliert reconstruction
	 *
	 * @param data
	 * @param reconstruction
	 * @return
	 */
	public double calcError(final CLFloatMatrix data, final CLFloatMatrix reconstruction) {
		final int numExamples = data.getRows();
		final int numVisible = rbm.getNumVisible();
		return (float)Math.sqrt(reconstruction.sub(data, reconstruction).sqr().sum() / (numExamples * numVisible)) * 255;
	}

	/**
	 * TODO muss auch kompatibel mit float[][] sein
	 *
	 * @param posHiddenProbs
	 */
	public void runVisible(final CLFloatMatrix posHiddenProbs, final CLFloatMatrix data) {
		posHiddenProbs.mmul(data, weights).sigfunc();
	}

	public void runHidden(final CLFloatMatrix reconstructedDataProbs, final CLFloatMatrix hiddenProbs) {
		reconstructedDataProbs.mmulrt(hiddenProbs, weights).sigfunc();
	}

	// hier leider kein System.copy moeglich, da data row-major ist und
	// CLFloatMatrix und FloatMatrix column-major
	private float[] to1D(final float[][] data) {
		final float[] data1D = new float[data.length * data[0].length];
		for (int i = 0; i < data[0].length; i++) {
			for (int j = 0; j < data.length; j++) {
				data1D[i * data.length + j] = data[j][i];
			}
		}
		return data1D;
	}

	@Override
	public TrainerType getTrainerType() {
		return TrainerType.NORMAL;
	}

	@Override
	public TargetDevice getTargetDevice() {
		return TargetDevice.GPU;
	}

	@Override
	public boolean isBiasedTrainer() {
		return false;
	}
}
