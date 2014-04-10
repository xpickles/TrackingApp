import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Date;
import java.awt.*;

import javax.swing.*;

import java.sql.*;

public class Server extends JFrame {
	private JTextArea jta = new JTextArea();
	private int clientNo;  // Keeps track of clients coming into server
	private int port = 8081; // Allows for quick access to port number for changing
	private PreparedStatement regPstmt; // Accessing database for registering
	private PreparedStatement signPstmt; // Accessing database for signing in
	private PreparedStatement reqFPstmt; // Accessing database for requesting friend
	private PreparedStatement reqLPstmt; // Accessing database for requesting location
	private PreparedStatement delPstmt; // Accessing database for deleting friend
	private PreparedStatement resLPstmt; // Accessing database for responding to location
	private PreparedStatement resFPstmt; // Accessing database for responding to friend 

	public static void main(String[] args) {
		new Server();
	}

	public Server() {
		
		initializeDB();
		
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
			
			// Logs information starting server
			jta.append("Server started at " + new Date() + "\n");
			
			clientNo = 1;

			// Accepts socket traffic and creates a new thread for the client
			while(true) {
				
				// Logs the time the client connects to the host
				Socket socket = serverSocket.accept();
				jta.append("Starting thread for client " + clientNo + 
						" at " + new Date() + "\n");

				// Logs client Host Name and IP address and prints Host name
				// to the server's output
				InetAddress inetAddress = socket.getInetAddress();
				jta.append("Client " + clientNo + "'s host name is "
						+ inetAddress.getHostName() + "\n");

				// Prints client IP address to the server's output
				jta.append("Client " + clientNo + "'s IP Address is "
						+ inetAddress.getHostAddress() + "\n");

				// Creates a thread to handle the client requests
				HandleAClient task = new HandleAClient(socket);

				// Starts the new client thread
				new Thread(task).start();

				// Increases client number to be ready for next client
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
				
				// Creates stream for data from the client
				DataInputStream inputFromClient = new DataInputStream(
						socket.getInputStream());
				
				// Creates stream for data back to the client
				DataOutputStream outputToClient = new DataOutputStream(
						socket.getOutputStream());

				int choice = inputFromClient.readInt();
				
				switch (choice) {
					case 1:  // Register User
						
						// Reads users information from client
						String screenname = inputFromClient.readUTF();
						String password = inputFromClient.readUTF();
						int birth_year = inputFromClient.readInt();
						String firstName = inputFromClient.readUTF();
						String lastName = inputFromClient.readUTF();
						
						try {
							
							// Adds values to prepared statements
							regPstmt.setString(1,  screenname);
							regPstmt.setString(2,  password);
							regPstmt.setInt(3, birth_year);
							regPstmt.setString(4,  firstName);
							regPstmt.setString(5,  lastName);
							
							// Executes query
							ResultSet rset = regPstmt.executeQuery();
							
							jta.append("User " + screenname + " added to database");
							
						} catch (SQLException ex) {
							jta.append("Error in registering User " + screenname);
						}
						break;
					case 2:  // SignIn User
						
						break;
					case 3:  // Request Friend
						
						break;
					case 4:  // Request Location
						
						break;
					case 5:  // Delete Friend
						
						break;
					case 6:  // Respond to Location Request
						
						break;
					case 7:  // Respond to Friend Request
						
						break;
					case 8:  // Get Friends
						
						break;
					case 9:  // Get Notifications
						
						break;
					case 10:  // Send Location
						
						break;
				}
				
				
			}
			catch(IOException e) {
				System.err.println(e);
			}
		}
	}
	
	public void initializeDB() {
		try {
			// Load the driver for postgresql
			Class.forName("org.postgresql.Driver");
			System.out.println("Driver loaded");
			
			// Establish connection for database
			Connection connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost/trackingproto", "postgres", "postgres");
			System.out.println("Database connected");
			
			String registerString = "INSERT INTO user VALUES (?, ?, ?, ?, ?, NULL, NULL) ";
			regPstmt = connection.prepareStatement(registerString);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
