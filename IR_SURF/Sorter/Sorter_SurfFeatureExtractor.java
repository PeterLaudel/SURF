package Sorter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import Imageprocess.Image;
import IntegralImage.IntegralImage;
import PicPropertys.Pic;
import PicPropertys.PicSurf;
import SURF.SurfFeatureDescriptor;
import SURF.SurfFeatureDetector;
import app.Sorter;
import app.SurfBinaryFile;

public class Sorter_SurfFeatureExtractor implements Sorter {
	
	PicSurf[] m_picSurf;
	String m_path;
	SurfBinaryFile m_xmlFile;
	int m_count;
	
	public Sorter_SurfFeatureExtractor(Pic[] pics, String path, int count) {
		// TODO Auto-generated constructor stub
		
		m_path = path;
		m_picSurf = new PicSurf[pics.length];
		m_xmlFile = new SurfBinaryFile(m_path);
		m_count = count;
		for(int i = 0; i < m_picSurf.length; i++)
		{
			m_picSurf[i] = new PicSurf(pics[i]);
		}
		getFeatureVectors();
	}
	
	public Sorter_SurfFeatureExtractor(Pic[] pics, String path) {
		// TODO Auto-generated constructor stub
		
		m_path = path;
		m_picSurf = new PicSurf[pics.length];
		m_xmlFile = new SurfBinaryFile(m_path);
		m_count = -1;
		for(int i = 0; i < m_picSurf.length; i++)
		{
			m_picSurf[i] = new PicSurf(pics[i]);
		}
		getFeatureVectors();
	}


	@Override
	public void getFeatureVectors() {
		
		for(int i = 0; i < m_picSurf.length; i++)
		{
			try {
				PicSurf surfpic = m_picSurf[i];
				SurfFeatureDetector sfd;
				if(m_count != -1)
					sfd = new SurfFeatureDetector(m_count, 4);
				else
					sfd = new SurfFeatureDetector(4);
				
				
				
				SurfFeatureDescriptor sfdesc = new SurfFeatureDescriptor();
				BufferedImage img = ImageIO.read(new File(m_path + "/" + surfpic.pic.name));
				Image image = new Image(img.getWidth(), img.getHeight());
				img.getRGB(0, 0, image.GetWidth(), image.GetHeight(), image.GetImagePixels(), 0, image.GetWidth());
				IntegralImage ii = new IntegralImage(image);
				sfd.Detect(ii, image.GetWidth(), image.GetHeight(), surfpic.interestPoints);
				sfdesc.Compute(ii, surfpic.interestPoints);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		m_xmlFile.WriteSurfBinaryFile(m_picSurf);
	}

	@Override
	public void sortBySimilarity() {	
	}

	@Override
	public void computeDistance(int queryPic, int actPic) {
	}


}
