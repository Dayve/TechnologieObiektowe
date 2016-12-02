package sciCon.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class NetworkConnection {

private static ObjectInputStream objIn = null;
private static ObjectOutputStream objOut = null;
private static Socket s = null;
	
	public NetworkConnection() {
		try {
			s = new Socket("localhost", 8080);
			objOut = new ObjectOutputStream(s.getOutputStream());
		} catch(ConnectException e) {
			System.out.println("Connection refused!");
			e.printStackTrace();
		}
		
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendObject(Object o) {
		try {
			if(o != null)
			{
				objOut.writeObject(o);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Object getObject() {
		
		Object o = null;
		try {
			System.out.println("procedura odbioru (s -> c)");
			o = objIn.readObject();
			System.out.println("wypisujê obiekt (s -> c)");
			System.out.println(o);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	public static void closeConnection() {
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void connectToServer() throws IOException
	{
		if(s == null) {
			try {
				int port = 8080;
				String server = "localhost";
				Socket s = new Socket(server, port);
		        objIn = new ObjectInputStream(s.getInputStream());
				objOut = new ObjectOutputStream(s.getOutputStream());
			} catch(IOException e) {
				e.printStackTrace();
				System.out.println("Data for socket not found.");
			}
		}
		
	}
}
