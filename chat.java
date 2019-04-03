import java.io.*;
import java.net.*;
import java.util.*;

public class Chat {

    ArrayList<Client> clientList;

    public Chat(){
        clientList = new ArrayList<Client>();
    }

    private ArrayList<String[]> ipList() {
        ArrayList<String[]> ipList = new ArrayList<String[]>();
        String[] ipPort = new String[2];
        for(Client client: clientList){
            ipPort[0] = client.getIP();
            ipPort[1] = client.getMyPort();
            ipList.add(ipPort);
        }
        return ipList;
    }

    // Display information about the available user interface options or command
    // manual.
    public void help() {
        String helpStr = "";
        helpStr +=  "\thelp                            \tGet list of commands"
                + "\n\tmyip                            \tDisplay the IP address of this process"
                + "\n\tmyport                          \tDisplay listening port for incomming connections"
                + "\n\tconnect <destination> <port no> \tCreate new client and connect to server"
                + "\n\t                                \t<destination> is the IP address of the target computer"
                + "\n\t                                \t<port no> is the listening port on target computer"
                + "\n\tlist                            \tList current connections to this server"
                + "\n\tterminate <connection id>       \tTerminates connection of specfied connected id"
                + "\n\tsend <connection id> <message>  \tSends message '<message>' to specified connected id"
                + "\n\texit                            \tTerminates all connections and exts program"
                + "\n";
        System.out.println(helpStr);
    }

