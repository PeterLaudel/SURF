package de.htw.lcs.rbm;

import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jblas.FloatMatrix;

public class DataTest {

	private static final String pathToDataSet = "c:/Data/Dataset/holiday/file.ext";

	public static void main(String[] args) {

		float[][] trainingsData = loadData(Paths.get(pathToDataSet));
		FloatMatrix data = new FloatMatrix(trainingsData);

		float max = data.max();
		float min = data.min();
		float mean = data.mean();
		float variance = 0;
		for (int i = 0; i < data.data.length; i++)
			variance += (data.data[i] - mean) * (data.data[i] - mean);

		System.out.println("mean: "+mean+" - max: "+max+" - min: "+min+" - variance: "+Math.sqrt(variance));

		// normalisiere die Daten
		for (int i = 0; i < data.data.length; i++)
			data.data[i] = (data.data[i] + Math.abs(min)) / (Math.abs(min)+Math.abs(max));

		mean = data.mean();
		max = data.max();
		min = data.min();
		variance = 0;
		for (int i = 0; i < data.data.length; i++)
			variance += (data.data[i] - mean) * (data.data[i] - mean);
		System.out.println("mean: "+mean+" - max: "+max+" - min: "+min+" - variance: "+Math.sqrt(variance));
	}

	private static float[][] loadData(Path path) {

		try(ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
		    return (float[][]) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
