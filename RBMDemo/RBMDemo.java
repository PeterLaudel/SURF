package de.htw.lcs.rbm;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.jblas.FloatMatrix;

import de.htw.lcs.cl.OCL;
import de.htw.lcs.data.DataContainerManager;
import de.htw.lcs.data.model.DataContainer;
import de.htw.lcs.data.model.DataShard.DataShardType;
import de.htw.lcs.data.structure.DataStructure;
import de.htw.lcs.data.structure.DataStructure.FeatureVectorType;
import de.htw.lcs.math.CLFloatMatrix;
import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.WeightInitializer;
import de.htw.lcs.ml.trainer.MiniBiasRBMTrainerOCL;
import de.htw.lcs.ml.trainer.RBMTrainer;
import de.htw.lcs.ml.trainer.TrainerFactory;
import de.htw.lcs.ml.trainer.TrainerFactory.TargetDevice;
import de.htw.lcs.ml.trainer.TrainerFactory.TrainerType;
import de.htw.lcs.rbm.ui.TrainingsController;
import de.htw.lcs.utils.ArrayUtils;

/**
 * GC
 * http://sanjaal.com/java/tag/jvm-option-parameter-xxuseconcmarksweepgc/
 *
 * @author Nico
 *
 */
public class RBMDemo {

	private static final String pathToDataSet = "c:/Data/Dataset/WildFaces/training_set1/";

	private static final int reduceToBits = 1500;
	private static final int maxEpochs = 5000;
	private static final int updateInterval = 250;
	private static final float learnRate = 0.5f;

	private static final TrainerType trainerType = TrainerType.MINIBIAS;
	private static final TargetDevice targetDevice = TargetDevice.GPU;

	public static void main(String[] args) throws InterruptedException  {

		// setzte die Lernrate für alle RBM Trainer
		RBMTrainer.DEFAULT_LEARNING_RATE = learnRate;

		try {

			// feste seeds
			WeightInitializer.initSeed(-1995863238); // -1995863238 rnd.nextInt()

			// besorge die Trainingsdaten
			float[][] trainingsData = getTrainingsData(Paths.get(pathToDataSet));

			// erzeuge ein Netzwerk welches mit den Trainer trainiert werdne kann
			TrainerFactory factory = new TrainerFactory(trainerType, targetDevice);

			train(trainingsData, factory);

			// aufräumen
			CLFloatMatrix.clear();
			System.gc();
			Thread.sleep(2000);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static float[][] getTrainingsData(Path dir) {

		List<Path> imageFiles = new ArrayList<>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{jpg, png}")) {
			for (Path entry : stream) {
				try {

					dc.readFile(entry);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	private static void train(float[][] data, TrainerFactory factory) {

		 // Get the Java runtime
	    Runtime runtime = Runtime.getRuntime();

		// OCL vs JBlas unterschiede -> Floating Point diskrepanzen
		// http://gafferongames.com/networking-for-game-programmers/floating-point-determinism/
		try(final OCL ocl = new OCL()) {

			// lade in OCL die Funktionen für die FloatMatrix
			CLFloatMatrix.setUpOCL(ocl);

			NeuralNetwork network = factory.createNeuralNetwork(data[0].length, reduceToBits);
			RBMTrainer trainer = factory.createTrainer(network);

			// eigentliches Training
			long start = System.nanoTime();
			double error = trainer.train(data, maxEpochs, updateInterval);
			long stop = System.nanoTime();

			System.out.println("Trainingserror for "+network.getNumVisible()+"x"+network.getNumHidden()+" is "+error+" and took "+(int)((stop-start)/1_000_000_000)+"s; free memory "+(runtime.freeMemory() / (1024L * 1024L) + " total memory "+(runtime.totalMemory() / (1024L * 1024L) + " max memory "+(runtime.maxMemory() / (1024L * 1024L)))));

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
