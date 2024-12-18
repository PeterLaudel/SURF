package app;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MatchBinaryFile {
	
	String m_path;
	String m_filename;
	Element m_rootElement;
	Document m_document;
	
	public MatchBinaryFile(String path, String filename) {
		// TODO Auto-generated constructor stub
		m_path = path;
		m_filename = filename;

	}
	
	public Map<Integer, Map<Integer, Float>> ReadMatches()
	{
		Map<Integer, Map<Integer, Float>> resultMap  = new HashMap<Integer, Map<Integer, Float>>();
		try
		{
			File file = new File(m_path + "/" + m_filename);
			if(!file.exists())
				return resultMap;
			
			DataInputStream is = new DataInputStream(new FileInputStream(file));
			int size  = is.readInt();
			resultMap = new HashMap<Integer, Map<Integer, Float>>(size);
			while(is.available() != 0)
			{
				int id = is.readInt();
				Map<Integer, Float> distanceMap = new HashMap<Integer, Float>();
				for(int i = 0; i < size; i++)
				{
					int id2 = is.readInt();
					float distance = is.readFloat();
					distanceMap.put(id2, distance);
				}
				resultMap.put(id, distanceMap);
			}
			is.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		return resultMap;
	}
	
	
	/**
	 * Method for writing matches in a binary file. Give the function
	 * a Map with the name hashId as key.
	 * @param matches map which contains all the matches.
	 */
	public void WriteMatches(Map<Integer, Map<Integer, Float>> matches)
	{
		try {
			File file = new File(m_path + "/" + m_filename);
			if(!file.exists())
				file.createNewFile();
			
			DataOutputStream os = new DataOutputStream(new FileOutputStream(file));
			
			int size = matches.entrySet().iterator().next().getValue().size();
			
			os.writeInt(size);
			
			Iterator<Entry<Integer, Map<Integer, Float>>> it = matches.entrySet().iterator();
			
		
			while(it.hasNext())
			{
				
				Map.Entry<Integer, Map<Integer, Float>> entry = it.next();
				int id = entry.getKey();
				os.writeInt(id);
				
		 
				Iterator<Entry<Integer, Float>> it2 = entry.getValue().entrySet().iterator();
				
				while(it2.hasNext())
				{
					Map.Entry<Integer, Float> distance = it2.next();
					int id2 = distance.getKey();
					os.writeInt(id2);
					float dist = distance.getValue();
					os.writeFloat(dist);
					
				}
			}
			os.close();
		  } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