    // Display the IP address of this process.
    // Note: Not 'Local' address (127.0.0.1). The actual IP of the computer.
    public String myip() {
        String msg = "";
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return ""+ socket.getLocalAddress().getHostAddress();
        }
        catch(Exception e){
            System.out.println("ERROR");
            return "";
        }
        
    }

    // Display the port on which this process is listening for incoming
    // connections.
    public void myport(int myPort) {
        String msg = "";
        msg += "\tListening on port: " + myPort + "\n";
        System.out.println(msg);
    }

    // This command establishes a new TCP connection to the specified
    // <destination> at the specified < port no>. The <destination> is the IP
    // address of the computer. Any attempt to connect to an invalid IP should
    // be rejected and suitable error message should be displayed. Success or
    // failure  in  connections  between  two  peers  should  be  indicated  by
    // both  the  peers  using  suitable  messages.  Self-connections and
    // duplicate connections should be flagged with suitable error messages
    public void connect(String destination, String sendPort, String myPort, String myip) {
        
        // Check is ip is already connected
        boolean alreadyConnected = false;
        String[] ipPort = new String[2];
        ipPort[0] = destination;
        ipPort[1] = sendPort;
        //get data from connected clients
        for(String[] iPCheck : this.ipList() ){
            if ( iPCheck[0].equals(destination) )
                if ( iPCheck[1].equals(sendPort) )
                    alreadyConnected = true;
        } 
        if(alreadyConnected){
            System.out.print("\tError: Already connected to " + destination + " on port " + sendPort + "\n\n  >> ");
            return;
        } 

        // Check if connected to same ip and port
        if(destination.equals(myip))
            if(sendPort.equals(myPort)){
                System.out.print("\tError: Can not connect to oneself.\n\n  >> ");
                return;
            }

        try{
            int sendPort_INT = Integer.parseInt(sendPort);
            int myPort_INT = Integer.parseInt(myPort);
            Client newClient = new Client(destination, sendPort_INT, myPort_INT, myip);
            if(!newClient.start())
                return;
            
            this.clientList.add(newClient);
        } catch(Exception e){
            String msg = "";
            msg +=  "\tError connecting: \n\t\t" + e
                + "\n\tEnter 'help' to see proper usage.\n\n  >> ";
            System.out.print(msg);
        }
    }

    // Display a numbered list of all the connections this process is part of.
    // This numbered list will include connections  initiated  by  this  process
    // and  connections  initiated  by  other  processes.  The  output  should
    // display the IP address and the listening port of all the peers the
    // process is connected to.
    public void list(Server server) {
        int i = 1;
        String msg = "\tid:\tIP Address\tPort No.\n";      
        for (Client client: this.clientList){
            msg += "\t" + i + ":\t" 
                + client.getIP() + "\t" 
                + client.getMyPort() + "\n";
            i++;
        } System.out.println(msg);
    }

    // This  command  will  terminate  the  connection  listed  under  the
    // specified number  when  LIST  is  used  to  display  all  connections.
    // E.g.,  terminate  2.  In  this  example,  the  connection  with
    // 192.168.21.21  should  end.  An  error  message  is  displayed  if  a
    // valid  connection  does  not  exist  as number 2. If a remote machine
    // terminates one of your connections, you should also display a message
    public void terminate(String connectionID, Server server) {
        String msg = "";
        try{
            int connectionID_INT = Integer.parseInt(connectionID) - 1;
            Client client = clientList.get(connectionID_INT);
            client.disconnect();
            clientList.remove(connectionID_INT);
            msg = "\t" + client.getIP() + " disconnected.\n";
            System.out.println(msg);
        } catch(Exception e){
            msg += "\tError: " + e
                + "\n\tInvalid usage"
                + "\n\tEnter 'help' to see proper usage.\n";
            System.out.println(msg);
        }
        
    }

    // (For example, send 3 Oh! This project is a piece of cake). This will send
    // the message to the host on the connection that is designated by the
    // number 3 when command 'list' is used.  The  message  to  be  sent  can
    // be  up-to  100  characters  long,  including  blank  spaces.  On
    // successfully  executing  the command,  the  sender  should  display
    // 'Message  sent  to  <connection  id>'  on  the  screen.  On  receiving
    // any  message  from  the  peer,  the  receiver  should  display  the
    // received  message  along  with  the  sender information.
    // (Eg. If a process on 192.168.21.20 sends a message to a process on
    // 192.168.21.21 then the output on 192.168.21.21 when receiving a message
    // should display as shown:
    //
    // Message received from 192.168.21.20
    // Sender’s Port: <The port no. of the sender>
    // Message: '<received message>'
    public void send(String user, String connectionID, String message, Server server) {
        
        int connectionID_INT = 999;

        try{
            connectionID_INT = Integer.parseInt(connectionID) - 1;
        } catch(Exception e) {
            System.out.println("\n\tError " + e);
        }

        if(connectionID_INT < clientList.size() && connectionID_INT >= 0) {
            Client client = clientList.get(connectionID_INT);
            try{
                client.sendMessage(new ChatMessage(user, message));
            } catch(Exception e){
                System.out.println("\n\tError " + e);
            }
        } else System.out.println("\tID not in list.");
    }

    // Close all connections and terminate this process. The other peers should
    // also update their connection list by removing the peer that exits 
    public void exit(Server server) {  
        
        //clientList = server.getList();   
        for (int i = 1; i <= this.clientList.size(); i++){
            String iStr = "" + i;
            terminate(iStr, server);
        }
        server.stop();
        
    }

    //////////////////////////////////////////////
    //////////////////// MAIN ////////////////////
    public static void main(String[] args) {

        Chat chat = new Chat();        
        Scanner scan = new Scanner(System.in);;
        String input;
        String[] inputTokens;
        String user;
        
        //initial port number set to 1400
        int portNumber = 1400; 
        if (args.length != 0) {
            try {
                portNumber = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.out.println("\tError: Invalid port number. Exited.\n");
                return;
            }
        }

        user = chat.myip() + " " + portNumber;

        //Start server on the set port
        String[] serverArgs = {"" + portNumber};
        Server server = new Server(portNumber);
        new Thread(){
            public void run(){
                server.start();
            };
        }.start();

        String welcomeMsg = "\n"
                + "\nWelcome to Chat.java"
                + "\nEnter 'help' to get a list of commands...";
        System.out.println(welcomeMsg);

        endProgram:
        while (true) {
            input = scan.nextLine();
            inputTokens = input.split(" ");
            switch (inputTokens[0]) {
                case "help" :
                    chat.help();
                    System.out.print("  >> ");
                    break;

                case "myip" :
                    System.out.println("\t" + chat.myip() + "\n");
                    System.out.print("  >> ");
                    break;

                case "myport" :
                    chat.myport(portNumber);
                    System.out.print("  >> ");
                    break;

                case "connect" :
                    if (inputTokens.length > 2){
                        try{
                            //connect <destinationIP> <destinationPort> <myPort> <myIP>
                            String portNumber_STR = "" + portNumber;
                            chat.connect(inputTokens[1], inputTokens[2], portNumber_STR, chat.myip());
                            
                        }catch(Exception e) {
                            System.out.print("\n\tError : " + e + "\n\tNot connected.\n\n  >> ");
                        }
                    }
                    else 
                        System.out.print("\tError: Not enough arguements.\n\n  >> ");
                    break;

                case "list" :
                    chat.list(server);
                    System.out.print("  >> ");
                    break;

                case "terminate" :
                    if (inputTokens.length > 1)
                        chat.terminate(inputTokens[1], server);
                    else 
                        System.out.println("\tError: Not enough arguements.\n");
                    
                    System.out.print("  >> ");
                    break;

                case "send" :
                    if (inputTokens.length > 2){ 
                        int throwAway = inputTokens[0].length() + inputTokens[1].length() + 2;
                        String message = input.substring(throwAway);
                        chat.send(user, inputTokens[1], message, server);
                    } else 
                        System.out.println("\tError: Not enough arguements.");
                        
                    System.out.print("\n  >> ");
                    break;

                case "exit" :
                    chat.exit(server);
                    break endProgram;

                case "" :
                    System.out.print("  >> ");
                    break;

                default :
                    System.out.println(""
                            + "\tError: '" + inputTokens[0] + "' is not a recognized command of this chat application. "
                            + "\n\tEnter 'help' to get a list of commands.\n");
                            
                    System.out.print("  >> ");
                    break;
            }
        }
        System.out.println("\tConnections closed. Program terminating.");
    }
}
