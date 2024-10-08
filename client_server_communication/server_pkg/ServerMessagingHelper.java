package client_server_communication.server_pkg;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ServerMessagingHelper extends ClientHandler {

    public ServerMessagingHelper(Socket socket) {
        super(socket);
    }

    // Redirect message as applicable
    public static void redirect_to_correct_message_routine(Integer sender_id, String message) throws IOException {
        // Check message type
        if(message.matches("^brm-.*")) {
            // Broadcast message
            send_to_all_clients(sender_id, message.substring(4));
        } else if(message.matches("^dm-.*")) {
            // Direct message
            // Get index of first underscore after user ID, to split message properly
            int next_dash = message.indexOf("-", 3);
            int reciever_id = Integer.parseInt(message.substring(3, next_dash));
            Set client_ids = ServerUtils.get_client_ids();
            // Send message to client
            // Check we have a valid id, if not, ignore
            if(client_ids.contains(reciever_id)) {
                send_to_client(sender_id,reciever_id, message.substring(next_dash + 1));
            } else {
                System.out.println("Client " + reciever_id + " not found. Direct message from " + sender_id +" ignored.");
            }

        } else if(message.matches("^grp-details$") || message.matches("^act-grp-details$")) {
            // Group details request
            send_group_details(sender_id);
        }
    }

    // Send to all clients (broadcast message)
    private static void send_to_all_clients(Integer sender_id, String message) throws IOException {

        // Loop through and send message back to each client as needed
        for (Integer client_id : client_details.keySet()) {
            try {
                // Get socket for user to send data to, then create new writer
                UserDetails user_details = client_details.get(client_id);
                Socket client_socket = user_details.socket;
                PrintWriter output = new PrintWriter(client_socket.getOutputStream(), true);
                // Send data (only to non-senders)
                if(!Objects.equals(sender_id, client_id)) {
                    output.println(sender_id + " broadcasted: " + message);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    // Send to all clients (private message)
    private static void send_to_client(Integer sender_id, Integer receiver_id, String message) throws IOException {
        // Loop through and send message back to each client as needed
        try {
            // Get socket for user to send data to, then create new writer
            UserDetails user_details = client_details.get(receiver_id);
            Socket client_socket = user_details.socket;
            PrintWriter output = new PrintWriter(client_socket.getOutputStream(), true);

            // Send data: If sender_id is 0, this is the server, tell the receiver accordingly
            output.println(sender_id != 0 ? sender_id + ": " + message : "Server: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send_group_details(Integer requester_id) throws IOException {
        // First, convert the client_details to well formatted line-by-line strings in an array
        List<String> formatted_group_details = new ArrayList<String>();
        for (Integer client_id : client_details.keySet()) {
            // Get socket for each user
            UserDetails user_details = client_details.get(client_id);
            Socket client_socket = user_details.socket;
            // Format data nicely and add to list
            formatted_group_details.add("Client: " + client_id + ", connected at ip: " + client_socket.getLocalAddress() + ", port: " + client_socket.getLocalPort() +". This user " + (user_details.is_coordinator ? "is" : "is not") + " the coordinator.");
        }

        // Next, send each item in the array on a seperate line
        try {
            UserDetails user_details = client_details.get(requester_id);
            Socket client_socket = user_details.socket;
            PrintWriter output = new PrintWriter(client_socket.getOutputStream(), true);
            for(String details : formatted_group_details) {
                output.println(details);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send_new_coordinator_info(Integer new_coordinator) throws IOException {
        // Get socket details
        UserDetails user_details = client_details.get(new_coordinator);
        Socket client_socket = user_details.socket;
        PrintWriter output = new PrintWriter(client_socket.getOutputStream(), true);

        // Send coordinator
        output.println("coord");
    }

    public static void send_coordinator_info(Integer receiver_id) throws IOException {
        // Get socket details
        UserDetails user_details = client_details.get(receiver_id);
        Socket client_socket = user_details.socket;
        PrintWriter output = new PrintWriter(client_socket.getOutputStream(), true);

        // Send coordinator
        output.println("The coordinator is currently: " + current_coordinator);
    }

}
