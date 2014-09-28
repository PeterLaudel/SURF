package de.htw.lcs.ml;

public class ActivationFunctions {


	public static float logit(float value) {
		if(value == 0) return -2f;
		if(value == 1) return 2f;

//		if(value == 0) value = 0.001f;
//		if(value == 1) value = 0.999f;

		return (float) -Math.log((1/value) - 1);
	}
}
