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
	private Connection connection; // Setting up the connection for the database

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
		private PreparedStatement genPstmt; // Generic statement for accessing database
		private DataInputStream inputFromClient;
		private DataOutputStream outputToClient;
		private String screenname;
		private String password;
		private int birth_year;
		private String firstName;
		private String lastName;
		private String friendScreenname;
		private int outputInt;
		
		public HandleAClient(Socket socket) {
			this.socket = socket;
		}
		
		public void run() {
			
			try {
				
				// Creates stream for data from the client
				inputFromClient = new DataInputStream(
						socket.getInputStream());
				
				// Creates stream for data back to the client
				outputToClient = new DataOutputStream(
						socket.getOutputStream());

				int choice = inputFromClient.readInt();
				
				switch (choice) {
					case 1:  // Register User
						
						// Reads users information from client
						screenname = inputFromClient.readUTF();
						password = inputFromClient.readUTF();
						birth_year = inputFromClient.readInt();
						firstName = inputFromClient.readUTF();
						lastName = inputFromClient.readUTF();
						
						// Register User
						registerUser();
						break;
					case 2:  // SignIn User
						
						//Reads users information from client
						screenname = inputFromClient.readUTF();
						password = inputFromClient.readUTF();
						
						//SignIn User
						signInUser();
						
						break;
					case 3:  // Request Friend
						
						// Reads users information from client
						screenname = inputFromClient.readUTF();
						friendScreenname = inputFromClient.readUTF();
						
						// Delete Friend
						requestFriend();
						
						break;
					case 4:  // Request Location
						//TODO
						break;
					case 5:  // Delete Friend
						
						// Reads users information from client
						screenname = inputFromClient.readUTF();
						friendScreenname = inputFromClient.readUTF();
						
						// Delete Friend
						deleteFriend();
						
						break;
					case 6:  // Respond to Location Request
						//TODO
						break;
					case 7:  // Respond to Friend Request
						//TODO
						break;
					case 8:  // Get Friends
						//TODO
						break;
					case 9:  // Get Notifications
						//TODO
						break;
					case 10:  // Send Location
						//TODO
						break;
						
					case 11:
						getFriends();
						getNotifications();
						sendLocation();
				}
				
				
			}
			catch(IOException e) {
				System.err.println(e);
			}
		}
		
		public void registerUser() {
			
			try {
				// Checks the screenname to the database to make sure
				// screenname does not already exist
				String checkQuery = "select screenname from users " +
						"where screenname = ?";
				genPstmt = connection.prepareStatement(checkQuery);
				genPstmt.setString(1, screenname);
				ResultSet rset = genPstmt.executeQuery();
				
				// If rset has anything in it, screenname exists
				// Else, the screenname is added
				if(rset.next()){
					jta.append("User " + screenname + " already exists\n");
					outputInt = -1;
				} else {
					
					// Prepares the statement for Insert
					String registerString = "INSERT INTO users VALUES (?, ?, ?, ?, ?, NULL, NULL) ";
					genPstmt = connection.prepareStatement(registerString);
					
					// Adds values to prepared statements
					genPstmt.setString(1,  screenname);
					genPstmt.setString(2,  password);
					genPstmt.setInt(3, birth_year);
					genPstmt.setString(4,  firstName);
					genPstmt.setString(5,  lastName);
					
					// Executes query
					genPstmt.execute();
					
					// Prints that screen name has been added into database
					jta.append("User " + screenname + " added to database\n");
					outputInt = 0;
				}
				
			} catch (SQLException ex) {
				jta.append("Error in registering User " + screenname + "\n");
	            ex.printStackTrace();
	            outputInt = -1;
			} catch (Exception ex) {
				jta.append("Unknown error has occur\n");
				ex.printStackTrace();
				outputInt = -1;
			} finally {
				try {
				outputToClient.writeInt(outputInt);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		public void signInUser(){
			try{
				String checkQuery = "select screenname from users " +
					"where screenname = ? "+
					"and password = ?";
				genPstmt = connection.prepareStatement(checkQuery);
				genPstmt.setString(1, screenname);
				genPstmt.setString(2, password);
				ResultSet rset = genPstmt.executeQuery();
				
				// If rset has anything in it, log in
				// Else, the screenname does not exist, do not log in
				if(rset.next()){
					jta.append("signing in " + screenname + "...\n");
					outputInt = 0;
				} else {
					jta.append("user " + screenname + "does not exist\n");
					outputInt = -1;
				}
			} catch (SQLException ex) {
				jta.append("Error in registering User " + screenname + "\n");
	            ex.printStackTrace();
	            outputInt = -1;
			} catch (Exception ex) {
				jta.append("Unknown error has occur\n");
				ex.printStackTrace();
				outputInt = -1;
			} finally {
				try {
				outputToClient.writeInt(outputInt);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		public void requestFriend() {

			try {
				// Checks the screenname to the database to make sure
				// screenname exist
				String checkQuery = "select screenname from users " +
						"where screenname = ?";
				genPstmt = connection.prepareStatement(checkQuery);
				genPstmt.setString(1, friendScreenname);
				ResultSet userExists = genPstmt.executeQuery();
				
				// If rset has anything in it, screenname exists
				// Else, the screenname does not exist
				if(userExists.next()){
					//check for request in request table
					String checkrequestQuery = "select requester from friend_request " +
							"where requester = ? " +
							"and requestee = ?";
					genPstmt = connection.prepareStatement(checkrequestQuery);
					genPstmt.setString(1, screenname);
					genPstmt.setString(2, friendScreenname);
					ResultSet requestExists = genPstmt.executeQuery();
					
					// If rsett has anything in it, friend_request exists
					// Else, the friend_request does not exist
					if(requestExists.next()){
						// Prints that screen name has been added into database
						jta.append("User " + screenname + " has already requested user " + friendScreenname + "\n");
						outputInt = -1;
					}else{
						//check for request in request table
						String checkrequestFromFriendQuery = "select * from friend_request " +
								"where requester = ? " +
								"and requestee = ?";
						genPstmt = connection.prepareStatement(checkrequestFromFriendQuery);
						genPstmt.setString(1, friendScreenname);
						genPstmt.setString(2, screenname);
						ResultSet requestExistsFlipped = genPstmt.executeQuery();
						
						// If rsettt has anything in it, friend_request exists
						// Else, the friend_request does not exist
						if(requestExistsFlipped.next()){
							// Prints that screen name has been added into the friends table
							jta.append("User " + screenname + " is friends with user" + friendScreenname + "\n");
							// Prepares the statement for Insert
							String registerString = "INSERT INTO friends_with VALUES (?, ?) ";
							genPstmt = connection.prepareStatement(registerString);
							
							if (screenname.compareTo(friendScreenname) > 0) {
								// Adds values to prepared statements
								genPstmt.setString(1,  screenname);
								genPstmt.setString(2,  friendScreenname);
							} else {
								// Adds values to prepared statements
								genPstmt.setString(1,  friendScreenname);
								genPstmt.setString(2,  screenname);
							}
							
							// Executes query
							genPstmt.execute();
							outputInt = 0;
						}else{
							// Prints that screen name has been added into the request table
							jta.append("User " + screenname + " has requested user " + friendScreenname + " to be his friend\n");
							// Prepares the statement for Insert
							String registerString = "INSERT INTO friends_request VALUES (?, ?) ";
							genPstmt = connection.prepareStatement(registerString);
							
							// Adds values to prepared statements
							genPstmt.setString(1,  screenname);
							genPstmt.setString(2,  friendScreenname);
							
							// Executes query
							genPstmt.execute();
							outputInt = 0;
						}
					}

				} else {
					// Prints that screen name has been added into database
					jta.append("User " + friendScreenname + " does not exist\n");
					outputInt = -1;
				}
				
			} catch (SQLException ex) {
				jta.append("Error in registering User " + screenname + "\n");
	            ex.printStackTrace();
	            outputInt = -1;
			} catch (Exception ex) {
				jta.append("Unknown error has occur\n");
				ex.printStackTrace();
				outputInt = -1;
			} finally {
				try {
				outputToClient.writeInt(outputInt);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		public void requestLocation() {
			//TODO
		}
		
		public void deleteFriend(){
			try {	
				
				String deleteQuery = "select screenname from users " +
						"where screenname1 = ? AND screenname2 = ?";
				genPstmt = connection.prepareStatement(deleteQuery);
				
				if(screenname.compareTo(friendScreenname) < 0){
					genPstmt.setString(1, screenname);
					genPstmt.setString(2, friendScreenname);
				}
				else{
					genPstmt.setString(1, friendScreenname);
					genPstmt.setString(2, screenname);				
				}
				
				int deletes = genPstmt.executeUpdate();			
				
				if(deletes > 0)
					jta.append("User " + screenname + " deleted " + friendScreenname + "\n");
				else 
					jta.append("User " + screenname + " failed to delete " + friendScreenname + "because he was not friends\n");
				
				outputInt = 0;
				
			} catch (SQLException ex) {
				jta.append("Error in registering User " + screenname + "\n");
	            ex.printStackTrace();
	            outputInt = -1;
			} catch (Exception ex) {
				jta.append("Unknown error has occur\n");
				ex.printStackTrace();
				outputInt = -1;
			} finally {
				try {
				outputToClient.writeInt(outputInt);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		public void respondLocation() {
			//TODO
		}
		
		public void respondFriend() {
			//TODO
		}
		
		public void getFriends() {
			//TODO
			
			/* String result = ""
			 * Query friends table 
			 * 1st: query for screenname on right side ordered by alphabet
			 * result += rset.next() + "//"
			 * 2nd: query for screenname on left side ordered by alphabet
			 * result += rset.next() + "//"*/
		}
		
		public void getNotifications() {
			//TODO
		}
		
		public void sendLocation() {
			//TODO
		}
	}
	
	public void initializeDB() {
		try {
			// Load the driver for postgresql
			Class.forName("org.postgresql.Driver");
			jta.append("Driver loaded\n");
			
			// Establish connection for database
			connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost/trackingproto", "postgres", "postgres");
			jta.append("Database connected\n");
						
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
