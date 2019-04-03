import java.io.*;
import java.net.*;
import java.util.*;

/*
 * The server that can be run both as a console application
 */
public class Server {
	
	// a unique ID for each connection
	private static int uniqueId;
	// an ArrayList to keep the list of the Client
	public ArrayList<ClientThread> al;
	private ArrayList<String> ipAL;
	// the ip server
	private String myIP;
	// the port number to listen for connection
	private int port;
	// the boolean that will be turned of to stop the server
	private boolean keepGoing;


    //Constructor
	public Server(int port){
		// the port
		this.port = port;
		// ArrayList for the Client list
		al = new ArrayList<ClientThread>();
		ipAL = new ArrayList<String>();

		//Get IP
		this.myIP = "";
		try(final DatagramSocket sockett = new DatagramSocket()){
			sockett.connect(InetAddress.getByName("8.8.8.8"), 10002);
			this.myIP = sockett.getLocalAddress().getHostAddress();            
		}
		catch(Exception e){
			System.out.println("ERROR");
		}
	}

	////////////////////////////////////////////////

	public ArrayList<String> getList(){
		return this.ipAL;
	}

	public void start() { 
		keepGoing = true;
		/* create socket server and wait for connection requests */
		try
		{
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// infinite loop to wait for connections
			while(keepGoing) 
			{
				// format message saying we are waiting
				display("Waiting for Clients on port " + port + ".\n\n  >> ");

				Socket socket = serverSocket.accept();  	// accept connection
				
				// if I was asked to stop
				if(!keepGoing)
					break;

				ClientThread t = new ClientThread(socket);  // make a thread of it
				al.add(t);									// save it in the ArrayList	
				ipAL.add(t.toStringg());
				t.start();
			}
			// I was asked to stop
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
						// not much I can do
					}
				}
			}
			catch(Exception e) {
				display("\tException closing the server and clients: \n\t\t" + e);
			}
		}
		// something went bad
		catch (IOException e) {
			display("\tException on new ServerSocket: \n\t\t" + e);
		}
	}

    //to stop the server
	protected void stop() {
		keepGoing = false;
		// connect to myself as Client to exit statement
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
			// nothing I can really do
		}
	}

	//Display an event (not a message) to the console
	private void display(String msg) {
        String msgF = "\n\tServer- " + msg;
        System.out.print(msgF);
	}

	//to broadcast a message to all Clients
	private synchronized void broadcast(String message) {
		// display single message on console
		System.out.print(message + "\n\n  >> ");

		/* to all clients:
		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			// try to write to the Client if it fails remove it from the list
			if(!ct.writeMsg(message)) {
				al.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
		*/
	}

	// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// found it
			if(ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}

	/** One instance of this thread will run for each client */
	public class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String username;
		// the only type of message a will receive
		ChatMessage cm;

		String myIP;
		String myPort;

		// Constructore
		ClientThread(Socket socket) {
			//Get IP
			this.myIP = "";
			this.myPort = "" + socket.getLocalPort();
			try(final DatagramSocket sockett = new DatagramSocket()){
				sockett.connect(InetAddress.getByName("8.8.8.8"), 10002);
				this.myIP = sockett.getLocalAddress().getHostAddress();            
			}
			catch(Exception e){
				System.out.println("ERROR");
			}
			
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			String incomingIP = "";
			String incomingPort = "";
			/* Creating both Data Stream */
			//display("Thread trying to create Object Input/Output Streams\n\n  >> ");
			try
			{
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				incomingIP = username.split(" ")[0];
				display(incomingIP + " just connected.");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: \n\t\t" + e + "\n\n  >> ");
				return;
			}
			// have to catch ClassNotFoundException
			// but I read a String, I am sure it will work
			catch (ClassNotFoundException e) {
			}

			
		}

		// what will run forever
		public void run() {
			String incomingIP, incomingPort; 
			// to loop until LOGOUT
			boolean keepGoing = true;
			while(keepGoing) {
				// read a String (which is an object)
				try {
					cm = (ChatMessage) sInput.readObject();
				} catch (IOException e) {
					display(username + " Exception reading Streams: \n\t\t" + e + "\n\n  >> ");
					break;
				} catch(ClassNotFoundException e2) {
					break;
				}
				// the messaage part of the ChatMessage
				String message;
				incomingIP = cm.getUser().split(" ")[0];
				incomingPort = cm.getUser().split(" ")[1];
				message = "\n\tMessage received from " + incomingIP
					 	+ "\n\tSender's Port: " + incomingPort
					 	+ "\n\tMessage: " + cm.getMessage();

				broadcast(message);
			}
			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(id);
			close();
		}

		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		/*
		 * Write a String to the Client output stream
		 */
		private boolean writeMsg(String msg) {
			String msgF;
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
		
		private String toStringg() {
			String incomingIP = username.split(" ")[0];
			String incomingPort = username.split(" ")[1];
			String msg = "\t" +this.id + "\t" + incomingIP + "\t" + incomingPort;
			return msg; 
		}
	}
}