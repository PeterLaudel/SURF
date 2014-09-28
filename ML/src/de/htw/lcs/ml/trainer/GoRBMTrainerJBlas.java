package de.htw.lcs.ml.trainer;

import java.util.Arrays;
import java.util.Random;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TargetDevice;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

/**
 * Aus basis von
 * http://r9y9.github.io/blog/2014/03/06/restricted-boltzmann-machines-mnist/
 *
 * @author Nico
 *
 */
@NetworkTrainer
public class GoRBMTrainerJBlas extends RBMTrainer {

	protected final FloatMatrix weights;
	private Random rand = new Random();

	public GoRBMTrainerJBlas(NeuralNetwork rbm) {
		this(rbm, DEFAULT_LEARNING_RATE, DEFAULT_THRESHOLD);
	}

	public GoRBMTrainerJBlas(final NeuralNetwork rbm, final float learningRate, final float threshold) {
		super(rbm, learningRate, threshold);
		weights = new FloatMatrix(this.rbm.getNumVisible(), this.rbm.getNumHidden(), this.rbm.getWeights());
	}

	public double train(final float[][] data, final int maxEpochs, final int updateInterval)
	{
		return train(new FloatMatrix(data), maxEpochs, updateInterval);
	}

	// ----------------------------------------------------------------------------------------------
	// -------------------------- Methoden die JBlas benötigen --------------------------------------
	// ----------------------------------------------------------------------------------------------
	public double train(final FloatMatrix data, final int maxEpochs, final int updateInterval)
	{
		// add contant Noise
//		data.addi(1.0e-21f);

		float LearningRate = 0.1f;
		float updateFactor = LearningRate / data.getRows();
		int MiniBatchSize = 32;

		FloatMatrix B = new FloatMatrix(1, rbm.getB().length, rbm.getB());
		FloatMatrix C = new FloatMatrix(1, rbm.getC().length, rbm.getC());


		// erstelle mini batches
		int numMiniBatches = (int)Math.ceil((double)data.getRows() / MiniBatchSize);
		FloatMatrix[] batchedData = new FloatMatrix[numMiniBatches];
		for (int i = 0; i < numMiniBatches; i++) {
			int fromRow = i * MiniBatchSize, toRow = (i+1) * MiniBatchSize;
			if(data.getRows() <= toRow) toRow = data.getRows();
			batchedData[i] = data.getRange(fromRow, toRow, 0, data.getColumns());
		}

		// durchlaufe alle Epochen und Batches
		int weightChanges = 0;
		for (int epoch = 0; epoch < maxEpochs; epoch++) {
			double error = 0;
			for (int batchIndex = 0; batchIndex < numMiniBatches; batchIndex++) {
				FloatMatrix batch = batchedData[batchIndex];

				// rbm.go : Gradient

				// Perform reconstruction using Gibbs-sampling
				FloatMatrix reconstructedVisible = Reconstruct(batch, B, C, 1, true);

				// pre-computation that is used in gradient computation
				FloatMatrix hiddenFromData = runVisible(batch, C);
				FloatMatrix hiddenFromReconst = runVisible(reconstructedVisible, C);

				// compute gradient of Wbatch
				FloatMatrix gradW = batch.transpose().mmul(hiddenFromData).subi(reconstructedVisible.transpose().mmul(hiddenFromReconst)).divi(batch.getRows());

				// Update W
				weights.addi(gradW.mul(updateFactor));
				weightChanges++;




				// compute gradient of B
				FloatMatrix gradB = batch.columnSums().subi(reconstructedVisible.columnSums()).divi(batch.getRows());

				// Update B
				B.addi(gradB.mul(updateFactor));



				// compute gradient of C
				FloatMatrix gradC = hiddenFromData.columnSums().subi(hiddenFromReconst.columnSums()).divi(batch.getRows());

				// Update C
				C.addi(gradC.mul(updateFactor));





	            // End of negative phase
	            // error for training this batch
				FloatMatrix reconstructedDataProbs = Reconstruct(batch, B, C, 1, false);
	            double err = calcError(batch, reconstructedDataProbs);

	            // sum error of all batches
	            error = error + (err / numMiniBatches);

				// update the ui
				if (weightChanges % updateInterval == 0)
					this.fireTrainingsEvent(new NeuralNetwork(weights.rows, weights.columns, weights.data.clone(), false), weightChanges, error/batchIndex*numMiniBatches);
			}

			// update the ui
			this.fireTrainingsEvent(new NeuralNetwork(weights.rows, weights.columns, weights.data.clone(), false), weightChanges, error);

			if (error < this.threshold) {
				System.out.println("Breaking loop at epoch " + epoch + " and weightChange: "+weightChanges);
				break;
			}
		}

		return 0;
	}

