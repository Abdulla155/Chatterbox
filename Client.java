import java.net.*;
import java.io.*;
import java.util.*;

/*
 * The Client that can be run both as a console 
 */
public class Client  {

	// for I/O
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;

	// the server, the port and the username
	private String server, username;
	private int port;

	/*
	 *  Constructor
	 *  server: the server address
	 *  port: the port number
	 *  username: the username
	 */
	Client(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}

	/*
	 * To start the dialog
	 */
	public boolean start() {
		// try to connect to the server
		try {
			socket = new Socket(server, port);
		}
		// if it failed not much I can so
		catch(Exception ec) {
			display("\tError connectiong to server: " + server + "\n\t" + ec);
			return false;
		}

		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);

		/* Creating both Data Stream */
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// creates the Thread to listen from the server
		new ListenFromServer().start();
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}

	/*
	 * To send a message to the console
	 */
	private void display(String msg) {
		System.out.println(msg); 
    }

	/*
	 * To send a message to the server
	 */
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	private void disconnect() {
		try {
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} // not much else I can do
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} // not much else I can do
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} // not much else I can do


	}
	/*
	 * To start the Client in console mode use one of the following command
	 * > java Client
	 * > java Client username
	 * > java Client username portNumber
	 * > java Client username portNumber serverAddress
	 * at the console prompt
	 * If the portNumber is not specified 1500 is used
	 * If the serverAddress is not specified "localHost" is used
	 * If the username is not specified "Anonymous" is used
	 * > java Client
	 * is equivalent to
	 * > java Client Anonymous 1500 localhost
	 * are eqquivalent
	 *
	 * If an error occurs the program stops
	 */
	public static void main(String[] args) {
		// default values
		int portNumber = 1400;
		String serverAddress = "localhost";
		String userName = "Anonymous";

		// depending of the number of arguments provided we fall through
		switch(args.length) {
			// > javac Client username portNumber serverAddr
			case 3:
				serverAddress = args[2];
			// > javac Client username portNumber
			case 2:
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
                    String msg = "";
                    msg += "\tError: Invalid usage of 'connect'"
                        + "\n\tEnter 'help' to see proper usage.\n";
					System.out.println(msg);
					return;
				}
			// > javac Client username
			case 1:
				userName = args[0];
			// > java Client
			case 0:
				break;
			// invalid number of arguments
			default:
				String msg = "";
                msg += "\tError: Invalid usage of 'connect'"
                    + "\n\tEnter 'help' to see proper usage.\n";
                System.out.println(msg);
                return;
		}
		// create the Client object
		Client client = new Client(serverAddress, portNumber, userName);
		// test if we can start the connection to the Server
		// if it failed nothing we can do
		if(!client.start())
			return;

		// wait for messages from user
		Scanner scan = new Scanner(System.in);
		// loop forever for message from the user
		while(true) {
			System.out.print("> ");
			// read message from user
			String msg = scan.nextLine();
			// logout if message is LOGOUT
			if(msg.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
				// break to do the disconnect
				break;
			}
			// message WhoIsIn
			else if(msg.equalsIgnoreCase("WHOISIN")) {
				client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
			}
			else {				// default to ordinary message
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		// done disconnect
		client.disconnect();
	}

	/*
	 * a class that waits for the message from the server and append them to the JTextArea
	 * System.out.println() it in console 
	 */
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					//print the message and add back the prompt
                    System.out.println(msg);
                    System.out.print("> ");
				}
				catch(IOException e) {
					display("Server has close the connection: " + e);
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}