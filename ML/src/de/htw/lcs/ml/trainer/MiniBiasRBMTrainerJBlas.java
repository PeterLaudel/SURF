package de.htw.lcs.ml.trainer;

import java.util.Random;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TargetDevice;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

@NetworkTrainer
public class MiniBiasRBMTrainerJBlas extends RBMTrainerJBlas {


	private Random rand = new Random();

	public MiniBiasRBMTrainerJBlas(NeuralNetwork rbm) {
		this(rbm, DEFAULT_LEARNING_RATE, DEFAULT_THRESHOLD);
	}

	public static String getTrainer() {
		return "mini bias hallo";
	}

	public MiniBiasRBMTrainerJBlas(final NeuralNetwork rbm, final float learningRate, final float threshold) {
		super(rbm, learningRate, threshold);
	}

	public double train(final FloatMatrix data, final int maxEpochs, final int updateInterval)
	{
		final int numExamples = data.getRows();
		int MiniBatchSize = 32;

		final FloatMatrix columnVectorOnes = FloatMatrix.ones(numExamples, 1);
		final FloatMatrix biasedData = FloatMatrix.concatHorizontally(columnVectorOnes, data);

		// erstelle mini batches
		int numMiniBatches = (int)Math.ceil((double)numExamples / MiniBatchSize);
		FloatMatrix[] batchedData = new FloatMatrix[numMiniBatches];
		for (int i = 0; i < numMiniBatches; i++) {
			int fromRow = i * MiniBatchSize, toRow = (i+1) * MiniBatchSize;
			if(biasedData.getRows() <= toRow) toRow = biasedData.getRows();
			batchedData[i] = biasedData.getRange(fromRow, toRow, 0, biasedData.getColumns());
		}

		double error = 0;
		for (int i = 1; i <= maxEpochs; i++) {
			error = 0;
			for (int batchIndex = 0; batchIndex < numMiniBatches; batchIndex++) {
				FloatMatrix batch = batchedData[batchIndex];
//				final float updateFactor = this.learningRate / batch.getRows(); // (batch.getRows() * batch.getRows()) ist bei sparse besser
				final float updateFactor = this.learningRate / numExamples;

				// Perform reconstruction using Gibbs-sampling
				FloatMatrix reconstructedVisible = Reconstruct(batch, 1, false);

				// pre-computation that is used in gradient computation
				FloatMatrix hiddenFromData = super.runVisible(batch);
				FloatMatrix hiddenFromReconst = super.runVisible(reconstructedVisible);

				// update weights
				weights.addi(batch.transpose().mmul(hiddenFromData).subi(reconstructedVisible.transpose().mmul(hiddenFromReconst)).muli(updateFactor));

			    // sum error of all batches
				FloatMatrix reconstructedDataProbs = Reconstruct(batch, 1, false);
				double err = calcError(batch, reconstructedDataProbs);
	            error = error + (err / numMiniBatches);
			}

            // Weight Manipulation, Achtung 0te Spalte und Zeile sind Biaswerte
//			if(i % 50 == 0) {
//
////				FloatMatrix biaslessWeights = weights.subColumnVector(weights.getColumn(0));
//				int lowestVarianceColumn = findLowestVarianceColumn(weights);
////				int lowestVarianceColumn = findLowestVarianceColumn(biaslessWeights);
//
//				if(lowestVarianceColumn > 0) {
////					FloatMatrix mani = FloatMatrix.zeros(weights.getRows(), weights.getColumns());
////					mani.putColumn(1, FloatMatrix.randn(weights.getRows(), 1).muli(0.001f));
////					mani.putColumn(1, FloatMatrix.ones(weights.getRows(), 1));
////					mani.put(0, 1, 0);
////					FloatMatrix mani = FloatMatrix.ones(weights.getRows(), weights.getColumns()).subi(weights).muli(0.1f);
////					weights.addi(mani);
//					FloatMatrix mani = FloatMatrix.randn(weights.getRows(), 1).muli(0.1f);
//					mani.put(0, 0);
//					weights.putColumn(lowestVarianceColumn, mani);
//				}
//			}

			// calculate error only once in an updateInterval
			if (i % updateInterval == 0) {
				this.fireTrainingsEvent(new NeuralNetwork(weights.getRows(), weights.getColumns(), weights.data, true), i, error);

				if(error < this.threshold) {
					System.out.println("Breaking at loop " + i);
					break;
				}
			}
		}

		return error;
	}

	private int findLowestVarianceColumn(FloatMatrix mat) {

		FloatMatrix fm = columnStandardDeviation(mat);
		fm.print();
		float[] columnStd = fm.data;

		float lowest = Float.MAX_VALUE;
		int lowestIndex = -1;
		for (int i = 1; i < columnStd.length; i++) {
			float val = columnStd[i];
			if(val < lowest && val < 0.5) {
				lowest = val;
				lowestIndex = i;
			}
		}
		if(lowestIndex > 0)
			System.out.println("lowest Variance is "+lowest+" at "+lowestIndex);
		return lowestIndex;
	}

	protected static FloatMatrix columnStandardDeviation(FloatMatrix mat) {
		int num_samples = mat.getRows();
		int num_dimensions = mat.getColumns();
		float[] data = mat.data;

		float[] std = new float[num_dimensions];
		float[] avg = mat.columnMeans().data;

		// addiere alle daten pro spalte aufeinander
		int pos = 0;
		for (int j = 0; j < num_dimensions; j++) {
			float stdVal = 0;
			for (int i = 0; i < num_samples; i++, pos++) {
				float val = avg[j] - data[pos];
				stdVal += val * val;
			}
			std[j] = stdVal;
		}

		for (int i = 0; i < num_dimensions; i++)
			std[i] = (float) Math.sqrt(std[i] / (num_samples));

		return new FloatMatrix(1, num_dimensions, std);
	}

	private FloatMatrix Reconstruct(FloatMatrix data, int numSteps, boolean states) {

		// Initial value is set to input visible
		FloatMatrix reconstructedVisible = data;

		// perform Gibbs-sampling
		for (int t = 0; t < numSteps; t++) {

			// 1. sample hidden units
			FloatMatrix hiddenStates = super.runVisible(reconstructedVisible);
			if(states)	makeStates(hiddenStates);

			// 2. sample visible units
			reconstructedVisible = super.runHidden(hiddenStates);
			if(states)	makeStates(reconstructedVisible);
		}

		return reconstructedVisible;
	}

	private FloatMatrix makeStates(FloatMatrix mat) {
		float[] rawData = mat.data;
		for (int i = 0; i < rawData.length; i++)
			rawData[i] = rawData[i] > rand.nextFloat() ? 1 : 0;
		return mat;
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
		return TrainerType.MINIBIAS;
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
