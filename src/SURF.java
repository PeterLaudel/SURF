import java.awt.geom.Point2D;
import java.util.ArrayList;

public class SURF {

	private Image m_image;
	private IntegralImage m_integralImage;
	private int m_octaveDepth;
	private Octave[] m_octaves;

	public SURF(Image image, int octaveDepth) {
		m_image = image;
		m_octaveDepth = octaveDepth;
		m_octaves = new Octave[m_octaveDepth];

		for (int i = 0; i < m_octaves.length; i++)
			m_octaves[i] = new Octave(i + 1);

		m_integralImage = new IntegralImage(m_image);

	}

	public Image GetImage() {
		return m_image;
	}

	public int GetOctaveDepth() {
		return m_octaveDepth;
	}

	public Octave[] GetOctaves() {
		return m_octaves;
	}

	public void Process() {

		for (int i = 0; i < m_octaves.length; i++) {
			m_octaves[i].ComputeOctaves(m_integralImage);
		}

		ArrayList<InterestPoint> result = FindLocalMaximum();

		CreateDescriptor(result);
	}

	public Image GetOctaveImage(int octaveNumber, int octaveLayer) {
		if (octaveNumber >= m_octaveDepth || octaveLayer > 3)
			return null;

		return m_octaves[octaveNumber].GetOctaveImage(octaveLayer);
	}

	private ArrayList<InterestPoint> FindLocalMaximum() {
		ArrayList<InterestPoint> result = new ArrayList<InterestPoint>();
		int[] neighborhood = new int[] { 1, 0, -1 };
		for (int i = 0; i < m_octaves.length; i++) {
			HarrisResponse[] octaveLayer = m_octaves[i].GetOctave();
			for (int j = 1; j < octaveLayer.length - 1; j++) {
				HarrisResponse octaveLayerImage = octaveLayer[j];
				for (int x = 1; x < octaveLayerImage.GetWidth() - 1; x++)
					for (int y = 1; y < octaveLayerImage.GetHeight() - 1; y++) {
						boolean found = true;
						failed: for (int u = 0; u < neighborhood.length; u++)
							for (int v = 0; v < neighborhood.length; v++)
								for (int w = 0; w < neighborhood.length; w++) {
									int layer = j + neighborhood[w];
									if (octaveLayerImage.GetResponse(x, y) <= octaveLayer[layer]
											.GetResponse((x + neighborhood[u]),
													(y + neighborhood[v]))
											&& !(neighborhood[u] == 0
													&& neighborhood[v] == 0 && neighborhood[w] == 0)) {
										found = false;
										break failed;
									}
								}
						if (found)
							result.add(new InterestPoint(x, y, octaveLayer[j]
									.GetScale()));
					}
			}
		}
		return result;
	}

