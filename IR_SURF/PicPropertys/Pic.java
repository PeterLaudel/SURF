package app;

import java.awt.image.BufferedImage;

public class Pic extends Object{
	
	int rank;                 	// Position bei sortierter 1D-Reihenfolge
	
	double[] featureVector;
	
	// Originalgroesse des Bildes
	int origWidth; 
	int origHeight;
	
	// Zeichenpositionen 
	int xStart = 0;
	int xLen = 0;
	int yStart = 0;
	int yLen = 0;
	
	
	BufferedImage bImage;
	
	String name;
	String type;
	int typeOcc;
	int id;
	boolean isSelected;
	
	// zur Visualisierung
	BufferedImage featureImage;

	public double distance;
}
