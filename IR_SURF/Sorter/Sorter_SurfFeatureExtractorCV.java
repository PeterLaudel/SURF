package Sorter;

import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

import Features.InterestPoint;
import PicPropertys.Pic;
import PicPropertys.PicSurf;
import app.Sorter;
import app.SurfBinaryFile;

public class Sorter_SurfFeatureExtractorCV implements Sorter{

	PicSurf[] m_picSurf;
	String m_path;
	SurfBinaryFile m_xmlFile;
	int m_count;

	public Sorter_SurfFeatureExtractorCV(Pic[] pics, String path, int count) {
		// TODO Auto-generated method stub
		m_path = path;
		m_picSurf = new PicSurf[pics.length];
		m_xmlFile = new SurfBinaryFile(m_path, "descriptorCV");
		m_count = count;
		for(int i = 0; i < m_picSurf.length; i++)
		{
			m_picSurf[i] = new PicSurf(pics[i]);
		}
		getFeatureVectors();
		
	}

	@Override
	public void sortBySimilarity() {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void computeDistance(int queryPic, int actPic) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getFeatureVectors() {
		// TODO Auto-generated method stub
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		FeatureDetector sfd = FeatureDetector.create(FeatureDetector.SURF);
		DescriptorExtractor de = DescriptorExtractor.create(DescriptorExtractor.SURF);
		
		
		
		for(int i = 0; i < m_picSurf.length; i++)
		{
			try {
				PicSurf surfpic = m_picSurf[i];
				//BufferedImage img = ImageIO.read(new File(m_path + "/" + surfpic.pic.name));
				Mat image = Highgui.imread(m_path +"/" + surfpic.pic.name, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
				MatOfKeyPoint keypoints = new MatOfKeyPoint();
				sfd.detect(image, keypoints);
				System.out.println("Name: " +  surfpic.pic.name);
				System.out.println("Detected: " + keypoints.rows());
				KeyPoint[] keyPointArray = keypoints.toArray();
				KeyPoint[] keyPoints = Arrays.copyOfRange(keyPointArray, 0, Math.min(m_count, keyPointArray.length));
				
				keypoints = new MatOfKeyPoint(keyPoints);
				System.out.println("Keypoints: " + keypoints.rows());
				Mat descriptors = new Mat();
				de.compute(image, keypoints, descriptors);
				KeyPoint[] keypointsArray = keypoints.toArray();
				
				int count = 0;
				for(int k = 0; k < descriptors.rows();k++)
				{
					KeyPoint kp = keypointsArray[k];
					Point pt = kp.pt;
					InterestPoint ip = new InterestPoint((int) pt.x, (int) pt.y, 0.0f, kp.response, (kp.response < 0));
					ip.orientation = kp.angle;
					ip.descriptor = new float[descriptors.cols()];
					for(int j = 0; j < descriptors.cols(); j++)
					{
						ip.descriptor[j] = (float) descriptors.get(k, j)[0];
					}
					surfpic.interestPoints.add(ip);
					count++;
				}
				System.out.println("Result: " + count + "/" + descriptors.rows() + "\n\n");
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		m_xmlFile.WriteSurfBinaryFile(m_picSurf);
		
	}

}
