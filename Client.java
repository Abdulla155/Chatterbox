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

	// the destination, the sendPort and the myPort
	private String destination, myIP;
	private int sendPort, myPort;

	/*
	 *  Constructor
	 *  destination: the destination address
	 *  sendPort: the sendPort number
	 *  myPort: the myPort
	 */
	Client(String destination, int sendPort, int myPort, String myIP) {
		this.destination = destination;
		this.sendPort = sendPort;
		this.myPort = myPort;
		this.myIP = myIP;
	}

	/*
	 * To start the dialog
	 */
	public boolean start() {
		// try to connect to the destination
		try {
			socket = new Socket(this.destination, this.sendPort);
		}
		// if it failed not much I can so
		catch(Exception ec) {
			display("\tError connecting to destination: " + this.destination + "\n\t" + ec);
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

		// creates the Thread to listen from the destination
		new ListenFromdestination().start();
		// Send our myPort to the destination this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		try
		{
			String username = myIP + " " + myPort;
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
		String msgF = "";
        msgF += ""
            + "\nClient- " + msg 
            + "\n";
        System.out.println(msgF);
    }

	/*
	 * To send a message to the destination
	 */
	public void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to destination: " + e);
		}
	}

	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	public void disconnect() {
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

	public String getIP(){
		/*
		String ip = "";
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip += socket.getLocalAddress().getHostAddress();            
        }
        catch(Exception e){
            System.out.println("ERROR");
        }
		*/
		return this.destination;
	}

	public String getMyPort(){
		return "" + this.sendPort;
	}

	/*
	 * To start the Client in console mode use one of the following command
	 * > java Client
	 * > java Client myPort
	 * > java Client myPort portNumber
	 * > java Client myPort portNumber destinationAddress
	 * at the console prompt
	 * If the portNumber is not specified 1500 is used
	 * If the destinationAddress is not specified "localHost" is used
	 * If the myPort is not specified "Anonymous" is used
	 * > java Client
	 * is equivalent to
	 * > java Client Anonymous 1500 localhost
	 * are eqquivalent
	 *
	 * If an error occurs the program stops
	 */
	
	/*
	 //////////////////////////////////////// Main Not Needed ////////////////
	public static void main(String[] args) {
		// default values
		int sendPort = 1400;
		String destination = "localhost";
		String myPort = "1400";

		// depending of the number of arguments provided we fall through
		switch(args.length) {
			case 3:
				try {
                    destination = args[0];
					sendPort = Integer.parseInt(args[1]);
                    myPort = args[2];
				}
				catch(Exception e) {
                    String msg = "";
                    msg += "\tError: Invalid usage of 'connect'"
                        + "\n\tEnter 'help' to see proper usage.\n";
					System.out.println(msg);
					return;
				}
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
		Client client = new Client(destination, sendPort, myPort);
		// test if we can start the connection to the destination
		// if it failed nothing we can do
		if(!client.start())
			return;

		// wait for messages from user
		Scanner scan = new Scanner(System.in);
		// loop forever for message from the user
        //endWhile:
		while(true) {
			/*
		    System.out.print("  >> ");
			String msg = scan.nextLine();

			// logout if message is LOGOUT
			if(msg.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
				break; // break to do the disconnect
			}

			// message WhoIsIn
			else if(msg.equalsIgnoreCase("WHOISIN")) {
				client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
			}

            // message 
			else {				// default to ordinary message
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
            
		}
		// done disconnect
		client.disconnect();
	}
	*/

	/*
	 * a class that waits for the message from the destination and append them to the JTextArea
	 * System.out.println() it in console 
	 */
	class ListenFromdestination extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					//print the message and add back the prompt
                    System.out.println(msg);
                    System.out.print("  >> ");
				}
				catch(IOException e) {
					display("destination has close the connection: " + e);
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}