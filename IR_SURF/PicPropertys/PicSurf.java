

package PicPropertys;

import java.util.List;
import java.util.Vector;

import Features.InterestPoint;

public class PicSurf extends Pic{
	
	public List<InterestPoint> interestPoints;
	
	public PicSurf(Pic pic) {
		// TODO Auto-generated method stub
		
		interestPoints = new Vector<InterestPoint>();
		
		this.rank = pic.rank;                 	// Position bei sortierter 1D-Reihenfolge
		
		this.featureVector = pic.featureVector;
		
		// Originalgroesse des Bildes
		this.origWidth = pic.origWidth; 
		this.origHeight = pic.origHeight;
		
		// Zeichenpositionen 
		this.xStart = pic.xStart;
		this.xLen = pic.xLen;
		this.yStart = pic.yStart;
		this.yLen = pic.yLen;
	
		
		this.bImage = pic.bImage;
		
		this.name = pic.name;
		this.type = pic.type;
		this.typeOcc = pic.typeOcc;
		this.id = pic.id;
		this.isSelected = pic.isSelected;
		
		// zur Visualisierung
		this.featureImage = pic.featureImage;

		this.distance = pic.distance;
		
	}

}
