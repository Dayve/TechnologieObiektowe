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
	
	public String filename;
	public int authorsId;
	public int targetConferenceId;
	
	private int fileSize;
	
	
	public byte[] getAsByteArray() {
		return buffer.array();
	}
	
	public byte[] getRawFileData() {
		byte[] fileBytes = new byte[fileSize];
		
		for(int i=0 ; i<fileSize ; ++i) {
			fileBytes[i] = buffer.get(i + Integer.BYTES*4);
		}
		
		return fileBytes;
	}
	
	public void createFromReceivedBytes(byte[] receivedBytes) {
		buffer = ByteBuffer.allocate(receivedBytes.length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(receivedBytes);
		
		fileSize = buffer.getInt(0);
		int fileNameLength = buffer.getInt(Integer.BYTES);
		authorsId = buffer.getInt(2*Integer.BYTES);
		targetConferenceId = buffer.getInt(3*Integer.BYTES);
		
		byte[] fileNameBytes = new byte[fileNameLength];

		for(int i=0 ; i<fileNameLength ; ++i) {
			fileNameBytes[i] = buffer.get(i + Integer.BYTES*4 + fileSize);
		}
		
		filename = new String(fileNameBytes);
	}
	
	public void createFromExistingFile(String pathWithFilename, int givenUserId, int givenConferenceId) {
		this.authorsId = givenUserId;
		this.targetConferenceId = givenConferenceId;
		
		File file = new File(pathWithFilename);
		filename = file.getName();
		fileSize = new Long(file.length()).intValue();
		
		byte[] fileNameAsBytes = filename.getBytes();
		byte[] fileBytes = new byte[fileSize];

		/* Byte array structure:
		 * Constant size fields:
		 *  1) fileSize (int, 4 bytes (we use Integer.BYTES, but it's equal to 4))
		 *  2) fileNameAsBytes.length (int, 4 bytes)
		 *  3) authorsId (int, 4 bytes)
		 *  4) targetConferenceId (int, 4 bytes)
		 * Variable size fields:
		 *  5) actual file contents (byte[], the rest of the array)
		 *  6) fileNameAsBytes (byte[], fileNameAsBytes.length bytes)
		 */
		buffer = ByteBuffer.allocate(Integer.BYTES*4 + fileSize + fileNameAsBytes.length);
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
		buffer.putInt(fileNameAsBytes.length);
		buffer.putInt(authorsId);
		buffer.putInt(targetConferenceId);
		
		buffer.put(fileBytes);
		buffer.put(fileNameAsBytes);
	}
	
	public void saveAsFile(String path) {
		try {			
			OutputStream out = new FileOutputStream(path + filename);
			out.write(getRawFileData(), 0, fileSize);
			out.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}