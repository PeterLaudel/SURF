package de.htw.lcs.test;

import de.htw.lcs.ml.NeuralNetwork;

public class NeuralNetworkTest {

	public static void main(String[] args) {

		int numVis = 2;
		int numHid = 3;

		// { 1,2,3
		//	 44,55,66 }
		float[] w = new float[] { 1,44 , 2,55 , 3,66 };

		NeuralNetwork nn = new NeuralNetwork(numVis, numHid, w, false);
		print(nn);
		System.out.println();
		NeuralNetwork nnBiased = nn.biased();
		print(nnBiased);
		System.out.println();
		print(nnBiased.unbiased());
	}

	public static void print(NeuralNetwork nn) {
		StringBuilder sb = new StringBuilder();
		int numHid = nn.getNumHidden();
		int numVis = nn.getNumVisible();
		float[] data = nn.getWeights();
		for (int y = 0; y < numVis; y++) {
			for (int x = 0; x < numHid; x++) {
				sb.append(data[y + x * numVis]);
				sb.append(';');
			}
			sb.append('\n');
		}
		System.out.println(sb.toString());
	}

}
