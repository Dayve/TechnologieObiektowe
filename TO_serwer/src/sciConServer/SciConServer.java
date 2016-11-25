package sciConServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class SciConServer implements Runnable {
	private ServerSocket listener;
	private int countClientConnections;
	private int port;

	public SciConServer(int p) {
		port = p;
	}

	@Override
	public void run() {
		try {
			countClientConnections = 1;
			startServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args0) {
		
	}

	private void startServer() throws IOException {
		listener = new ServerSocket(port);
		try {
			while (true) {
				// serverLogs.setText("Starting Server...\n");
				// AdviceServer s = new
				// AdviceServer(Integer.parseInt(txtPort.getText()));
				//
				System.out.println("Starting server...");
				SomeServerImplementation ssi = new SomeServerImplementation(listener.accept(),
						countClientConnections++);
				// ManageAdviceServer.serverLogs.append("New Client: " +
				// (countClientConnections - 1) + " connected...\n");
				Thread thread = new Thread(ssi);
				thread.start();
				System.out.println("Server started.");
			}
		} finally {
			listener.close();
		}
	}

	private class SomeServerImplementation implements Runnable {
		private Socket s;
		int counter;
		private BufferedReader in;
		private PrintWriter out;

		public SomeServerImplementation(Socket socket, int executions) {
			s = socket;
			counter = executions;
		}

		@Override
		public void run() {
			try {
				out = new PrintWriter(s.getOutputStream(), true);
				out.println("Execution Count: " + counter + " executions");
				counter++;

				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				while (true) {
					System.out.println(in.readLine());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					s.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
