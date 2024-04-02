package client_server_communication.client_pkg;

public class ClientUtils {
    public static void initial_menu(Integer user_id, Boolean is_coordinator) {
        System.out.println("Welcome " + user_id + "! Please read the following information before continuing");
        if(is_coordinator) System.out.println("NOTE: You are the current coordinator");
        System.out.println("To send a broadcast message, start with: 'brm-'...");
        System.out.println("To send a direct message, start with: 'dm-USERIDHERE-'...");
        System.out.println("To get details of all group members, enter 'grp-details'");
        System.out.println("To exit/leave the chat program, press ctrl+C");
    }
}
