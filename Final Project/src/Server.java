/*
 * Programmer: Andres Carranza
 * Date: 5/16/2019
 *
 * CLASS DETAILS:
 	- This class holds the graphics and logic for a server
 	- Server communicates with all users
 	- Users communicate with each other through the server
 */
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class Server {
	public static final int JPANEL_WIDTH = 500;
	public static final int JPANEL_HEIGHT = 500;
	private static final int JFRAME_WIDTH = getJFrameWidth();
	private static final int JFRAME_HEIGHT = getJFrameHeight();
	public static final int SERVER_PORT_NUMBER = 5000;
	public static final String CLOSE_CONNECTION = "!close connection";//Used to request to close connection
	public static final String GET_ONLINE_IPS = "!get online ips";//Used to request online ips
	public static final String RECIPIENT_NAME_NOT_FOUND = "!recipient name not found";//Used to communicate to client that a username was not found
	public static final String SET_NAME = "!set name";//Used to set username
	public static final String USERNAME_VALID = "!username valid";//Used to communicate that setting the username was successful
	public static final String USERNAME_INVALID = "!username invalid";//Used to communicate that setting the username was unsuccessful
	private JFrame frame;//JFrame for server
	private ServerSocket server;//Socket for server
	private Socket client;//Socket for client
	private LinkedHashMap< String,ServerThread> serverThreads; //List of all server threads
	private LinkedHashMap< String, String> onlineNames; //List of all username
	private JLabel onlineTitle;//Label for online header
	private JLabel trafficTitle;//Label for traffic header
	private JTextArea onlineIpsDisplay;//Displays all online usernames/ips
	private JTextArea serverTraffic;//Logs all server traffic
	private JTextArea serverIp;//Displays server ip(connect code)
	private JScrollPane serverTrafficPane;//Scrolls server traffic

	//Constructor
	Server(){
		serverThreads = new LinkedHashMap<String, ServerThread>(); //Ip is key, server thread is value
		onlineNames = new LinkedHashMap<String, String>(); //name is key, ip is value

		setUpScreen();
	}

	//Sets up GUI
	public void setUpScreen() {
		//Creating window
		frame = new JFrame("Server");

		onlineTitle = new JLabel("Online: ");

		onlineIpsDisplay = new JTextArea(27, 15);
		onlineIpsDisplay.setEditable(false);
		updateOnlineIpsDisplay();

		JScrollPane onlineIpsPane = new JScrollPane(onlineIpsDisplay, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		trafficTitle = new JLabel("Server Traffic: ");


		serverTraffic = new JTextArea(27,25);
		serverTraffic.setEditable(false);

		serverTrafficPane = new JScrollPane(serverTraffic, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		serverIp = new JTextArea(1,10);
		serverIp.setEditable(false);
		String sIp = "unknown";
		try {
			sIp= InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {}

		serverIp.setText("Server Connection code: " + sIp);
		serverIp.setFont(new Font("Arial", Font.BOLD, 25));
		
		Box box1 = Box.createVerticalBox();
		box1.add(Box.createVerticalStrut(10));
		box1.add(onlineTitle);
		box1.add(Box.createVerticalStrut(5));
		box1.add(onlineIpsPane);
		box1.add(Box.createVerticalStrut(10));

		Box box2 = Box.createVerticalBox();
		box2.add(Box.createVerticalStrut(10));
		box2.add(trafficTitle);
		box2.add(Box.createVerticalStrut(5));
		box2.add(serverTrafficPane);
		box2.add(Box.createVerticalStrut(10));

		Box box3 = Box.createHorizontalBox();
		box3.add(box1);
		box3.add(Box.createHorizontalStrut(20));
		box3.add(box2);

		Box box4 = Box.createHorizontalBox();
		box4.add(serverIp);

		Box box5 = Box.createVerticalBox();
		box5.add(box3);
		box5.add(Box.createHorizontalStrut(10));
		box5.add(box4);

		Container c = frame.getContentPane();
		c.setLayout (new FlowLayout());
		c.add(box5);

		//setting up window
		frame.setSize(JFRAME_WIDTH , JFRAME_HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(0, 0);
		frame.setResizable(false);

		frame.setVisible(true);

	}

	/*****************
	 *PRIVATE METHODS*
	 *****************
	 */
	
	//Starts the server
	private void startServer() throws IOException {

		server = new ServerSocket(SERVER_PORT_NUMBER);
		while(true)
		{
			client = server.accept();//Waits for a client to connect
			String clientIp =  client.getInetAddress().getHostAddress();


			appendServerTraffic(clientIp + " connected to the server\n");

			ServerThread serverThread = new ServerThread(client,this);//Creates a new server thread with the connection to the client
			new Thread(serverThread).start();

			serverThreads.put(clientIp, serverThread);//Adds the thread to hash map. clientIp is the key, serverThread is the value
			setClientName(clientIp, clientIp);

			updateOnlineIpsDisplay();

		}

	}

	//Sends a message 
	private void sendMessage(String senderName, String recipientName, String message) {
		String recipientIp = onlineNames.get(recipientName);
		serverThreads.get(recipientIp).sendMessage(senderName, message);
	}

	//Notifies all connected clients of the online ips/usernames
	private void notifyClientsOfOnlineIps() {
		for(ServerThread s : serverThreads.values()) {
			s.sendMessage(MessageScene.CLASS_ID + ":" + GET_ONLINE_IPS + ":" + getOnlineNames());
		}
	}
	
	//Returns the online ips
	private Collection<String> getOnlineIps(){
		return onlineNames.values();
	}
	
	//Returns the online usernames
	private Set<String> getOnlineNames(){
		return onlineNames.keySet();
	}

	//Deletes an ip
	private synchronized void deleteIp(String name) {
		serverThreads.remove(onlineNames.get(name));
		onlineNames.remove(name);
		updateOnlineIpsDisplay();
	}

	//Sets a username for a client
	private void  setClientName(String clientName, String clientIp) {
		if(getOnlineNames().contains(clientIp))
			onlineNames.remove(clientIp);
		onlineNames.put(clientName, clientIp); 
		updateOnlineIpsDisplay();
	}

	//Updates the onlineIpsDisplay
	private void updateOnlineIpsDisplay() {
		if(onlineNames.size() == 0) {
			onlineIpsDisplay.setText("No one is currently online");
		}
		else {
			onlineIpsDisplay.setText(null);
			for(String name : getOnlineNames())
				onlineIpsDisplay.append(name+"\n");

		}

	}

	//Appends server traffic
	private void appendServerTraffic(String str) {
		serverTraffic.append(str);

		serverTraffic.setCaretPosition(serverTraffic.getText().length());
	}

	//returns screen size
	private static Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	//Returns jframe height
	private static int getJFrameHeight() {
		Insets insets = getInsets();
		return JPANEL_HEIGHT + insets.bottom + insets.top;
	}

	//returns jframe height
	private static int getJFrameWidth() {
		Insets insets = getInsets();
		return JPANEL_WIDTH + insets.left + insets.right;
	}

	//returns insets
	private static Insets getInsets() {
		JFrame f = new JFrame();
		f.setLocation(getScreenSize().width, getScreenSize().height);
		f.setVisible(true);
		Insets insets = f.getInsets();
		f.setVisible(false);
		return insets;
	}

	//Class handles a connection with a client
	private  class ServerThread implements Runnable {
		private Server server;//Used to communicate with the server object
		private Socket client;//Used to communicate with the client
		private BufferedReader clientIn;//Used to receive messages form the client
		private PrintStream clientOut;//Used to send messages to the client
		private String clientIp;//Stores the client's ip
		private String clientName;//Stores the client's username

		//Constructor
		ServerThread(Socket client, Server server ) throws IOException {

			this.client = client;
			this.server = server;

			clientIp = client.getInetAddress().getHostAddress();
			clientName = clientIp;

			clientIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			clientOut = new PrintStream(client.getOutputStream());

		}

		//Communicating with the client
		@Override
		public void run() {
			try{
				String messageReceived;
				while(true) {
					messageReceived = clientIn.readLine();
					if(messageReceived.charAt(0) == '!') {
						appendServerTraffic(clientName + " >>> Server: " + messageReceived + "\n");
						if(messageReceived.equals(CLOSE_CONNECTION)) {
							break;
						}
						else if(messageReceived.equals(GET_ONLINE_IPS)) {
							String onlineIps = getOnlineIps().toString();
							sendMessage(MessageScene.CLASS_ID + ":" + GET_ONLINE_IPS + ":" +onlineIps);
						}
						else if(messageReceived.equals(SET_NAME)) {
							String requestedName = clientIn.readLine();
							appendServerTraffic(clientIp + " >>> Server: " +requestedName + "\n");

							if(getOnlineNames().contains(requestedName) || !requestedName.matches("^[a-zA-Z0-9]*$") || requestedName.length() > 10) {
								sendMessage(Main.CLASS_ID + ":" + USERNAME_INVALID);
							}
							else {
								clientName = requestedName;
								setClientName(clientName, clientIp);
								sendMessage(Main.CLASS_ID + ":" + USERNAME_VALID);
								notifyClientsOfOnlineIps();
							}
						}
					}
					else {
						//Format recipient name:recipient class:type:message
						int indexOfC1 =  messageReceived.indexOf(':');
						String recipientName = messageReceived.substring(0, indexOfC1);//Message will be in format recipientIp:classid:message
						String message = messageReceived.substring(indexOfC1 + 1);
						appendServerTraffic(clientName + " >>> " + recipientName +": " + message + "\n");

						if(getOnlineNames().contains(recipientName)) {
							server.sendMessage(clientName, recipientName, message);
						}
						else {
							appendServerTraffic("Server >>> " + clientName +": " + RECIPIENT_NAME_NOT_FOUND + "\n");
							sendMessage(RECIPIENT_NAME_NOT_FOUND);
						}
					}
				}
				closeConnection();		
			} 
			catch(IOException ex){
			}
		}

		//Sends a message from the server to the client
		public void sendMessage(String message) {
			appendServerTraffic("Server >>> " + clientName + ": " + message + "\n");
			clientOut.println("!" + message);
		}

		//Sends a message from a client to a client
		public synchronized void sendMessage(String senderName, String message) {
			clientOut.println(senderName + ":" + message);
		}

		//Closes the connection
		public void closeConnection() throws IOException{
			appendServerTraffic(clientName + " disconnected from the server\n");

			client.close();
			clientIn.close();
			clientOut.close();
			deleteIp(clientName);

			notifyClientsOfOnlineIps();
		}
	}
	
	//Initializes the server
	public static void main(String[] args) { 
		Server serverobj =  new Server();
		try {
			serverobj.startServer();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

}
