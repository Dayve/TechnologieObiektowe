package sciCon.model;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkConnection {

//static BufferedReader in = null;
//static PrintWriter out = null;
//static Socket s = null;

static BufferedReader in = null;
static PrintWriter out = null;
static Socket s;

//	public NetworkConnection() {
//		try {
//			s = new Socket("localhost", 8080);
//		} catch(ConnectException e) {
//			System.out.println("Connection refused!");
//			e.printStackTrace();
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	public static void sendMessage(String data) {
		out.println(data);
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
		try{
			int port = 8080;
			String server = "localhost";
			Socket s = new Socket(server, port);
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
	        out = new PrintWriter(s.getOutputStream(), true);
	        String answer = in.readLine();
	        out.println("Hello, just connected.");
	        System.out.println(answer);
		} catch(UnknownHostException e) {
			System.out.println("IP not found.");
		} catch(IOException e) {
			System.out.println("Data for socket not found.");
		}
	}
}
