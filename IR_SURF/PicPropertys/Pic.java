package PicPropertys;

import java.awt.image.BufferedImage;

public class Pic extends Object{
	
	public int rank;                 	// Position bei sortierter 1D-Reihenfolge
	
	public double[] featureVector;
	
	// Originalgroesse des Bildes
	public int origWidth; 
	public int origHeight;
	
	// Zeichenpositionen 
	public int xStart = 0;
	public int xLen = 0;
	public int yStart = 0;
	public int yLen = 0;
	
	
	public BufferedImage bImage;
	
	public String name;
	public String type;
	public int typeOcc;
	public int id;
	public boolean isSelected;
	
	// zur Visualisierung
	public BufferedImage featureImage;

	public double distance;
}
