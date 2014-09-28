package de.htw.lcs.ml.trainer;

import java.util.Arrays;

import de.htw.lcs.math.CLFloatMatrix;
import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TargetDevice;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

public class BiasRBMTrainerOCL extends RBMTrainerOCL {

	public BiasRBMTrainerOCL(NeuralNetwork rbm) {
		super(rbm);
	}

	public BiasRBMTrainerOCL(NeuralNetwork rbmModel, float learningRate, float threashold) {
		super(rbmModel, learningRate, threashold);
	}


	/**
	 * die daten brauchen keinen bias
	 */
	@Override
	public double train(final float[][] data, final int maxEpochs, final int updateInterval)
	{
		final int numExamples = data.length;
		final int numVisible = rbm.getNumVisible();
		final CLFloatMatrix biasedData = new CLFloatMatrix(numExamples, numVisible, this.addDataBias(data)).enqueue();
		return train(biasedData, maxEpochs, updateInterval);
	}

	/**
	 * Die Daten müssen einen Bias haben
	 */
	@Override
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
		for (int i = 0; i <= maxEpochs; i++) {


			// positive phase
			super.runVisible(posHiddenProbs, data);

			// https://github.com/echen/restricted-boltzmann-machines/blob/master/rbm.py
			// kann auch nach den posAssociations berechnet werden -> starke bias filterbilder
//			posHiddenProbs.randomQuantisation();
			posHiddenProbs.putColVecOnes();

			posAssociations.mmullt(data, posHiddenProbs);


			// reconstruction
			super.runHidden(reconstructedDataProbs, posHiddenProbs);
			reconstructedDataProbs.putColVecOnes();

			// negative phase
			super.runVisible(negHiddenProbs, reconstructedDataProbs);

			// associations for both phases
			negAssociations.mmullt(reconstructedDataProbs, negHiddenProbs);

			// update weights
			gradient.sub(posAssociations, negAssociations).mul(updateFactor);
			weights.add(weights, gradient);

			// calculate error only once in an updateInterval
			if (i % updateInterval == 0) {
				currentError = calcError(data, reconstructedDataProbs);

				this.fireTrainingsEvent(new NeuralNetwork(weights.getRows(), weights.getColumns(), weights.dequeue().getElements(), true), i, currentError);

				if(currentError < this.threshold) {
					System.out.println("Breaking at loop " + i);
					break;
				}
			}
		}

		return currentError;
	}

	// hier leider kein System.copy moeglich, da data row-major ist und
	// CLFloatMatrix und FloatMatrix column-major
	protected float[] addDataBias(final float[][] data) {
		final float[] biasedData = new float[data.length * (data[0].length + 1)];
		Arrays.fill(biasedData, 0, data.length, 1);
		for (int i = 0; i < data[0].length; i++) {
			for (int j = 0; j < data.length; j++) {
				biasedData[(i + 1) * data.length + j] = data[j][i];
			}
		}
		return biasedData;
	}

	@Override
	public TrainerType getTrainerType() {
		return TrainerType.BIAS;
	}

	@Override
	public TargetDevice getTargetDevice() {
		return TargetDevice.GPU;
	}

	@Override
	public boolean isBiasedTrainer() {
		return true;
	}
}
