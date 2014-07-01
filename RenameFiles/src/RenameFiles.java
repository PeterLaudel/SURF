import java.io.File;


public class RenameFiles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		File folderSrc = new File("D:\\HTW Berlin\\4. Semester\\IC\\workspace\\IR_SURF\\images\\holiday_320");
		File folderDst = new File("D:\\HTW Berlin\\4. Semester\\IC\\workspace\\IR_SURF\\images\\holiday_big");
		
		File[] filesSrc = folderSrc.listFiles();
		File[] filesDst = folderDst.listFiles();
		
		for(int i = 0; i < filesSrc.length; i++)
		{
			File newFile = new File("D:\\HTW Berlin\\4. Semester\\IC\\workspace\\IR_SURF\\images\\holiday_big\\" + filesSrc[i].getName());
			filesDst[i].renameTo(newFile);
		}

	}

}
