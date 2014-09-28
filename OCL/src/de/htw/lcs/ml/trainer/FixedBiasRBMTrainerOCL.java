package de.htw.lcs.ml.trainer;

import de.htw.lcs.math.CLFloatMatrix;
import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

public class FixedBiasRBMTrainerOCL extends BiasRBMTrainerOCL {

	private CLFloatMatrix fixedBiasWeightPosCL;
	private CLFloatMatrix fixedBiasWeightCL;

	public FixedBiasRBMTrainerOCL(NeuralNetwork rbm ) {
		super(rbm);
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
		for (int i = 1; i <= maxEpochs; i++) {

			// positive phase
			((RBMTrainerOCL)this).runVisible(posHiddenProbs, data);
			posAssociations.mmullt(data, posHiddenProbs);
			posHiddenProbs.putColVecOnes();

			// reconstruction
			((RBMTrainerOCL)this).runHidden(reconstructedDataProbs, posHiddenProbs);
			reconstructedDataProbs.putColVecOnes();

			// negative phase
			((RBMTrainerOCL)this).runVisible(negHiddenProbs, reconstructedDataProbs);

			// associations for both phases
			negAssociations.mmullt(reconstructedDataProbs, negHiddenProbs);

			// update weights
			gradient.sub(posAssociations, negAssociations).mul(updateFactor);
			weights.add(weights, gradient);

			if(this.fixedBiasWeightPosCL != null)
				weights.putColumnVectorAt(this.fixedBiasWeightCL, this.fixedBiasWeightPosCL);
			else if(this.fixedBiasWeightCL != null)
				weights.putColumnVector(this.fixedBiasWeightCL);

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

	public void setFixedBiasWeights(float[] fixedBiasWeights) {
		this.fixedBiasWeightCL = new CLFloatMatrix(fixedBiasWeights.length, 1, fixedBiasWeights).enqueue();
	}

	public void setFixedBiasWeightPos(float[] fixedBiasWeightPos) {
		this.fixedBiasWeightPosCL = new CLFloatMatrix(fixedBiasWeightPos.length, 1, fixedBiasWeightPos).enqueue();
	}

	@Override
	public TrainerType getTrainerType() {
		return TrainerType.FIXEDBIAS;
	}
}
