package Model;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.util.ArrayList;

public class MessageServer {

    private ServerSocket serverSocket;


    public MessageServer(int port) throws IOException, ClassNotFoundException {
        System.out.println("Server started");
        new Connection(port).start(); //Skapa ett Connection-objekt som lyssnar p� porten--> k�rs med egen tr�d (Connection �rver Thread)
    }


    private class Connection extends Thread {

        private int port; //servern beh�ver veta till vilken port den ska lyssna


        public Connection(int port) throws IOException {

            this.port = port;
            serverSocket = new ServerSocket(port);
        }

        @Override
        public void run() {

            System.out.println("Waiting for connection...");

            while (true) {

                try {
                    Socket socket = serverSocket.accept();//lyssnar p� socket
                    new ClientHandler(socket).start();//skapar en ny ClientHandler --> l�mna processen till en tr�d f�r varje uppkopplad Client
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }
    }


    private class ClientHandler extends Thread {

        private Socket socket;
        private String username;// den anv�ndare som just kopplat upp sig
        private ArrayList<ClientHandler> clientHandlers = new ArrayList<>();  //ett antal anv�ndare som ansluter sig
        private ObjectInputStream ois;
        private ObjectOutputStream oos;


        public ClientHandler(Socket socket) throws IOException {
            System.out.println("A new Client connected");
            try {
                this.socket = socket;
                this.oos = new ObjectOutputStream(socket.getOutputStream());
                this.ois = new ObjectInputStream(socket.getInputStream());//Vid uppkoppling skapas ObjectInputStream som kopplas till server-sockets InputStream
                this.username= (String) ois.readObject();
                this.clientHandlers.add(ClientHandler.this);
                broadcastMessage("Server: " + username + " has entered the chat!");

            } catch (Exception e) {
                System.out.println("ClientHandler: " + e);
            }
        }

        @Override
        public void run() { //v�ntar f�r meddelande fr�n den uppkopplade client--> skickar den till andra clients.
            String messages;
            System.out.println("ClientHandler run starting...");

            while (socket.isConnected()) {

                try {
                    messages = (String) ois.readObject();
                    System.out.println(messages);
                    broadcastMessage(messages);
                } catch (Exception e) {
                    break;
                }
            }//while-loopen
        }//run()



        private void broadcastMessage(String message) { //skickar meddelande till alla som �r i chatten (f�rutom den som skickar meddelandet!)

            for (ClientHandler clientHandler : clientHandlers) {
                try {
                    if (!clientHandler.username.equals(username)) {
                        System.out.println("broadcastMessage k�rs!");
                        clientHandler.oos.writeObject(message);
                        clientHandler.oos.flush();
                    }
                } catch (IOException e) {
                }
            }
        }
    }//ClientHandler




    public static void main(String[] args) throws IOException, ClassNotFoundException {
        MessageServer messageServer = new MessageServer(3343);
        //System.out.println(InetAddress.getLocalHost().getHostAddress());


    }
}