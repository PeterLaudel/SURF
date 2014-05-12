import java.awt.geom.Point2D;
import java.util.ArrayList;

public class SURF {

	private Image m_image;
	private IntegralImage m_integralImage;
	private int m_octaveDepth;
	private Octave[] m_octaves;
	private float m_max;
	private ArrayList<InterestPoint> m_interestPoints;

	public SURF(Image image, int octaveDepth) {
		m_image = image;
		m_octaveDepth = octaveDepth;
		m_octaves = new Octave[m_octaveDepth];

		for (int i = 0; i < m_octaves.length; i++)
			m_octaves[i] = new Octave(i + 1);

		m_integralImage = new IntegralImage(m_image);
		m_max = Float.MIN_VALUE;
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
	
	public float GetMax()
	{
		return m_max;
	}

	public ArrayList<InterestPoint> Process() {

		for (int i = 0; i < m_octaves.length; i++) {
			m_octaves[i].ComputeOctaves(m_integralImage);
		}

		m_interestPoints = FindLocalMaximum();
		m_interestPoints.trimToSize();

		CreateDescriptor(m_interestPoints);

		return m_interestPoints;
	}
	
	public ArrayList<InterestPoint> GetInterestPoints()
	{
		return m_interestPoints;
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
						float response = octaveLayerImage.GetResponse(x, y);
						failed: for (int u = 0; u < neighborhood.length; u++)
							for (int v = 0; v < neighborhood.length; v++)
								for (int w = 0; w < neighborhood.length; w++) {
									int layer = j + neighborhood[w];
									if (response <= octaveLayer[layer].GetResponse((x + neighborhood[u]), (y + neighborhood[v]))
										&& !(neighborhood[u] == 0 && neighborhood[v] == 0 && neighborhood[w] == 0)) {
										found = false;
										break failed;
									}
								}
						if (found)
						{
							result.add(new InterestPoint(x, y, octaveLayer[j]
									.GetScale(), response));
							m_max = Math.max(m_max, response);
							
						}
					}
			}
		}
		return result;
	}

	void CreateDescriptor(ArrayList<InterestPoint> interestPoints) {

		for (int i = 0; i < interestPoints.size(); i++) {
			
			InterestPoint ip = interestPoints.get(i);
			// ip.x = 90;
			// ip.y = 90;
			float radius = ip.scale * 6;
			int Left = (int) Math.round(ip.x - radius);
			int Right = (int) Math.round(ip.x + radius);
			int Top = (int) Math.round(ip.y - radius);
			int Bottom = (int) Math.round(ip.y + radius);
			double radius2 = radius * radius;

			BoxFilter xWavelet = BoxFilter.GetWaveletX(ip.scale * 4);
			BoxFilter yWavelet = BoxFilter.GetWaveletY(ip.scale * 4);

			float xResponse = 0;
			float yResponse = 0;
			
			double[][] gaussian = Matrix.get2DGaussianKernel((int) Math.round(radius+1), 2 * ip.scale);

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

						int idxX = Math.abs(j - ip.y);
						int idxY = Math.abs(k - ip.x);
						xResponse *= gaussian[idxX][idxY];
						yResponse *= gaussian[idxX][idxY];

						if (xResponse != 0 || yResponse != 0)
							angles.add(new Point2D.Float(xResponse, yResponse));
						/*
						 * System.out.println("xResponse: " + xResponse +
						 * "yResponse: " + yResponse);
						 * 
						 * 
						 * float ang = (float) (Math.atan2(yResponse,
						 * xResponse)); if(j < 0 || k < 0 || j>=
						 * m_image.GetHeight() || k >= m_image.GetWidth())
						 * continue; int posTmp = j * m_image.GetWidth() + k;
						 * int response = (int) xResponse +128;
						 * imagePixels[posTmp] = 0xFF000000 | (response << 16) |
						 * (response << 8) | response;
						 */
					}
				}
			}

			// calculate the dominant direction
			float sumX = 0.f, sumY = 0.f;
			float max = 0.f, orientation = 0.f;
			float ang1 = 0.f, ang2 = 0.f;
			float pi2 = (float) (2 * Math.PI);
			float pi3 = (float) (Math.PI / 3.0);

			// loop slides pi/3 window around feature point
			for (ang1 = 0; ang1 < pi2; ang1 += 0.15f) {

				ang2 = ang1 + pi3;

				if (ang2 > pi2)
					ang2 -= (pi2);
				// ang2 = (float) (ang1 + Math.PI / 3.0f > Math.PI ? -(ang1
				// + Math.PI / 3.0f - Math.PI) : ang1 + Math.PI / 3.0f);
				sumX = sumY = 0.f;
				for (int k = 0; k < angles.size(); k++) {
					Point2D.Float angle = angles.get(k);
					// get angle from the x-axis of the sample point
					float ang = (float) (Math.atan2(angle.y, angle.x));

					if (ang < 0)
						ang += pi2;

					// determine whether the point is within the window
					if (ang1 < ang && ang2 > ang) {
						sumX += angle.x;
						sumY += angle.y;
					} else if (ang2 < ang1 && (0 > ang && ang < ang2)
							|| (ang1 < ang && ang < 2 * Math.PI)) {
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
					if (orientation < 0)
						orientation += pi2;
				}
			}

			// assign orientation of the dominant response vector
			ip.orientation = orientation;

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
