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

	//To start the dialog
	public boolean start() {
		// try to connect to the destination
		try {
			socket = new Socket(this.destination, this.sendPort);
		}
		// if it failed not much I can so
		catch(Exception ec) {
			display("Error connecting to destination: " + this.destination + "\n\t\t" + ec);
			return false;
		}
		display("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());

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

	//To send a message to the console  
	private void display(String msg) {
		String msgF = "\tClient- " + msg + "\n\n  >> ";
        System.out.print(msgF);
    }

	//To send a message to the destination
	public void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			System.out.print("\n\tException writing to destination: \n\t\t" + e);
		}
	}

	// When something goes wrong
	// Close the Input/Output streams and disconnect not much to do in the catch clause
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
		return this.destination;
	}

	public String getMyPort(){
		return "" + this.sendPort;
	}

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
					display("Destination has close the connection: " + e 
						+ "\n\t\tUse the 'terminate' command to remove this connection.");
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}