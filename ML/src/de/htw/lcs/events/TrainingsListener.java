package de.htw.lcs.events;

import de.htw.lcs.ml.NeuralNetwork;


public interface TrainingsListener {

	public void update(NeuralNetwork rbm, int epoche, double error, double testError);
}