	private FloatMatrix Reconstruct(FloatMatrix data, FloatMatrix B, FloatMatrix C, int numSteps, boolean states) {

		// Initial value is set to input visible
		FloatMatrix reconstructedVisible = data;

		// perform Gibbs-sampling
		for (int t = 0; t < numSteps; t++) {

			// 1. sample hidden units
			FloatMatrix hiddenStates = runVisible(reconstructedVisible, C);
			if(states)	makeStates(hiddenStates);

			// 2. sample visible units
			reconstructedVisible = runHidden(hiddenStates, B);
			if(states)	makeStates(reconstructedVisible);
		}

		return reconstructedVisible;
	}

	private FloatMatrix runVisible(FloatMatrix visible, FloatMatrix bias) {
		final FloatMatrix posHiddenActivations = visible.mmul(weights).addiRowVector(bias);
		applySigmoidFunction(posHiddenActivations);
		return posHiddenActivations;
	}

	private FloatMatrix runHidden(FloatMatrix hidden, FloatMatrix bias) {
		final FloatMatrix negVisibleActivations = hidden.mmul(weights.transpose()).addiRowVector(bias);
		applySigmoidFunction(negVisibleActivations);
		return negVisibleActivations;
	}

	private FloatMatrix makeStates(FloatMatrix mat) {
		float[] rawData = mat.data;
		for (int i = 0; i < rawData.length; i++)
			rawData[i] = rawData[i] > rand.nextFloat() ? 1 : 0;
		return mat;
	}

	public double calcError(FloatMatrix data) {

		return 0;

//		// positive phase
//		final FloatMatrix posHiddenProbs = runVisible(data);
//
//		// reconstruction
//		final FloatMatrix reconstructedDataProbs = runHidden(posHiddenProbs);
//
//		// calculate error only once in an updateInterval
//		return calcError(data, reconstructedDataProbs);
	}


	/**
	 * TODO: Fehlerberechnung nur für MNIST Set
	 *
	 * @param data
	 * @param reconstruction
	 * @return
	 */
	public double calcError(final FloatMatrix data, final FloatMatrix reconstruction) {
		final int numExamples = data.getRows();
		final int numVisible = rbm.getNumVisible();
		final FloatMatrix difference = data.sub(reconstruction);
		return (float)Math.sqrt(MatrixFunctions.pow(difference, 2.0f).sum() / (numExamples * numVisible)) * 255;
	}

	public FloatMatrix applySigmoidFunction(final FloatMatrix m) {
		final float[] data = m.data;
		for (int i = 0; i < data.length; i++)
			data[i] = logistic(data[i]); // 1 / (1 + e^-x)
		return m;
	}

	public static float logistic(float val) {
		return (float) (1. / ( 1. + Math.exp(-val) ));
	}

	@Override
	public TrainerType getTrainerType() {
		return TrainerType.GO;
	}

	@Override
	public TargetDevice getTargetDevice() {
		return TargetDevice.CPU;
	}

	@Override
	public boolean isBiasedTrainer() {
		return false;
	}
}
