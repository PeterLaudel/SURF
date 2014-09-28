package de.htw.lcs.ml.trainer;

import de.htw.lcs.ml.ActivationFunctions;
import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

public class UsefullBiasRBMTrainerOCL extends FixedBiasRBMTrainerOCL {

	public UsefullBiasRBMTrainerOCL(NeuralNetwork rbm) {
		super(rbm);
	}

	/**
	 * Setzt jene BiasWerte nach jeder Trainingsphase auf 0 zurück, bei dennen alle Trainingsdaten den gleichen Wert liefern.
	 */
	@Override
	public double train(final float[][] data, final int maxEpochs, final int updateInterval)
	{
		float[] fixedBiasWeights = new float[data[0].length+1];
		float[] fixedBiasWeightPos = new float[data[0].length+1];

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
			fixedBiasWeights[j] = ActivationFunctions.logit(avg);
			fixedBiasWeightPos[j] = (diff > 0) ?  0 : 1;
		}

		// speichern
		super.setFixedBiasWeights(fixedBiasWeights);
		super.setFixedBiasWeightPos(fixedBiasWeightPos);

		// führe das Training der FixedBiasRBMTrainerOCL durch
		return super.train(data, maxEpochs, updateInterval);
	}

	@Override
	public TrainerType getTrainerType() {
		return TrainerType.USEFULLBIAS;
	}
}
