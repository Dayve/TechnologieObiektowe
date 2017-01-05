package sciCon.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Paper implements Serializable {
	
	private static final long serialVersionUID = 3578902895952322210L;
	private ByteBuffer buffer = null;
	
	public byte[] getAsByteArray() {
		return buffer.array();
	}
	
	public void createFromReceivedBytes(byte[] receivedBytes) {
		buffer = ByteBuffer.allocate(receivedBytes.length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(receivedBytes);
	}
	
	public void createFromExistingFile(String pathWithFilename) {
		File file = new File(pathWithFilename);
		String filename = file.getName();
		byte[] filenameStringAsBytes = filename.getBytes();
		
		int size = new Long(file.length()).intValue();	
		byte[] fileBytes = new byte[size];

		buffer = ByteBuffer.allocate(size + Integer.BYTES*2 + filenameStringAsBytes.length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		try {
			InputStream in = new FileInputStream(file);
			in.read(fileBytes);
			in.close();
		}
		catch (FileNotFoundException fileNotFound) {
			fileNotFound.printStackTrace();
		}
		catch (IOException ioException) {
			ioException.printStackTrace();
		}
		
		buffer.putInt(fileBytes.length);
		buffer.putInt(filename.length());
		buffer.put(fileBytes);
		buffer.put(filenameStringAsBytes);
	}
	
	public void saveAsFile(String path) {
		try {
			int howManyBytes = buffer.getInt(0);
			int filenameLength = buffer.getInt(Integer.BYTES);
			byte[] filenameBytes = new byte[filenameLength];
			byte[] fileBytes = new byte[howManyBytes];
			
			for(int i=0 ; i<howManyBytes ; ++i) {
				fileBytes[i] = buffer.get(i + Integer.BYTES*2);
			}
			
			for(int i=0 ; i<filenameLength ; ++i) {
				filenameBytes[i] = buffer.get(i + Integer.BYTES*2 + howManyBytes);
			}
			
			String filename = new String(filenameBytes);
			
			OutputStream out = new FileOutputStream(path + filename);
			out.write(fileBytes, 0, howManyBytes);
			out.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}