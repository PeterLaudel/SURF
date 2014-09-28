package de.htw.lcs.ml.trainer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.sun.org.apache.xml.internal.utils.StopParseException;

import de.htw.lcs.math.CLFloatMatrix;
import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TargetDevice;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

public class MiniBiasRBMTrainerOCL extends BiasRBMTrainerOCL {

	public MiniBiasRBMTrainerOCL(NeuralNetwork rbm) {
		super(rbm);
	}

	public MiniBiasRBMTrainerOCL(NeuralNetwork rbmModel, float learningRate, float threashold) {
		super(rbmModel, learningRate, threashold);
	}


	/**
	 * die daten brauchen keinen bias
	 */
	@Override
	public double train(final float[][] data, final int maxEpochs, final int updateInterval)
	{
		int MiniBatchSize = 1024;//2048;//4096;
		int numVisible = rbm.getNumVisible();

		if(data.length < MiniBatchSize)
			MiniBatchSize = data.length;

		// erstelle mini batches, aber nur so viele wie auch komplett gefüllt sein können
		int numMiniBatches = (int)Math.max(1, Math.floor((double)data.length / MiniBatchSize));
		CLFloatMatrix[] batchedData = new CLFloatMatrix[numMiniBatches];
		for (int i = 0; i < numMiniBatches; i++) {
			int fromRow = i * MiniBatchSize, toRow = (i+1) * MiniBatchSize;

			// besorge die Datenzeilen, füge ihnen die Bias Unit hinzug und kopiere sie auf die Grafikkarte
			batchedData[i] = new CLFloatMatrix(MiniBatchSize, numVisible, this.addDataBias(data, fromRow, toRow)).enqueue(true);
		}

		// rufe eine train methoode auf die eine liste von CLFloatMatrix verwenden kann
		return train(batchedData, MiniBatchSize, maxEpochs, updateInterval);
	}

	protected double train(final CLFloatMatrix batchData[], final int MiniBatchSize, final int maxEpochs, final int updateInterval)
	{
		final int numMiniBatches = batchData.length;
		final int numExamples = MiniBatchSize;
		final int numVisible = rbm.getNumVisible();
		final int numHidden = rbm.getNumHidden();
		final float updateFactor = this.learningRate / (MiniBatchSize * numMiniBatches);

		final CLFloatMatrix posHiddenProbs = new CLFloatMatrix(numExamples, numHidden);
		final CLFloatMatrix posAssociations = new CLFloatMatrix(numVisible, numHidden);
		final CLFloatMatrix reconstructedDataProbs = new CLFloatMatrix(numExamples, numVisible);
		final CLFloatMatrix negHiddenProbs = new CLFloatMatrix(numExamples, numHidden);
		final CLFloatMatrix negAssociations = new CLFloatMatrix(numVisible, numHidden);
		final CLFloatMatrix gradient = new CLFloatMatrix(numVisible, numHidden);

		double currentError = 0;
		long start = System.currentTimeMillis();
		for (int epoch = 0; epoch <= maxEpochs; epoch++) {
			currentError = 0;
			for (int batchIndex = 0; batchIndex < numMiniBatches; batchIndex++) {
				CLFloatMatrix batch = batchData[batchIndex];

				// positive phase
				super.runVisible(posHiddenProbs, batch);
				posHiddenProbs.putColVecOnes();

				// associations for postive phases
				posAssociations.mmullt(batch, posHiddenProbs);

				// https://github.com/echen/restricted-boltzmann-machines/blob/master/rbm.py
				// kann auch nach den posAssociations berechnet werden -> starke bias filterbilder
//				if (!(epoch % updateInterval == 0 || epoch == maxEpochs))
//					posHiddenProbs.randomQuantisation();

				// reconstruction
				super.runHidden(reconstructedDataProbs, posHiddenProbs);
				reconstructedDataProbs.putColVecOnes();

				// negative phase
				super.runVisible(negHiddenProbs, reconstructedDataProbs);

				// associations for negative phases
				negAssociations.mmullt(reconstructedDataProbs, negHiddenProbs);

				// update weights
				gradient.sub(posAssociations, negAssociations).mul(updateFactor);
				weights.add(weights, gradient);

				// berechne den Fehler
				if (epoch % updateInterval == 0 || epoch == maxEpochs) {
					currentError += calcError(batch, reconstructedDataProbs) / numMiniBatches;
				}

				CLFloatMatrix.getOCL().queue.finish();
			}

			if (epoch % updateInterval == 0) {

				// update the ui
				NeuralNetwork nn = new NeuralNetwork(weights.getRows(), weights.getColumns(), weights.dequeue().getElements(), true);
				this.fireTrainingsEvent(nn, epoch, currentError);

				// Fehler ist klein genug, beende das Training
				if(currentError < this.threshold) {
					System.out.println("Breaking at loop " + epoch);
					break;
				}
			}
		}

		return currentError;
	}

	public static void saveWeightsRaw(NeuralNetwork nn, Path file)  {
		try(ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
		    oos.writeObject(nn.getWeightMatrix2D());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Hier leider kein System.copy moeglich, da data[][] in row-major
	 * gespeichert ist und CLFloatMatrix / FloatMatrix mit column-major arbeiten
	 *
	 * @param data
	 * @param from
	 * @param to
	 * @return
	 */
	protected float[] addDataBias(final float[][] data, int from, int to) {
		int rows = to - from;
		final float[] biasedData = new float[rows * (data[0].length + 1)];
		Arrays.fill(biasedData, 0, rows, 1);
		for (int i = 0; i < data[0].length; i++) {
			for (int j = from, rowIndex = 0; j < to; j++, rowIndex++) {
				biasedData[(i + 1) * rows + rowIndex] = data[j][i];
			}
		}
		return biasedData;
	}

	@Override
	public TrainerType getTrainerType() {
		return TrainerType.MINIBIAS;
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
