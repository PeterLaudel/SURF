package de.htw.lcs.ml.trainer;

import de.htw.lcs.ml.ActivationFunctions;
import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

public class MeanBiasRBMTrainerOCL extends FixedBiasRBMTrainerOCL {

	public MeanBiasRBMTrainerOCL(NeuralNetwork rbm) {
		super(rbm);
	}

	/**
	 * Setzt die Bias Werte nach jeder Trainingsphase auf den Durchschnittswert der Trainingsdaten zurück.
	 *
	 */
	@Override
	public double train(final float[][] data, final int maxEpochs, final int updateInterval)
	{
		float[] fixedBiasWeights = new float[rbm.getNumVisible()];

		// analyisere die daten und setzte die festen bias werte
		// zusammen addieren aller Daten
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[i].length; j++)
				fixedBiasWeights[j+1] += data[i][j];

		// durchschnittswerte
		for (int j = 1; j < fixedBiasWeights.length; j++) {
			float avg = fixedBiasWeights[j] / data.length;

			// durchschnitt von den Eingangsbilder abziehen
//			for (int i = 0; i < data.length; i++)
//				data[i][j-1] += 0.5 - avg;

			fixedBiasWeights[j] = ActivationFunctions.logit(avg);
		}

		// speichern
		super.setFixedBiasWeights(fixedBiasWeights);

		// führe das Training der FixedBiasRBMTrainerOCL durch
		return super.train(data, maxEpochs, updateInterval);
	}

	@Override
	public TrainerType getTrainerType() {
		return TrainerType.MEANBIAS;
	}
}
