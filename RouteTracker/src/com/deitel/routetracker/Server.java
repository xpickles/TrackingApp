//test
import java.io.*;
import java.net.*;
import java.text.Collator;
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
		private double lastKnownLat;
		private double lastKnownLong;
		private String friendScreenname;
		private int outputInt;
		private boolean response;
		
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
						
						// Request Friend
						requestFriend();
						
						break;
					case 4:  // Request Location
						
						// Reads users information from client
						screenname = inputFromClient.readUTF();
						friendScreenname = inputFromClient.readUTF();
						
						requestLocation();
						
						break;
					case 5:  // Delete Friend
						
						// Reads users information from client
						screenname = inputFromClient.readUTF();
						friendScreenname = inputFromClient.readUTF();
						
						// Delete Friend
						deleteFriend();
						
						break;
					case 6:  // Respond to Location Request

						friendScreenname = inputFromClient.readUTF();
						screenname = inputFromClient.readUTF();
						lastKnownLat = inputFromClient.readDouble();
						lastKnownLong = inputFromClient.readDouble();
						response = inputFromClient.readBoolean();
						
						respondLocation();
						
						break;
					case 7:  // Respond to Friend Request
						
						friendScreenname = inputFromClient.readUTF();
						screenname = inputFromClient.readUTF();
						response = inputFromClient.readBoolean();
						
						respondFriend();
						
						break;
					case 8:  // Get Friends
						screenname = inputFromClient.readUTF();
						
						getFriends();
						
						break;
					case 9:  // Get Notifications
						screenname = inputFromClient.readUTF();
						
						getNotifications();
						
						break;
					case 10:  // Get Location

						screenname = inputFromClient.readUTF();
						
						getLocation();
						
						break;
						
					case 11: // Done as soon logged in
						
						screenname = inputFromClient.readUTF();
						getFriends();
						getNotifications();
						getLocation();
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
					String checkrequestQuery = "select * from friend_request " +
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
					} else {
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
							
							Collator collator = Collator.getInstance();
							if (collator.compare(screenname,friendScreenname) < 0) {
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
							jta.append("User " + screenname + " sent friend request");
							outputInt = 0;
						}else{
							// Prints that screen name has been added into the request table
							jta.append("User " + screenname + " has requested user " + friendScreenname + " to be his friend\n");
							// Prepares the statement for Insert
							String registerString = "INSERT INTO friend_request VALUES (?, ?) ";
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
				jta.append("Error in requesting friend " + friendScreenname + "\n");
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

			try {
				String checkQuery = "Select * from friends_with" +
					"where requester = ? and requestee = ?";
				genPstmt = connection.prepareStatement(checkQuery);
				
				// Adds friends into table in compared alphabetically
				Collator collator = Collator.getInstance();
				if (collator.compare(screenname,friendScreenname) < 0) {
					genPstmt.setString(1, screenname);
					genPstmt.setString(2, friendScreenname);
				} else {
					genPstmt.setString(1, friendScreenname);
					genPstmt.setString(2, screenname);
				}
				
				ResultSet areFriends = genPstmt.executeQuery();
				
				if (areFriends.next()) {
					checkQuery = "Select * from location_request " +
						"where requester = ? and requestee = ?";
					genPstmt = connection.prepareStatement(checkQuery);
					genPstmt.setString(1, screenname);
					genPstmt.setString(2, friendScreenname);
					ResultSet requestExists = genPstmt.executeQuery();
					
					if (!requestExists.next()) {
						String addRequest = "Insert into location_request " +
						"values (?, ?)";
						genPstmt = connection.prepareStatement(addRequest);
						genPstmt.setString(1, screenname);
						genPstmt.setString(2, friendScreenname);
						genPstmt.execute();
						jta.append(screenname + " has requested the location of " +
								friendScreenname);
						outputInt = 0;
					} else {
						String addRequest = "Delete from location_request " +
						"where requester = ? and requestee = ?";
						genPstmt = connection.prepareStatement(addRequest);
						genPstmt.setString(1, screenname);
						genPstmt.setString(2, friendScreenname);
						genPstmt.execute();
						addRequest = "Insert into location_request " +
						"values (?, ?)";
						genPstmt = connection.prepareStatement(addRequest);
						genPstmt.setString(1, screenname);
						genPstmt.setString(2, friendScreenname);
						genPstmt.execute();	
						jta.append(screenname + " has requested the location of " +
								friendScreenname + ", which updated the time");
						outputInt = 0;
					}
				} else {
					jta.append("Error in adding " + friendScreenname + " for " +
							screenname);
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
		
		public void deleteFriend(){
			try {	
				
				String deleteQuery = "delete from friends_with " +
						"where screenname1 = ? AND screenname2 = ?";
				genPstmt = connection.prepareStatement(deleteQuery);
				
				Collator collator = Collator.getInstance();
				if (collator.compare(screenname,friendScreenname) < 0){
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
				jta.append("Error in deleting " + friendScreenname + "\n");
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
			try {
				// If request is accepted
				 if (response) {
					 // Update users last known location
					 String respondString = "Update users " +
					 			"Set last_known_lat = ?, " +
					 			"Set last_known_long = ?";
					 genPstmt = connection.prepareStatement(respondString);
					 genPstmt.setDouble(1, lastKnownLat);
					 genPstmt.setDouble(2, lastKnownLong);
					 genPstmt.execute();
					 jta.append("Location updated for " + screenname);
					 
					 // Show that the request has been accepted
					 respondString = "Insert into accepted_location_request values (?, ?)";
					 genPstmt = connection.prepareStatement(respondString);
					 genPstmt.setString(1, friendScreenname);
					 genPstmt.setString(2, screenname);
					 genPstmt.execute();
					 jta.append("Request for location from " + friendScreenname +
							 " to " + screenname + " accepted");
				 }
				 
				 // Delete request whether request was accepted or rejected
				 String deleteRequest = "Delete from location_request " + 
				 			"where requester = ? and requestee = ?";
				 genPstmt = connection.prepareStatement(deleteRequest);
				 genPstmt.setString(1, friendScreenname);
				 genPstmt.setString(2, screenname);
				 genPstmt.execute();
				 jta.append("Request for location from " + friendScreenname +
							 " to " + screenname + " deleted");
				 outputInt = 0;
				 
			} catch (SQLException ex) {
				jta.append("Error in responding to location request " +
						"for " + screenname + "\n");
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
		
		public void respondFriend() {
			try {
				// Accepts request
				if (response) {
					String respondString = "INSERT INTO friends_with VALUES (?, ?) ";
					genPstmt = connection.prepareStatement(respondString);
					
					// Adds friends into table in compared alphabetically
					Collator collator = Collator.getInstance();
					if (collator.compare(screenname,friendScreenname) < 0) {
						genPstmt.setString(1,  screenname);
						genPstmt.setString(2,  friendScreenname);
					} else {
						genPstmt.setString(1,  friendScreenname);
						genPstmt.setString(2,  screenname);
					}
					
					// Executes query
					genPstmt.execute();
					jta.append(screenname + " is now friends with " + 
							friendScreenname + "\n");
					outputInt = 0;
				} 
				else{
					jta.append(screenname + " denied being friends with " + 
							friendScreenname + "\n");
				}
				// Accepted or Denied, Request is Deleted
				String deleteRequest = "Delete from friend_request " +
					"where requester = ? and requestee = ?";
				genPstmt = connection.prepareStatement(deleteRequest);
				genPstmt.setString(1, friendScreenname);
				genPstmt.setString(2, screenname);
				genPstmt.execute();
				outputInt = 0;
				
				
			} catch (SQLException ex) {
				jta.append("Error in requesting friend for " + screenname + "\n");
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
		
		public void getFriends() {
			
			String result = "";
			try {
				jta.append(screenname + " trying to get friends\n");
				String friendQuery = "select screenname1 from friends_with " +
						"where screenname2 = ?";
				genPstmt = connection.prepareStatement(friendQuery);				
				genPstmt.setString(1, screenname);				
				
				ResultSet friends = genPstmt.executeQuery();			
				
				while(friends.next()){
					result += friends.getString("screenname1") + "/";
				}
				
				friendQuery = "select screenname2 from friends_with " +
						"where screenname1 = ?";
				genPstmt = connection.prepareStatement(friendQuery);				
				genPstmt.setString(1, screenname);				
				
				friends = genPstmt.executeQuery();
				while(friends.next()){
					result += friends.getString("screenname2") + "/";
				}
				if (!result.equals("")) {
					result = result.substring(0, result.length()-1);	
				}
							
				jta.append(screenname + " received friends\n");
				
				outputInt = 0;
				
			} catch (SQLException ex) {
				jta.append("Error getting User " + screenname + "'s friends\n");
	            ex.printStackTrace();
	            outputInt = -1;
			} catch (Exception ex) {
				jta.append("Unknown error has occur\n");
				ex.printStackTrace();
				outputInt = -1;
			} finally {
				try {
				outputToClient.writeInt(outputInt);
				outputToClient.writeUTF(result);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		public void getNotifications() {
			String result = "";
			try {
				jta.append(screenname + " trying to get notifications\n");
				String locationRequestQuery = "select requester from location_request " +
						"where requestee = ?";
				genPstmt = connection.prepareStatement(locationRequestQuery);				
				genPstmt.setString(1, screenname);				
				
				ResultSet locationRequests = genPstmt.executeQuery();			
				result = "location/";
				while(locationRequests.next()){
					result += locationRequests.getString("requester") + "/";
				}
				
				String friendRequestQuery = "select requester from friend_request " +
						"where requestee = ?";
				genPstmt = connection.prepareStatement(friendRequestQuery);				
				genPstmt.setString(1, screenname);				
				
				ResultSet friendRequests = genPstmt.executeQuery();			
				result += "friend/";
				while(friendRequests.next()){
					result += friendRequests.getString("requester") + "/";
				}
				
				result = result.substring(0, result.length()-1);				
				
				jta.append(screenname + " received notifications\n");
				
				outputInt = 0;
				
			} catch (SQLException ex) {
				jta.append("Error getting User " + screenname + "'s notifications\n");
	            ex.printStackTrace();
	            outputInt = -1;
			} catch (Exception ex) {
				jta.append("Unknown error has occur\n");
				ex.printStackTrace();
				outputInt = -1;
			} finally {
				try {
				outputToClient.writeInt(outputInt);
				outputToClient.writeUTF(result);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		public void getLocation() {
			try {
				String checkQuery = "Select * from accepted_location_request " +
					"where requester = ?";
				genPstmt = connection.prepareStatement(checkQuery);
				genPstmt.setString(1, screenname);
				ResultSet rset = genPstmt.executeQuery();
				
				if (rset.next()) {
					String requestee = rset.getString("requestee");
					checkQuery = "Select * from users where screenname = ?";
					genPstmt = connection.prepareStatement(checkQuery);
					genPstmt.setString(1, requestee);
					rset = genPstmt.executeQuery();
					
					if (rset.next()) {
						try {
							outputToClient.writeDouble(rset.getDouble("last_known_lat"));
							outputToClient.writeDouble(rset.getDouble("last_known_long"));
							outputInt = 0;
						} catch (Exception ex) {
							outputInt = -1;
						}
					}
				}
				
			} catch (SQLException ex) {
				jta.append("Error in getting " + screenname + "'s location\n");
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
