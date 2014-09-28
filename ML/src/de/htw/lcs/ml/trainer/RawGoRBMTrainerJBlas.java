package de.htw.lcs.ml.trainer;

import java.util.Arrays;
import java.util.Random;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.TrainerFactory.TargetDevice;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;

/**
 * Auf Basis von
 * http://r9y9.github.io/blog/2014/03/06/restricted-boltzmann-machines-mnist/
 *
 * @author Nico
 *
 */
@NetworkTrainer
public class RawGoRBMTrainerJBlas extends RBMTrainer {

	protected final FloatMatrix weights;
	private Random rand = new Random();

	public RawGoRBMTrainerJBlas(NeuralNetwork rbm) {
		this(rbm, DEFAULT_LEARNING_RATE, DEFAULT_THRESHOLD);
	}

	public RawGoRBMTrainerJBlas(final NeuralNetwork rbm, final float learningRate, final float threshold) {
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
		data.addi(1.0e-21f);

		float LearningRate = 0.1f;
		float updateFactor = LearningRate / data.getRows();
		int OrderOfGibbsSampling = 1;
		boolean UsePersistent = false;
		int MiniBatchSize = 32;
		boolean L2Regularization = false;
		float RegularizationRate = 0.0001f;

		// Daten gewrapped in FloatMatrix
		FloatMatrix gradWMat = new FloatMatrix(weights.getRows(), weights.getColumns(), rbm.getGradW());
		FloatMatrix gradBMat = new FloatMatrix(1, rbm.getGradB().length, rbm.getGradB());
		FloatMatrix gradCMat = new FloatMatrix(1, rbm.getGradC().length, rbm.getGradC());

		FloatMatrix B = new FloatMatrix(1, rbm.getB().length, rbm.getB());
		FloatMatrix C = new FloatMatrix(1, rbm.getC().length, rbm.getC());


		// erstelle mini batches
		int numMiniBatches = (int)Math.ceil((double)data.getRows() / MiniBatchSize);
		FloatMatrix[] batchedData = new FloatMatrix[numMiniBatches];
		FloatMatrix[] batchedPersistentVisibleUnits = new FloatMatrix[numMiniBatches];
		for (int i = 0; i < numMiniBatches; i++) {
			int fromRow = i * MiniBatchSize, toRow = (i+1) * MiniBatchSize;
			if(data.getRows() <= toRow) toRow = data.getRows();
			FloatMatrix batch = data.getRange(fromRow, toRow, 0, data.getColumns());
			batchedData[i] = batch;

			if(UsePersistent) {
				float[] buff = batch.data;
				float[] copy = Arrays.copyOf(buff, buff.length);
				batchedPersistentVisibleUnits[i] = new FloatMatrix(batch.getRows(), batch.getColumns(), copy);
			}
		}

		// durchlaufe alle Epochen und Batches
		int weightChanges = 0;
		for (int epoch = 0; epoch < maxEpochs; epoch++) {
			double error = 0;
			for (int batchIndex = 0; batchIndex < numMiniBatches; batchIndex++) {
				FloatMatrix batch = batchedData[batchIndex];


				// Set start state of Gibbs-sampling
				FloatMatrix gibbsStart = batch;
				if(UsePersistent)
					gibbsStart = batchedPersistentVisibleUnits[batchIndex];

				// Perform reconstruction using Gibbs-sampling
				FloatMatrix reconstructedVisible = Reconstruct(gibbsStart, B, C, OrderOfGibbsSampling, true);

				// keep recostructed visible
				if(UsePersistent)
					batchedPersistentVisibleUnits[batchIndex] = reconstructedVisible;

				// pre-computation that is used in gradient computation
				FloatMatrix hiddenFromData = runVisible(batch, C, false);
				FloatMatrix hiddenFromReconst = runVisible(reconstructedVisible, C, false);

				// compute gradient of Wbatch
				FloatMatrix gradW = batch.transpose().mmul(hiddenFromData).subi(reconstructedVisible.transpose().mmul(hiddenFromReconst));

				// compute gradient of B
				FloatMatrix gradB = batch.columnSums().subi(reconstructedVisible.columnSums());

				// compute gradient of C
				FloatMatrix gradC = hiddenFromData.columnSums().subi(hiddenFromReconst.columnSums());

				// Normalized by size of mini-batch
				gradW.divi(batch.getRows() / updateFactor);
				gradB.divi(batch.getRows() / updateFactor);
				gradC.divi(batch.getRows() / updateFactor);

	            // End of negative phase
	            // error for training this batch
				FloatMatrix reconstructedDataProbs = Reconstruct(batch, B, C, OrderOfGibbsSampling, false);
	            double err = calcError(batch, reconstructedDataProbs);

	            // sum error of all batches
	            error = error + (err / numMiniBatches);

				// Momentum changes
				float momentum = 0.5f;
				if(epoch > 5)
					momentum = 0.5f;

				// Update W
				weights.addi(gradWMat.muli(momentum).addi(gradW));
				if(L2Regularization)
					weights.muli(1.0f - RegularizationRate);

				// Update B
				B.addi(gradBMat.muli(momentum).addi(gradB));

				// Update C
				C.addi(gradCMat.muli(momentum).addi(gradC));

				// update the ui
				weightChanges++;
				if (weightChanges % updateInterval == 0)
					this.fireTrainingsEvent(new NeuralNetwork(weights.rows, weights.columns, weights.data.clone(), false), weightChanges, error/batchIndex*numMiniBatches);
			}

			// calculate error only once in an updateInterval
			if (epoch % updateInterval == 0) {
				//currentError = calcError(data, reconstructedDataProbs);
				this.fireTrainingsEvent(new NeuralNetwork(weights.rows, weights.columns, weights.data.clone(), false), epoch, error);

				if (error < this.threshold) {
					System.out.println("Breaking at loop " + epoch);
					break;
				}
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
			FloatMatrix hiddenStates = this.runVisible(reconstructedVisible, C, states);

			// 2. sample visible units
			reconstructedVisible = runHidden(hiddenStates, B, states);
		}

		return reconstructedVisible;
	}

	private FloatMatrix runVisible(FloatMatrix visible, FloatMatrix bias, boolean states) {
		final FloatMatrix posHiddenActivations = visible.mmul(weights).addiRowVector(bias);
		applySigmoidFunction(posHiddenActivations);
		if(states)	makeStates(posHiddenActivations);
		return posHiddenActivations;
	}

	private FloatMatrix runHidden(FloatMatrix hidden, FloatMatrix bias, boolean states) {
		final FloatMatrix negVisibleActivations = hidden.mmul(weights.transpose()).addiRowVector(bias);
		applySigmoidFunction(negVisibleActivations);
		if(states) makeStates(negVisibleActivations);
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
		return TrainerType.RAWGO;
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
