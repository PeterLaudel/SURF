package Sorter;

import app.Sorter;
import Imageprocess.Image;
import IntegralImage.IntegralImage;
import PicPropertys.Pic;
import PicPropertys.PicSurf;
import SURF.SurfFeatureDescriptor;
import SURF.SurfFeatureDetector;

public class Sorter_SURF implements Sorter {
	
	PicSurf[] m_picSurf;

	public Sorter_SURF(Pic[] pics) {
		// TODO Auto-generated constructor stub
		
		m_picSurf = new PicSurf[pics.length];
		for(int i = 0; i < m_picSurf.length; i++)
		{
			m_picSurf[i] = new PicSurf(pics[i]);
		}
		getFeatureVectors();
	}

	@Override
	public void getFeatureVectors() {
		SurfFeatureDetector sfd = new SurfFeatureDetector(200, 4);
		SurfFeatureDescriptor sfdesc = new SurfFeatureDescriptor();
		for(int i = 0; i < m_picSurf.length; i++)
		{
			PicSurf surfpic = m_picSurf[i];
			
			Image image = new Image(surfpic.bImage.getWidth(), surfpic.bImage.getHeight());
			surfpic.bImage.getRGB(0, 0, image.GetWidth(), image.GetHeight(), image.GetImagePixels(), 0, image.GetWidth());
			IntegralImage ii = new IntegralImage(image);
			sfd.Detect(ii, image.GetWidth(), image.GetHeight(), surfpic.interestPoints);
			sfdesc.Compute(ii, surfpic.interestPoints);
		}
	}

	@Override
	public void sortBySimilarity() {
		// TODO Auto-generated method stub
		
	}


}
