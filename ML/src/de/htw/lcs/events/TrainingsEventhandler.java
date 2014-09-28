package de.htw.lcs.events;

import java.util.ArrayList;
import java.util.List;

import de.htw.lcs.ml.NeuralNetwork;


public class TrainingsEventhandler {

	private List<TrainingsListener> updateListeners = new ArrayList<TrainingsListener>();

	public void addEventListener(TrainingsListener listener)  {
		 updateListeners.add(listener);
	}

	protected void fireTrainingsEvent(NeuralNetwork rbm, int epoche, double error) {
		fireTrainingsEvent(rbm, epoche, error, 0);
	}

	protected void fireTrainingsEvent(NeuralNetwork rbm, int epoche, double error, double testError) {
		for (TrainingsListener listener : updateListeners)
			listener.update(rbm, epoche, error, testError);
	}
}