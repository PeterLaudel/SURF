package de.htw.lcs.ml.trainer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import sun.misc.Unsafe;
import de.htw.lcs.ml.NeuralNetwork;
import de.htw.lcs.ml.WeightInitializer;

public class TrainerFactory {


	static {

		Unsafe unsafe = null;
        try
        {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe)field.get(null);
        }
        catch (Exception e) { throw new RuntimeException(e); }

		trainerClasses = new HashMap<>();
		isBiasedTrainer = new HashMap<>();

		// https://code.google.com/p/reflections/
		Reflections reflections = new Reflections();

		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// TODO: annotationen wären vielleicht besser, da dann keine instanzierung nötg wäre
		// finde alle Klassen die von RBMTrainer Ableiten
//		NeuralNetwork rbm = new NeuralNetwork(1, 1, new float[1], false);
		Set<Class<? extends RBMTrainer>> subTypes = reflections.getSubTypesOf(RBMTrainer.class);
		for (Class<? extends RBMTrainer> trainerClass : subTypes) {
			try {
				// besorge eine Instance von diesen Klassen
				RBMTrainer trainer = (RBMTrainer) unsafe.allocateInstance(trainerClass);
//				RBMTrainer trainer = trainerClass.getDeclaredConstructor(NeuralNetwork.class).newInstance(rbm);

				// registriere den Trainer bei der trainer Factory
				TrainerFactory.registerTrainer(trainer, trainerClass);

				System.out.println("trainerClass: "+ trainerClass.toString());
			} catch (Exception e) {	e.printStackTrace(); }
		}
	}

	protected static Map<Integer, Class<? extends RBMTrainer>> trainerClasses;
	protected static Map<Integer, Boolean> isBiasedTrainer;


	public static enum TrainerType { HINTON, RAWGO, GO, NORMAL, BIAS, ZEROBIAS, FIXEDBIAS, USEFULLBIAS, STATICBIAS, MEANBIAS, MINIBIAS, MINIBIASTEST };
	public static enum TargetDevice { CPU, GPU };

	/**
	 * fügt eine neue trainer klasse zu liste hinzu und überschreibt die
	 * alte mit den gleichen TrainerType und TargetDevice, falls vorhanden
	 *
	 * @param type
	 * @param device
	 * @param trainer
	 */
	private static void registerTrainer(RBMTrainer trainer, Class<? extends RBMTrainer> trainerClass) {

		// besorge die Date
		int key = getKey(trainer.getTrainerType(), trainer.getTargetDevice());

		// speichere
		trainerClasses.put(key, trainerClass);
		isBiasedTrainer.put(key, trainer.isBiasedTrainer());

		// logge
//		System.out.println("Register RBM Trainer "+trainer.toString()+" with settings ("+type.toString()+" and "+device.toString()+") to TrainerFactory.");
	}

	/**
	 * Erstellt einen eindeutigen key aus den beiden Parametern
	 * @param type
	 * @param device
	 * @return
	 */
	protected static int getKey(TrainerType type, TargetDevice device) {
		return device.ordinal() + type.ordinal() * 10;
	}

	private TrainerType type;
	private TargetDevice device;

	public TrainerFactory(TrainerType type, TargetDevice device) {
		this.type = type;
		this.device = device;
	}

	/**
	 * liefert ein neues Trainer Objekt zurück welches die angegebenen Parameter erfüllt
	 *
	 * @param type
	 * @param device
	 * @param rbm
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 */
	public RBMTrainer createTrainer(NeuralNetwork rbm) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
		int key = getKey(type, device);
		Class<? extends RBMTrainer> trainerClass = trainerClasses.get(key);

		// gibt es überhaupt eine Klasse die diese Anforderungen erfüllt und hat sie sich überhaupt registriert
		if(trainerClass == null)
			throw new ClassNotFoundException("No trainer class found for type "+type.toString()+" and target device "+device.toString());

		// erstelle Objekt
		return trainerClass.getDeclaredConstructor(NeuralNetwork.class).newInstance(rbm);
	}

	/**
	 * erstelle ein neues Netzwerk welches genau mit diesesn Trainer trainiert werden kann
	 *
	 * @return
	 */
	public NeuralNetwork createNeuralNetwork(int visibleUnits, int hiddenUnits) {
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int key = getKey(type, device);
		boolean isBiased = isBiasedTrainer.get(key);

		// biased Gewichte brauchen mehr units
		if(isBiased) {
			visibleUnits++;
			hiddenUnits++;
		}

		// Gewichte die nur vom Trainer mit Bias verwendet werden können
		final float[] weightsArray = WeightInitializer.randFloats(visibleUnits * hiddenUnits);
		return new NeuralNetwork(visibleUnits, hiddenUnits, weightsArray, isBiased);
	}
}
