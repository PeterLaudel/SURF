package de.htw.lcs.ml.trainer;

import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

public class ZeroBiasRBMTrainerOCL extends FixedBiasRBMTrainerOCL {

	public ZeroBiasRBMTrainerOCL(NeuralNetwork rbm) {
		super(rbm);
	}


	/**
	 * Setzt die Bias Werte nach jeder Trainingsphase auf 0 zurück.
	 */
	@Override
	public double train(final float[][] data, final int maxEpochs, final int updateInterval)
	{
		// analyisere die daten und setzte die festen bias werte
		float[] fixedBiasWeights = new float[rbm.getNumVisible()];
		super.setFixedBiasWeights(fixedBiasWeights);

		// führe das Training der FixedBiasRBMTrainerOCL durch
		return super.train(data, maxEpochs, updateInterval);
	}

	@Override
	public TrainerType getTrainerType() {
		return TrainerType.ZEROBIAS;
	}
}