	void CreateDescriptor(ArrayList<InterestPoint> interestPoints) {
		int[] imagePixels = m_image.GetImagePixels();
		for (int i = 10; i < 11; i++) {

			InterestPoint ip = interestPoints.get(i);
			//ip.x = 90;
			//ip.y = 90;
			float radius = ip.scale * 6;
			int Left = (int) Math.round(ip.x - radius);
			int Right = (int) Math.round(ip.x + radius);
			int Top = (int) Math.round(ip.y - radius);
			int Bottom = (int) Math.round(ip.y + radius);
			double radius2 = radius * radius;

			BoxFilter xWavelet = BoxFilter.GetWaveletX(ip.scale * 4);
			BoxFilter yWavelet = BoxFilter.GetWaveletY(ip.scale * 4);

			float gaussianWeight = 1.0f / (2.0f * ip.scale);
			float xResponse = 0;
			float yResponse = 0;

			ArrayList<Point2D.Float> angles = new ArrayList<Point2D.Float>();
			for (int j = Top; j <= Bottom; j++) {
				for (int k = Left; k <= Right; k++) {
					double dist = Math.pow(ip.x - k, 2.0)
							+ Math.pow(ip.y - j, 2.0);
					if (dist <= radius2) {

						xResponse = m_integralImage.ApplyBoxFilter(xWavelet, k,
								j);
						yResponse = m_integralImage.ApplyBoxFilter(yWavelet, k,
								j);

						//xResponse *= gaussianWeight;
						//yResponse *= gaussianWeight;

						System.out.println("xResponse: " + xResponse
								+ "yResponse: " + yResponse);

						angles.add(new Point2D.Float(xResponse, yResponse));
						float ang = (float) (Math.atan2(yResponse, xResponse));
						if(j < 0 || k < 0 || j>= m_image.GetHeight() || k >= m_image.GetWidth())
							continue;
						int posTmp = j * m_image.GetWidth() + k;
						int response = (int) xResponse +128;
						imagePixels[posTmp] = 0xFF000000 | (response << 16) | (response << 8) | response;
					}
				}
			}

			// calculate the dominant direction
			float sumX = 0.f, sumY = 0.f;
			float max = 0.f, orientation = 0.f;
			float ang1 = 0.f, ang2 = 0.f;

			// loop slides pi/3 window around feature point
			for (ang1 = (float) -Math.PI; ang1 < Math.PI; ang1 += 0.15f) {
				
				ang2 = ang1 + ((float) Math.PI / 3.0f);
				
				if(ang2 > Math.PI)
					ang2 = -(ang2 - (float) Math.PI);
				//ang2 = (float) (ang1 + Math.PI / 3.0f > Math.PI ? -(ang1
				//		+ Math.PI / 3.0f - Math.PI) : ang1 + Math.PI / 3.0f);
				sumX = sumY = 0.f;
				for (int k = 0; k < angles.size(); k++) {
					Point2D.Float angle = angles.get(k);
					// get angle from the x-axis of the sample point
					float ang = (float) (Math.atan2(angle.y, angle.x));

					// determine whether the point is within the window
					if (ang1 < ang && ang2 > ang) {
						sumX += angle.x;
						sumY += angle.y;
					}
					else if(ang2 < ang1 && (ang2 > ang || ang1 < ang )) {
						sumX += angle.x;
						sumY += angle.y;
					}
				}

				// if the vector produced from this window is longer than all
				// previous vectors then this forms the new dominant direction
				if (sumX * sumX + sumY * sumY > max) {
					// store largest orientation
					max = sumX * sumX + sumY * sumY;
					orientation = (float) (Math.atan2(sumY, sumX));
				}
			}

			// assign orientation of the dominant response vector
			ip.orientation = orientation;
			float x = (float) Math.cos(orientation);
			float y = (float) Math.sin(orientation);

			int pos = ip.y * m_image.GetWidth() + ip.x;
			
			imagePixels[pos] = 0xFF000000 | (255 << 16) | (0 << 8) | 0;
			/*
			for(int n = 0; n < 20; n++)
			{
				int pos2 = (int) ((ip.y + y*n) * m_image.GetWidth() + (ip.x + x*n));
				imagePixels[pos2] = 0xFF000000 | (0 << 16) | (0 << 8) | 255;
			}
			*/

		}
	}

	/*
	 * private ArrayList<Integer> FindLocalMaximum(Image image) {
	 * ArrayList<Integer> list = new ArrayList<Integer>(); int n = 1; int step =
	 * 2*n +1;
	 * 
	 * for(int i = n; i < image.GetWidth()-n; i+=step) for(int j = n; j <
	 * image.GetHeight()-n; j+=step) { int mi = i; int mj = j;
	 * 
	 * for(int i2 = i; i2 < i + n; i2++) for(int j2 = j; j2 < j + n; j2++)
	 * if(image.GetPixel(i2, j2) > image.GetPixel(mi, mj)) { mi = i2; mj = j2; }
	 * boolean found = true; failed: for(int i2 = mi - n; i2 < mi + n; i2++)
	 * for(int j2 = mj - n; j2 < mj + n; j2++) if(image.GetPixel(i2, j2) >
	 * image.GetPixel(mi, mj)) { found = false; break failed; }
	 * 
	 * if(found) { int pos = mj * image.GetWidth() + mi; list.add(pos); } }
	 * 
	 * return list; }
	 */

}
