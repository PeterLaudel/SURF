package de.htw.lcs.ml.trainer;

import de.htw.lcs.ml.ActivationFunctions;
import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

public class StaticBiasRBMTrainerOCL extends FixedBiasRBMTrainerOCL {

	public StaticBiasRBMTrainerOCL(NeuralNetwork rbm) {
		super(rbm);
	}

	/**
	 * Setzt die Bias Werte nach jeder Trainingsphase auf den Durchschnittswert der Trainingsdaten zurück.
	 * Außer wenn sich alle an der Position identisch sind, dann bekommt die Biasunit den Wert 0,5;
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
			float diff = 0;

			// berechne die Abweichung zum Durchschnitt
			for (int i = 0; i < data.length; i++)
				diff += Math.abs(avg - data[i][j-1]);

			// bei keiner Abweichung, müssen wohl alle den selben Wert haben
			fixedBiasWeights[j] = ActivationFunctions.logit((diff/data.length <= 0.3) ? avg : 0.5f);
		}

		// speichern
		super.setFixedBiasWeights(fixedBiasWeights);

		// führe das Training der FixedBiasRBMTrainerOCL durch
		return super.train(data, maxEpochs, updateInterval);
	}

	@Override
	public TrainerType getTrainerType() {
		return TrainerType.STATICBIAS;
	}
}
