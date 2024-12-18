package app;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Features.InterestPoint;
import PicPropertys.PicSurf;

public class SurfBinaryFile {
	
	String m_path;
	String m_name;

	public SurfBinaryFile(String path, String name) {
		// TODO Auto-generated constructor stub
		m_path = path;
		m_name = name;
	}
	public Map<Integer, List<InterestPoint>> ReadSurfBinaryFile(int countIP)
	{
		try {
			 
			File file = new File(m_path + "/" + m_name + ".surf");
				if(!file.exists())
					return null;
				
				
				DataInputStream is = new DataInputStream(new FileInputStream(file));
				
				int size = is.readInt();
				Map<Integer, List<InterestPoint>> resultMap = new HashMap<Integer, List<InterestPoint>>(size);
				
				for(int i = 0; i < size; i++)
				{
					int id = is.readInt();
					
					int count = is.readInt();
					List<InterestPoint> interestPoints = new ArrayList<InterestPoint>(countIP);
					//is.skipBytes(4);
					int offset = Math.max(0, count - countIP);
					int skip = (4 + 4 + 4 + 1 + 4 + 4 + 4 + (64 * 4)) * offset;
					is.skipBytes(skip);
					int end = Math.min(count, countIP);
					for(int j = 0; j < end; j++)
					{
						int x = is.readInt();
						int y = is.readInt();
						float orientation = is.readFloat();
						Boolean negative = is.readBoolean();
						float scale = is.readFloat();
						float value = is.readFloat();
						
						
						int descriptorLength = is.readInt();
						ByteBuffer buffer = ByteBuffer.allocate(4 * descriptorLength);
						is.read(buffer.array());
						float[] descriptor = new float[descriptorLength];
						buffer.asFloatBuffer().get(descriptor);
						
						InterestPoint ip = new InterestPoint(x, y, scale, value, negative);
						ip.orientation = orientation;
						ip.descriptor = descriptor;
						interestPoints.add(ip);
							
					}
					resultMap.put(id, interestPoints);
				}
				is.close();
				return resultMap;
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		
		return null;
		
	}
	
	public void WriteSurfBinaryFile(Map<Integer, List<InterestPoint>> surf)
	{
		try {
			
			File file = new File(m_path + "/" + m_name + ".surf");
			if(!file.exists())
				file.createNewFile();
			
			DataOutputStream os = new DataOutputStream(new FileOutputStream(file));
			os.writeInt(surf.size());
			Iterator<Entry<Integer, List<InterestPoint>>> it = surf.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry<Integer, List<InterestPoint>> entry = it.next();
				int id = entry.getKey();
				os.writeInt(id);
		 
				
				List<InterestPoint> interestPoints = entry.getValue();
				int ipCount = interestPoints.size();
				os.writeInt(ipCount);
				
				for(int j = 0; j < ipCount; j++)
				{
					InterestPoint ip = interestPoints.get(j);
					
					os.writeInt(ip.x);
					os.writeInt(ip.y);
					os.writeFloat(ip.orientation);
					os.writeBoolean(ip.negative);
					os.writeFloat(ip.scale);
					os.writeFloat(ip.value);
					os.writeInt(ip.descriptor.length);
					ByteBuffer buffer = ByteBuffer.allocate(4 * ip.descriptor.length);
					
			        for (float value : ip.descriptor){
			            buffer.putFloat(value);
			        }
					os.write(buffer.array());
				}
			}
			os.close();
		  } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void WriteSurfBinaryFile(PicSurf []surfPics)
	{
		try {
				
				File file = new File(m_path + "/" + m_name + ".surf");
				if(!file.exists())
					file.createNewFile();
				
				DataOutputStream os = new DataOutputStream(new FileOutputStream(file));
				os.writeInt(surfPics.length);
			
				for(int i  = 0; i < surfPics.length; i++)
				{
					int id = surfPics[i].pic.name.hashCode();
					os.writeInt(id);
			 
					
					List<InterestPoint> interestPoints = surfPics[i].interestPoints;
					int ipCount = interestPoints.size();
					os.writeInt(ipCount);
					
					for(int j = 0; j < ipCount; j++)
					{
						InterestPoint ip = interestPoints.get(j);
						
						os.writeInt(ip.x);
						os.writeInt(ip.y);
						os.writeFloat(ip.orientation);
						os.writeBoolean(ip.negative);
						os.writeFloat(ip.scale);
						os.writeFloat(ip.value);
						os.writeInt(ip.descriptor.length);
						ByteBuffer buffer = ByteBuffer.allocate(4 * ip.descriptor.length);
						
				        for (float value : ip.descriptor){
				            buffer.putFloat(value);
				        }
						os.write(buffer.array());
					}
				}
				os.close();
			  } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
	
	
	

}
