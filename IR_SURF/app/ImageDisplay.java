package app;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

class ImageDisplay {
	BufferedImage bimage; // BufferendImage in das gezeichnet wird
	private int hCanvas; // Hoehe und Breite der Zeichenflaeche
	private int wCanvas;

	private double xm; // diese Variablen steuern die Verschiebung der Ansicht
						// (ueber Mouse-Drag)
	private double ym;

	private Pic[] pics; // Liste der zu zeichnenden Bilder
	private int h2; // Canvasmitte
	private int w2;
	private double zoomFactorLast = 1; // letzter Zoomfaktor (zur Berechnung der
										// Verschiebung des Bildes bei
										// Zoomaenderung)

	private double borderFactor = 0.9; // Randfaktor 0.9 bedeutet 90% Bild und
										// links und rechts 5% Rand
	private boolean drawFeatures;

	public ImageDisplay(int width, int height) {
		this.hCanvas = height;
		this.wCanvas = width;
		h2 = height / 2;
		w2 = width / 2;

		bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	public void resize(int width, int height) {
		this.hCanvas = height;
		this.wCanvas = width;
		h2 = height / 2;
		w2 = width / 2;

		bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	// zeichnet alle Bilder
	public void draw(Graphics g) {
		if (pics == null)
			return;

		Graphics2D gi = bimage.createGraphics();

		// Backgroundcolor grau
		gi.setBackground(Color.GRAY);
		gi.clearRect(0, 0, wCanvas, hCanvas);

		// zeichnen
		// Schleife ueber alle Bilder
		BufferedImage bi;

		for (int i = 0; i < pics.length; i++) {
			Pic pic = pics[i];
			if (pic != null) {

				if (drawFeatures == false)
					bi = pic.bImage;
				else
					bi = pic.featureImage;

				int xs = pic.xStart;
				int ys = pic.yStart;
				int xLen = pic.xLen;
				int yLen = pic.yLen;

				gi.drawImage(bi, xs, ys, xLen, yLen, null);
				if (pic.isSelected) {
					gi.setColor(Color.RED);
					gi.drawRect(xs, ys, xLen, yLen);
				}
			}
		}

		gi.dispose();
		g.drawImage(bimage, 0, 0, null);
	}

	// bestimmt die Zeichenpositionen fuer die Liste der (eindimensional)
	// sortierten Bilder
	// bei der Sortierung zu einem oder mehreren Vorgabebildern
	public void calculateDrawingPositions(int xMousePos, int yMousePos,
			int xMouseMove, int yMouseMove, double zoomFactor) {

		if (pics == null)
			return;

		int nThumbs = pics.length;

		// Groesse eines thumbnail-Bereichs
		int thumbSize = (int) Math.sqrt((double) wCanvas * hCanvas / nThumbs);
		while (thumbSize > 0 && (wCanvas / thumbSize) * (hCanvas / thumbSize) < nThumbs)
			--thumbSize;

		int mapPlacesX = wCanvas / thumbSize;
		int mapPlacesY = hCanvas / thumbSize;

		double thumbSizeX = (double) wCanvas / mapPlacesX;
		double thumbSizeY = (double) hCanvas / mapPlacesY;

		// avoid empty lines at the bottom
		while (mapPlacesX * (mapPlacesY - 1) >= nThumbs) {
			mapPlacesY--;
		}
		thumbSizeY = (double) hCanvas / mapPlacesY;

		double scaledThumbSizeX = thumbSizeX * zoomFactor;
		double scaledThumbSizeY = thumbSizeY * zoomFactor;

		double sizeX = scaledThumbSizeX * borderFactor;
		double sizeY = scaledThumbSizeY * borderFactor;
		double size = Math.min(sizeX, sizeY);

		double xDelta = (w2 - xMousePos) * (zoomFactor / zoomFactorLast - 1);
		double yDelta = (h2 - yMousePos) * (zoomFactor / zoomFactorLast - 1);
		zoomFactorLast = zoomFactor;

		double xmLast = xm;
		double ymLast = ym;

		xm -= (xMouseMove + xDelta) / scaledThumbSizeX;
		ym -= (yMouseMove + yDelta) / scaledThumbSizeY;

		int xMinPos = (int) (w2 - xm * scaledThumbSizeX);
		int xMaxPos = (int) (xMinPos + mapPlacesX * scaledThumbSizeX);
		int yMinPos = (int) (h2 - ym * scaledThumbSizeY);
		int yMaxPos = (int) (yMinPos + mapPlacesY * scaledThumbSizeY);

		// disallow to move out of the map by dragging
		if (xMinPos > 0 || xMaxPos < wCanvas - 1) {
			xm = xmLast;
			xMinPos = (int) (w2 - xm * scaledThumbSizeX);
			xMaxPos = (int) (xMinPos + mapPlacesX * scaledThumbSizeX);
		}
		// when zooming out (centered at the mouseposition) it might be
		// necessary to shift the map back to the canvas
		if (xMaxPos < wCanvas - 1) {
			int xMoveCorrection = wCanvas - 1 - xMaxPos;
			xMinPos += xMoveCorrection;
			xm -= xMoveCorrection / scaledThumbSizeX;
		} else if (xMinPos > 0) {
			xm += xMinPos / scaledThumbSizeX;
			xMinPos = 0;
		}

		// same for y
		if (yMinPos > 0 || yMaxPos < hCanvas - 1) {
			ym = ymLast;
			yMinPos = (int) (h2 - ym * scaledThumbSizeY);
			yMaxPos = (int) (yMinPos + mapPlacesY * scaledThumbSizeY);
		}
		if (yMaxPos < hCanvas - 1) {
			int yMoveCorrection = hCanvas - 1 - yMaxPos;
			yMinPos += yMoveCorrection;
			ym -= yMoveCorrection / scaledThumbSizeY;
		} else if (yMinPos > 0) {
			ym += yMinPos / scaledThumbSizeY;
			yMinPos = 0;
		}

		// Zeichenposition errechnen
		for (int i = 0; i < pics.length; i++) {
			Pic pic = pics[i];
			if (pic != null) {
				int w, h;
				if (drawFeatures == false) {
					w = pic.origWidth;
					h = pic.origHeight;
				} else {
					w = 64;
					h = 64;
				}
				// skalierung, keep aspect ratio
				double s = Math.max(w, h);
				double scale = size / s;

				int xLen = (int) (scale * w);
				int yLen = (int) (scale * h);

				int pos = pic.rank;

				int xStart = (int) (xMinPos + (pos % mapPlacesX)
						* scaledThumbSizeX);
				int yStart = (int) (yMinPos + (pos / mapPlacesX)
						* scaledThumbSizeY);

				int xs = xStart + (int) ((scaledThumbSizeX - xLen + 1) / 2); // xStart mit Rand
				int ys = yStart + (int) ((scaledThumbSizeY - yLen + 1) / 2);

				pic.xStart = xs;
				pic.xLen = xLen;
				pic.yStart = ys;
				pic.yLen = yLen;
			}
		}
	}

	// liefert die Id des Bildes zurueck, dass sich an einer bestimmten
	// Mausposition befindet
	// Rueckgabe von -1 bedeutet, dass unter der Maus kein Bild war
	public int getImageId(int xMouse, int yMouse) {

		// Schleife ueber alle Bilder
		for (int i = 0; i < pics.length; i++) {
			Pic pic = pics[i];
			if (pic != null) {
				int xs = pic.xStart;
				int ys = pic.yStart;
				int xLen = pic.xLen;
				int yLen = pic.yLen;

				if (xMouse > xs && xMouse < xs + xLen && yMouse > ys
						&& yMouse < ys + yLen) {
					return pic.id;
				}
			}
		}
		return -1; // no image found
	}

	public void setPics(Pic[] pics) {
		this.pics = pics;
	}

	public void drawFeature(boolean b) {
		this.drawFeatures = b;
	}
}
