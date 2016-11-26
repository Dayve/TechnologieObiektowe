package sciCon.model;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

public class NetworkConnection {

static BufferedReader in = null;
static PrintWriter out = null;
static ObjectInputStream objIn = null;
static ObjectOutputStream objOut = null;
static Socket s;

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
	
	public static void sendMessage(String data) {
		out.println(data);
	}
	
	public static void pron() {
		System.out.println("hy");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ho");
	}
	
	public static void sendObject(Object o) {
		try {
			if(o != null)
			{
				objOut.writeObject(o);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void closeConnection() {
		try {
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void connectToServer() throws IOException
	{
		try {
			int port = 8080;
			String server = "localhost";
			Socket s = new Socket(server, port);
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
	        out = new PrintWriter(s.getOutputStream(), true);
	        objIn = new ObjectInputStream(s.getInputStream());
			objOut = new ObjectOutputStream(s.getOutputStream());
//	        String answer = in.readLine();
//	        out.println("Hello, just connected.");
//	        System.out.println(answer);
		} catch(IOException e) {
			e.printStackTrace();
			System.out.println("Data for socket not found.");
		}
	}

//	@Override
//	public void run() {
//		try {
//			connectToServer();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
