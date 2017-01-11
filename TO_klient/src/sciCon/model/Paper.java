package sciCon.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Paper implements Serializable {	
	
	private static final long serialVersionUID = 3578902895952322210L;
	
	/* Buffer structure:
	 *  1) Size of fileInfo			(int, 4 bytes)
	 *  2) File size				(int, 4 bytes)
	 *  
	 *  3) fileInfo as bytes		(byte[], fileInfoSize bytes)
	 *  4) Actual file contents		(byte[], fileSize bytes)
	 */
	
	private ByteBuffer buffer = null;
	
	private int fileSize; // It's the size (number of bytes) of raw file data, not the whole buffer
	private int rawDataOffset; // The offset inside the buffer
	
	private int fileInfoSize;
	private int rawFileInfoOffset;
	
	public FileInfo fileInfo;
	
	
	public byte[] getWholeBufferAsByteArray() {
		return buffer.array();
	}
	
	
	public byte[] getRawFileData() {
		byte[] fileBytes = new byte[fileSize];
		
		for(int i=0 ; i<fileSize ; ++i) {
			fileBytes[i] = buffer.get(rawDataOffset+i);
		}
		
		return fileBytes;
	}
	
	
	public byte[] getRawFileInfo() {
		byte[] fileInfoBytes = new byte[fileInfoSize];
		
		for(int i=0 ; i<fileInfoSize ; ++i) {
			fileInfoBytes[i] = buffer.get(rawFileInfoOffset+i);
		}
		
		return fileInfoBytes;
	}

	
	public void createFromReceivedBytes(byte[] receivedBytes) {
		buffer = ByteBuffer.allocate(receivedBytes.length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(receivedBytes);
		
		int fetchPointer = 0;
		
		// Fetch sizes:
		fileInfoSize = buffer.getInt(fetchPointer);
		fetchPointer += Integer.BYTES;
		
		fileSize = buffer.getInt(fetchPointer);
		fetchPointer += Integer.BYTES;
		
		// Fetch file info:
		byte[] fileInfoAsBytes = new byte[fileInfoSize];
		
		rawFileInfoOffset = fetchPointer;
		
		for(int i=0 ; i<fileInfoSize ; ++i, ++fetchPointer) {
			fileInfoAsBytes[i] = buffer.get(fetchPointer);
		}
		
		try {
			fileInfo = (FileInfo) deserialize(fileInfoAsBytes);
			System.out.println("Received file metadata:\n" + fileInfo);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Received file size:\n" + fileSize + " bytes");
		
		// Enable fetching the actual file contents:
		rawDataOffset = fetchPointer;
	}
	
	
	public void createFromExistingFile(String pathWithFilename, User author, int givenConferenceId, String desc) {
		File file = new File(pathWithFilename);		
		fileInfo = new FileInfo(file.getName(), desc, author, givenConferenceId);
		System.out.println("Sent file metadata:\n" + fileInfo);
		
		
		byte[] fileInfoAsBytes = null;
		
		try {
			fileInfoAsBytes = serialize(fileInfo);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		fileSize = new Long(file.length()).intValue(); // Up to 2,14 GB
		byte[] fileBytes = new byte[fileSize];
		System.out.println("Sent file size:\n" + fileSize + " bytes");
		fileInfoSize = fileInfoAsBytes.length;
		
		buffer = ByteBuffer.allocate(Integer.BYTES*2 + fileInfoSize + fileSize + 8);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		rawDataOffset = Integer.BYTES*2 + fileInfoSize;
		rawFileInfoOffset = Integer.BYTES*2;
				
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

		buffer.putInt(fileInfoSize);
		buffer.putInt(fileSize);
		buffer.put(fileInfoAsBytes);
		buffer.put(fileBytes);
	}
	
	
	public void saveAsFile(String path) {
		// Add forwardslash if needed:
		if(path.charAt(path.length() - 1) != '/') path += "/";
		
		try {			
			OutputStream out = new FileOutputStream(path + fileInfo.getFilename());
			out.write(getRawFileData(), 0, fileSize);
			out.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
    public static byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    
    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return o.readObject();
            }
        }
    }
}