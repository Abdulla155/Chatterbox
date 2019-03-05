import java.util.*;

public class chat {

    // Display information about the available user interface options or command
    // manual.
    static void help() {

    }

    // Display the IP address of this process.
    // Note: Not “Local” address (127.0.0.1). The actual IP of the computer.
    static void myip() {

    }

    // Display the port on which this process is listening for incoming
    // connections.
    static void myport() {

    }

    // This command establishes a new TCP connection to the specified
    // <destination> at the specified < port no>. The <destination> is the IP
    // address of the computer. Any attempt to connect to an invalid IP should
    // be rejected and suitable error message should be displayed. Success or
    // failure  in  connections  between  two  peers  should  be  indicated  by
    // both  the  peers  using  suitable  messages.  Self-connections and
    // duplicate connections should be flagged with suitable error messages
    static void connect(String input) {

    }

    // Display a numbered list of all the connections this process is part of.
    // This numbered list will include connections  initiated  by  this  process
    // and  connections  initiated  by  other  processes.  The  output  should
    // display the IP address and the listening port of all the peers the
    // process is connected to.
    static void list() {

    }

    // This  command  will  terminate  the  connection  listed  under  the
    // specified number  when  LIST  is  used  to  display  all  connections.
    // E.g.,  terminate  2.  In  this  example,  the  connection  with
    // 192.168.21.21  should  end.  An  error  message  is  displayed  if  a
    // valid  connection  does  not  exist  as number 2. If a remote machine
    // terminates one of your connections, you should also display a message
    static void terminate(String input) {

    }

    // (For example, send 3 Oh! This project is a piece of cake). This will send
    // the message to the host on the connection that is designated by the
    // number 3 when command “list” is used.  The  message  to  be  sent  can
    // be  up-to  100  characters  long,  including  blank  spaces.  On
    // successfully  executing  the command,  the  sender  should  display
    // “Message  sent  to  <connection  id>”  on  the  screen.  On  receiving
    // any  message  from  the  peer,  the  receiver  should  display  the
    // received  message  along  with  the  sender information.
    // (Eg. If a process on 192.168.21.20 sends a message to a process on
    // 192.168.21.21 then the output on 192.168.21.21 when receiving a message
    // should display as shown:
    //
    // Message received from 192.168.21.20
    // Sender’s Port: <The port no. of the sender>
    // Message: “<received message>”
    static void send(String input) {

    }

    // Close all connections and terminate this process. The other peers should
    // also update their connection list by removing the peer that exits
    static void exit() {

    }

    //////////////////// MAIN ////////////////////

    public static void main(String[] args) {

        System.out.println("Howdy partner!\nYour port number is: " + args[0]);

        endProgram:
        while (true) {
            System.out.print("\n\t>> ")
            Scanner scan = new Scanner(System.in);
            String input = scan.nextLine();
            String[] inputTokens = input.split(" ", 2);
            switch (inputTokens[0]) {
                case "help" :
                    help();
                    break;
                case "myip" :
                    myip();
                    break;
                case "myport" :
                    myport();
                    break;
                case "connect" :
                    connect(inputTokens[1]);
                    break;
                case "list" :
                    list();
                    break;
                case "terminate" :
                    terminate(inputTokens[1]);
                    break;
                case "send" :
                    send(inputTokens[1]);
                    break;
                case "exit" :
                    exit();
                    break endProgram;
                default :
                    System.out.println("Error: '" + inputTokens[0] + "' is not "
                            + "a recognized command of this chat application.");
                    break;
            }
        }

    }

}
