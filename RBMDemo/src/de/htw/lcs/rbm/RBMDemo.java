package de.htw.lcs.rbm;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.jblas.FloatMatrix;

import de.htw.lcs.cl.OCL;
import de.htw.lcs.events.TrainingsListener;
import de.htw.lcs.math.CLFloatMatrix;
import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.WeightInitializer;
import de.htw.lcs.ml.trainer.BiasRBMTrainerJBlas;
import de.htw.lcs.ml.trainer.RBMTrainer;
import de.htw.lcs.ml.trainer.RBMTrainerJBlas;
import de.htw.lcs.ml.trainer.TrainerFactory;
import de.htw.lcs.ml.trainer.TrainerFactory.TargetDevice;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;


/**
 * GC
 * http://sanjaal.com/java/tag/jvm-option-parameter-xxuseconcmarksweepgc/
 *
 * @author Nico
 *
 */
public class RBMDemo implements Serializable, TrainingsListener {

	private static final String pathToDataSet = "D:/HTW Berlin/4. Semester/IC/images/holiday_320/file600.ext";
	private static final String outputFile = "D:/HTW Berlin/4. Semester/IC/images/holiday_320/file600.output";
	private static final String weightPath = "D:/HTW Berlin/4. Semester/IC/images/holiday_320/weights.output";

	private static final int reduceToBits = 32;
	private static final int maxEpochs = 10;
	private static final int updateInterval = 1;
	private static final float learnRate = 0.1f;

	private static final TrainerType trainerType = TrainerType.MINIBIAS;
	private static final TargetDevice targetDevice = TargetDevice.CPU;

	public static void main(String[] args) { new RBMDemo(); }


	private float[][] trainingsData;

	public RBMDemo() {

		// setzte die Lernrate für alle RBM Trainer
		RBMTrainer.DEFAULT_LEARNING_RATE = learnRate;

		try {

			// feste seeds
			WeightInitializer.initSeed(-1995863238); // -1995863238 rnd.nextInt()

			// besorge die Trainingsdaten
			trainingsData = normalize(loadData(Paths.get(pathToDataSet)));

			// erzeuge ein Netzwerk welches mit den Trainer trainiert werdne kann
			TrainerFactory factory = new TrainerFactory(trainerType, targetDevice);

			// trainiere das Netzwerk
			train(trainingsData, factory);

			// aufräumen
			CLFloatMatrix.clear();
			System.gc();
			Thread.sleep(2000);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private  float[][] getHiddenData(float[][] trainingsData, NeuralNetwork network) {

		boolean isBiased = network.isWithBias();
		RBMTrainerJBlas rbmTrainer = (isBiased ? new BiasRBMTrainerJBlas(network) :  new RBMTrainerJBlas(network));

		FloatMatrix data = new FloatMatrix(trainingsData);
		return rbmTrainer.runVisible(data).toArray2();
	}

	private static void saveData(Path path, float[][] data) {
		try(ObjectOutputStream ois = new ObjectOutputStream(Files.newOutputStream(path))) {
		    ois.writeObject(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private float[][] normalize(float[][] input) {

		FloatMatrix data = new FloatMatrix(input);
		float max = Math.abs(data.max());
		float min = Math.abs(data.min());

		for (int i = 0; i < data.data.length; i++)
			data.data[i] = (data.data[i] + min) / (min+max);

		return data.toArray2();
	}


	private float[][] loadData(Path path) {

		try(ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
			return (float[][]) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private float[][] getTrainingsData(Path dir) throws Exception {

		List<float[]> imageData = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.png")) {
			for (Path entry : stream) {
				try {
					BufferedImage bi = ImageIO.read(entry.toFile());
					imageData.add(getGrayScale(bi));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// erstelle das endgültige Array
		float[][] result = new float[imageData.size()][];
		for (int i = 0; i < result.length; i++)
			result[i] = imageData.get(i);

		return result;
	}

	private  float[] getGrayScale(BufferedImage bi) throws IOException {

		int[] rgb = new int[bi.getWidth() * bi.getHeight()];
		bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), rgb, 0, bi.getWidth());

		float[] features = new float[bi.getWidth() * bi.getHeight()];
		for (int i = 0; i < rgb.length; i++) {
			int r = (rgb[i]  >> 16) & 0xFF;
			int g = (rgb[i]  >> 8) & 0xFF;
			int b = (rgb[i] ) & 0xFF;
			features[i] = (float)((r+g+b)/3)/255;
		}
		return features;
	}

	private float[][] train(float[][] data, TrainerFactory factory) {

		float[][] hiddenData = null;

		 // Get the Java runtime
	    Runtime runtime = Runtime.getRuntime();

		// OCL vs JBlas unterschiede -> Floating Point diskrepanzen
		// http://gafferongames.com/networking-for-game-programmers/floating-point-determinism/
		try/*(final OCL ocl = new OCL())*/ {

			// lade in OCL die Funktionen für die FloatMatrix
			//CLFloatMatrix.setUpOCL(ocl);

			// lade die Gewichte
			FloatMatrix weightMatrix = new FloatMatrix(loadData(Paths.get(weightPath)));
			NeuralNetwork network = new NeuralNetwork(weightMatrix.getRows(), weightMatrix.getColumns(), weightMatrix.data, true);

			// erzeuge neue Gewichte
//			NeuralNetwork network = factory.createNeuralNetwork(data[0].length, reduceToBits);

			// erstelle einen passenden TRainer
			RBMTrainer trainer = factory.createTrainer(network);
			trainer.addEventListener(this);

			// eigentliches Training
			long start = System.nanoTime();
			double error = trainer.train(data, maxEpochs, updateInterval);
			long stop = System.nanoTime();

			System.out.println("Trainingserror for "+network.getNumVisible()+"x"+network.getNumHidden()+" is "+error+" and took "+(int)((stop-start)/1_000_000_000)+"s; free memory "+(runtime.freeMemory() / (1024L * 1024L) + " total memory "+(runtime.totalMemory() / (1024L * 1024L) + " max memory "+(runtime.maxMemory() / (1024L * 1024L)))));

		} catch (final Exception e) {
			e.printStackTrace();
		}

		return hiddenData;
	}

	public void print(float[][] arr) {
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++)
				System.out.printf(" %.3f", arr[i][j]);
			System.out.println();
		}
	}

	@Override
	public void update(NeuralNetwork network, int epoche, double error, double testError) {

		System.out.println(epoche+". epoch with error "+error);

		// neuen Features speichern
		float[][] hiddenData = getHiddenData(trainingsData, network);
		saveData(Paths.get(outputFile), hiddenData);

		// gewichte der RBM speichern
		float[][] trainedW = network.getWeightMatrix2D();
		saveData(Paths.get(weightPath), trainedW);
	}
}
