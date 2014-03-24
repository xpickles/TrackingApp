package com.deitel.routetracker;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

public class Server extends JFrame {
	private JTextArea jta = new JTextArea();
	private int clientNo;
	private int port = 8000;

	public static void main(String[] args) {
		new Server();
	}

	public Server() {
		// Creates GUI for server
		setLayout(new BorderLayout());
		add(new JScrollPane(jta), BorderLayout.CENTER);

		setTitle("Tracker Server");
		setSize(500, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		try {
			// Opens a socket to access the port
			ServerSocket serverSocket = new ServerSocket(port);
			jta.append("Server started at " + new Date() + "\n");

			clientNo = 1;

			// Accepts socket traffic and creates a new thread for the client
			while(true) {
				Socket socket = serverSocket.accept();

				jta.append("Starting thread for client " + clientNo +
						" at " + new Date() + "\n");

				InetAddress inetAddress = socket.getInetAddress();
				jta.append("Client " + clientNo + "'s host name is "
						+ inetAddress.getHostName() + "\n");
				jta.append("Client " + clientNo + "'s IP Address is "
						+ inetAddress.getHostAddress() + "\n");

				HandleAClient task = new HandleAClient(socket);

				new Thread(task).start();

				clientNo++;
			}
		}
		catch (IOException ex){
			System.err.println(ex);
		}
	}

	// Thread creation and implementation
	class HandleAClient implements Runnable {
		private Socket socket;

		public HandleAClient(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				DataInputStream inputFromClient = new DataInputStream(
						socket.getInputStream());
				DataOutputStream outputToClient = new DataOutputStream(
						socket.getOutputStream());

				while (true) {
					//Enter what you want the server to do
				}
			}
			catch(IOException e) {
				System.err.println(e);
			}
		}
	}
}

