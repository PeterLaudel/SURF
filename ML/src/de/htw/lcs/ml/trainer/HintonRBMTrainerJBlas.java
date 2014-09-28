package de.htw.lcs.ml.trainer;

import java.util.Arrays;
import java.util.Random;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.trainer.NetworkTrainer;
import de.htw.lcs.ml.trainer.RBMTrainer;
import de.htw.lcs.ml.trainer.RBMTrainerJBlas;
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
public class HintonRBMTrainerJBlas extends RBMTrainer {

	protected final FloatMatrix weights;
	private Random rand = new Random();

    private float epsilonRealW         = 0.01f; // 0.001
    private float epsilonBinaryW       = 0.1f;  // 0.1
    private float epsilonvb            = 0.01f; // 0.001
    private float epsilonhb            = 0.01f; // 0.001
    private float epsilona             = 0.01f; // 0.001
    private float weight_cost          = 0.0002f;// L1-decay 0.0002 & L2 0.00002
    private float initial_momentum     = 0.5f;
    private float final_momentum       = 0.9f;
    private int final_momentum_epoch   = 5000000;

	public HintonRBMTrainerJBlas(NeuralNetwork rbm) {
		this(rbm, DEFAULT_LEARNING_RATE, DEFAULT_THRESHOLD);
	}

	public HintonRBMTrainerJBlas(final NeuralNetwork rbm, final float learningRate, final float threshold) {
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


		boolean UsePersistent = false;
		int MiniBatchSize = 32;

		boolean isBinary = false;
		float momentum = 0.5f;
		float epsilonw = (isBinary) ? epsilonBinaryW : epsilonRealW;

		// Daten gewrapped in FloatMatrix
		FloatMatrix gradWMat = new FloatMatrix(weights.getRows(), weights.getColumns(), rbm.getGradW());
		FloatMatrix gradBMat = new FloatMatrix(1, rbm.getGradB().length, rbm.getGradB());
		FloatMatrix gradCMat = new FloatMatrix(1, rbm.getGradC().length, rbm.getGradC());

		FloatMatrix visibleBias = new FloatMatrix(1, rbm.getB().length, rbm.getB());
		FloatMatrix hiddenBias = new FloatMatrix(1, rbm.getC().length, rbm.getC());


		// erstelle mini batches
		int numMiniBatches = data.getRows() / MiniBatchSize;
		FloatMatrix[] batchedData = new FloatMatrix[numMiniBatches];
		FloatMatrix[] batchedPersistentVisibleUnits = new FloatMatrix[numMiniBatches];
		for (int i = 0; i < numMiniBatches; i++) {
			int fromRow = i * MiniBatchSize, toRow = (i+1) * MiniBatchSize;
			FloatMatrix batch = data.getRange(fromRow, toRow, 0, data.getColumns());
			batchedData[i] = batch;

			if(UsePersistent) {
				float[] buff = batch.data;
				float[] copy = Arrays.copyOf(buff, buff.length);
				batchedPersistentVisibleUnits[i] = new FloatMatrix(batch.getRows(), batch.getColumns(), copy);
			}
		}

		// durchlaufe alle Epochen und Batches
		for (int epoch = 0; epoch < maxEpochs; epoch++) {
			double error = 0;
			for (int batchIndex = 0; batchIndex < numMiniBatches; batchIndex++) {
				FloatMatrix batch = batchedData[batchIndex];
				int num_cases = batch.getRows();

				 // Start of positive CD phase (aka reality phase)
		        // clamp to data
		        FloatMatrix positive_hidden_probabilities = runVisible(batch, hiddenBias, isBinary);

	            // sample from hidden units probabilities
		        FloatMatrix positive_associations = batch.transpose().mmul(positive_hidden_probabilities);

	            // sum each colum of updated probabilities and original data to
	            // update biases
	        	FloatMatrix positive_hidden_activations = positive_hidden_probabilities.columnSums();
	            FloatMatrix positive_visible_activations = isBinary ? batch.columnSums() : batch.subRowVector(visibleBias).columnSums();


		        // End of positive phase
	            FloatMatrix positive_hidden_states = positive_hidden_probabilities;
//	            makeStates(positive_hidden_states);

		        // Start of negative CD phase (aka daydreaming phase)
		        // reconstruct the visible units
	            FloatMatrix negative_data = runHidden(positive_hidden_states, visibleBias, isBinary);




	            // sample again from the hidden units
	            FloatMatrix negative_hidden_probabilities = runVisible(negative_data, hiddenBias, isBinary);
	            FloatMatrix negative_associations = negative_data.transpose().mmul(negative_hidden_probabilities);

	            // sum each colum of reconstructed probabilities and data to
	            // update biases
	            FloatMatrix negative_hidden_activations   = negative_hidden_probabilities.columnSums();
	            FloatMatrix negative_visible_activations  = isBinary ? negative_data.columnSums() : negative_data.subRowVector(visibleBias).columnSums();

	            // End of negative phase
	            // error for training this batch
	            double err = calcError(batch, negative_data);

	            // sum error of all batches
	            error = error + (err / numMiniBatches);

	            // switch to final momentum if required
	            if(epoch > final_momentum_epoch)
	                momentum = final_momentum;
	            else
	                momentum = initial_momentum;


	            // aktuelles weight decay
//	            DoubleMatrix weightDecay = weights.mul(weights.lt(0).mul(-1)).mul(weight_cost); // L1-Decay
//	            DoubleMatrix weightDecay = weights.mul(weights).divi(2).mul(weight_cost); // L2-Decay
	            FloatMatrix weightDecay = weights.mul(weight_cost); // Simple-Decay

	            // Update weights and biases compute increments using error,
	            // learning rate, momentum and cost of weight updates
	            gradWMat.muli(momentum).addi(positive_associations.sub(negative_associations).divi(num_cases).subi(weightDecay).muli(epsilonw));
	            gradBMat.muli(momentum).addi(positive_visible_activations.sub(negative_visible_activations).mul(epsilonvb / num_cases));
	            gradCMat.muli(momentum).addi(positive_hidden_activations.sub(negative_hidden_activations).mul(epsilonhb / num_cases));

	            // update weights and biases
	            weights.addi(gradWMat);
	            visibleBias.addi(gradBMat);
	            hiddenBias.addi(gradCMat);
			}


			// calculate error only once in an updateInterval
			if (epoch % updateInterval == 0) {
				//currentError = calcError(data, reconstructedDataProbs);
				this.fireTrainingsEvent(new NeuralNetwork(weights.rows, weights.columns, weights.data.clone(), false), epoch, error);

				if(error < this.threshold) {
					System.out.println("Breaking at loop " + epoch);
					break;
				}
			}
		}

		return 0;
	}

	private FloatMatrix Reconstruct(FloatMatrix data, FloatMatrix B, FloatMatrix C, int numSteps) {

		// Initial value is set to input visible
		FloatMatrix reconstructedVisible = data;

		// perform Gibbs-sampling
		for (int t = 0; t < numSteps; t++) {

			// 1. sample hidden units
			FloatMatrix hiddenStates = this.runVisible(reconstructedVisible, C, true);

			// 2. sample visible units
			reconstructedVisible = runHidden(hiddenStates, B, true);
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
		return TrainerType.HINTON;
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
