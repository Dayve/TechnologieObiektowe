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

	public static void sendSocketEvent(SocketEvent se) {
		try {
			if(se != null)
			{
				objOut.writeObject(se);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static SocketEvent rcvSocketEvent() {
		SocketEvent se = null;
		try {
			se = (SocketEvent) objIn.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return se;
	}
	
	public static void connect(String address, int port) {
		try {
			s = new Socket(address, port);
			objOut = new ObjectOutputStream(s.getOutputStream());
			objIn = new ObjectInputStream(s.getInputStream());
		} catch(ConnectException e) {
			System.out.println("Server is not responding!");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} NetworkConnection.connect(address, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
