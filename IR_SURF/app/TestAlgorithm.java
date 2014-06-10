package app;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import app.IR_Project.MyCanvas;

public class TestAlgorithm {

	private MyCanvas myCanvas;
	private String path;
	private Pic[] pics;
	private int numPictures;
	private ArrayList<Integer> picTypeOcc;
	private Vector<String> picTypes;
	private Graph graph = null;

	public TestAlgorithm(MyCanvas myCanvas, String path, Pic[] pics) {
		this.myCanvas = myCanvas;
		setUp(path, pics);
	}

	public void setUp(String path, Pic[] pics) {
		this.path = path;
		this.pics = pics;
		picTypes = new Vector<String>();
		picTypeOcc = new ArrayList<Integer>();
		numPictures = pics.length;

		// läuft über alle Bilder um pictypes+pictypeocc zu bestimmen
		for (int i = 0; i < numPictures; i++) {
			if (pics[i] != null) {
				// wenn typ noch nicht in picTypes
				if (!picTypes.contains(pics[i].type)) {
					// zu allTypes hinzufügen
					picTypes.add(pics[i].type);
					// neuer Eintrag in TypeOccurrence
					picTypeOcc.add(1);
				} else {
					// Erhöhe den Zähler für diesen Typ um eins
					int pos = picTypes.indexOf(pics[i].type);
					picTypeOcc.set(pos, (picTypeOcc.get(pos) + 1));
				}
			}
		}

		// auch in pic speichern wie of der jeweilige typ vorkommt
		for (int i = 0; i < picTypes.size(); i++) {
			for (int j = 0; j < pics.length; j++) {
				if (pics[j].type.equals(picTypes.elementAt(i))) {
					pics[j].typeOcc = picTypeOcc.get(i);
				}
			}
		}
	}

	public Vector<String> getAllTypes() {
		return picTypes;
	}

	public void test(Vector<Pic> selectedPics, String description) {
		int totalTestRuns = selectedPics.size();
		int numPics = pics.length;

		float[][] precision = new float[totalTestRuns][numPics];
		float[][] recall = new float[totalTestRuns][numPics];
		int[][] relRetrieved = new int[totalTestRuns][numPics];
		int[] numRelevant = new int[totalTestRuns];

		float[] meanPrecision = new float[numPics];
		float[] meanRecall = new float[numPics];

		float[] averagePrecision = new float[totalTestRuns];
		float meanAveragePrecision = 0;

		float[][] avgPOverR = new float[4][numPics];

		// ueber markierte Bilder Laufen
		for (int testRun = 0; testRun < totalTestRuns; testRun++) {
			myCanvas.sortBySimilarity(selectedPics.elementAt(testRun).id); 
			// nach aktuellem  Bild sortieren lassen

			String currentType = selectedPics.elementAt(testRun).type;
			int currTypeOcc = selectedPics.elementAt(testRun).typeOcc;
			numRelevant[testRun] = currTypeOcc;

			Pic[] picsSorted = new Pic[numPics];
			for (int k = 0; k < numPics; k++) {
				picsSorted[(pics[k].rank)] = pics[k];
			}

			// precision und recall berechnen
			int currRelevantRetrieved = 0;
			for (int pic = 0; pic < numPics; pic++) {
				if (picsSorted[pic].type.equals(currentType)) {
					currRelevantRetrieved++;
				}
				relRetrieved[testRun][pic] = currRelevantRetrieved;
				precision[testRun][pic] = (float) currRelevantRetrieved / (pic + 1);
				recall[testRun][pic] = (float) currRelevantRetrieved / currTypeOcc;
			}
		}

		// calculate avg p and r
		for (int testRun = 0; testRun < meanPrecision.length; testRun++) {
			float currPrecisionSum = 0f;
			for (int pic = 0; pic < precision.length; pic++) {
				currPrecisionSum += precision[pic][testRun];
			}
			meanPrecision[testRun] = currPrecisionSum / precision.length;
		}

		for (int testRun = 0; testRun < meanRecall.length; testRun++) {
			float currRecallSum = 0f;
			for (int pic = 0; pic < recall.length; pic++) {
				currRecallSum += recall[pic][testRun];
			}
			meanRecall[testRun] = currRecallSum / recall.length;
		}

		// calculate average precision
		for (int testRun = 0; testRun < totalTestRuns; testRun++) {
			float pSum = 0f;
			for (int pic = 0; pic < numPics; pic++) {
				if (pic > 0) {
					if (precision[testRun][pic] >= precision[testRun][pic - 1]) {
						pSum += precision[testRun][pic];
						// System.out.println("pic "+pic+" is relevant!");
					}
				} else {
					if (precision[testRun][pic] > 0) {
						pSum += precision[testRun][pic];
						// System.out.println("pic "+pic+" is relevant!");
					}
				}
				// System.out.println("pic:"+pic+"  "+"rel:"+relRetrieved[testRun][pic]+"   pre:"+precision[testRun][pic]+"  rec:"+recall[testRun][pic]);
			}
			averagePrecision[testRun] = pSum / (float) numRelevant[testRun];
			//System.out.println("ap:" + averagePrecision[testRun]);
		}

		// calculate mean average precision
		float averagePrecisionSum = 0;
		for (int testRun = 0; testRun < totalTestRuns; testRun++) {
			averagePrecisionSum += averagePrecision[testRun];
		}
		meanAveragePrecision = averagePrecisionSum / (float) totalTestRuns;
		System.out.println();
		System.out.println("meanAveragePrecision:" + meanAveragePrecision);

		// durchscnittswerte fuer die Darstellung vorbereiten

		for (int pic = 0; pic < numPics; pic++) {
			avgPOverR[2][pic] = meanPrecision[pic];
			avgPOverR[3][pic] = meanRecall[pic];
		}

		if ( !description.equals("") ) {
			// Beschriftung
			String sortMethod = myCanvas.getSortMethod();

			// String
			// title=sortMethod+" "+description+" mAP: "+meanAveragePrecision;
			String title = String.format(Locale.ENGLISH,"%s %s mAP=%6.3f", sortMethod,
					description, meanAveragePrecision);
			if (graph != null && !graph.closed)
				graph.addGraph(avgPOverR, title);
			else
				graph = new Graph(avgPOverR, path, title);
		}
	}

}
