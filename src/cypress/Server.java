package cypress;

import java.io.*;
import java.net.*; 
import java.util.*;


public class Server {
	    private static final String USAGE = "Usage: java ChatServer";

	    /** Default port number on which this server to be run. */
	    private static final int PORT_NUMBER = 60101;
	    private static final int MCAST_PORT = 60001;
	    private List<PrintWriter> clients = null;

	    /** Creates a new server.
	     */ 
	    public Server() {
	        clients = new LinkedList<PrintWriter>();
	    }
	    
	    /** Starts the server. */
	    public void startServer() {
	        System.out.println("AndyChat server started on port "
	                           + PORT_NUMBER + "!");
	        try {
	            ServerSocket s = new ServerSocket(PORT_NUMBER);
	            new broadcastDetails().start(); 
	            for (;;) {
	                Socket incoming = s.accept();
	                new ClientHandler(incoming).start();
	                System.out.println("Connected to a Client with address: "+incoming.getInetAddress().getHostAddress());
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        System.out.println("AndyChat server stopped."); 
	    }
	    
	    
	    /**
	     * 
	     */
	    private class broadcastDetails extends Thread{
	    	public void run() {
		    	try {
		    		byte[] buf = new byte[256];
		            // don't wait for request...just send a quote
	
		            String dString = "Hello!! This is Cypress server";
		            buf = dString.getBytes();
		            InetAddress group = InetAddress.getByName("224.0.0.123");
		            
		            DatagramSocket socket = new DatagramSocket(MCAST_PORT);
		            DatagramPacket packet;
		            packet = new DatagramPacket(buf, buf.length, group, MCAST_PORT);	            
		            Integer cntr = 0;
		            for (;;cntr++) {
		            	if(cntr == 20){
		            		System.out.println("Server broadcast");
		            		cntr = -1;
		            	}
		            	sleep(100);
		            	socket.send(packet);
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
	    	}
	    }
	    

	    /** Adds a new client identified by the given print writer. */
	    private void addClient(PrintWriter out) {
	        synchronized(clients) {
	            clients.add(out);
	        }
	    }

	    /** Adds the client with given print writer. */
	    private void removeClient(PrintWriter out) {
	        synchronized(clients) {
	            clients.remove(out);
	        }
	    }

	    /** Broadcasts the given text to all clients. */
	    private void broadcast(String msg) {
	        for (PrintWriter out: clients) {
	            out.println(msg);
	            out.flush();
	        }
	    }

	    public static void main(String[] args) {
	        if (args.length > 0) {
	            System.out.println(USAGE);
	            System.exit(-1);
	        }
	        new Server().startServer();
	    }

	    
	    
	    
	    /** A thread to serve a client. This class receive messages from a
	     * client and broadcasts them to all clients including the message
	     * sender. */
	    private class ClientHandler extends Thread {

	        /** Socket to read client messages. */
	        private Socket incoming; 

	        /** Creates a hander to serve the client on the given socket. */
	        public ClientHandler(Socket incoming) {
	            this.incoming = incoming;
	        }

	        /** Starts receiving and broadcasting messages. */
	        public void run() {
	            PrintWriter out = null;
	            try {
	                out = new PrintWriter(
	                        new OutputStreamWriter(incoming.getOutputStream()));
	                
	                // inform the server of this new client
	                Server.this.addClient(out);

	                out.print("Welcome to AndyChat! ");
	                out.println("Enter BYE to exit."); 
	                out.flush();

	                BufferedReader in 
	                    = new BufferedReader(
	                        new InputStreamReader(incoming.getInputStream())); 
	                for (;;) {
	                    String msg = in.readLine(); 
	                    if (msg == null) {
	                        break; 
	                    } else {
	                        if (msg.trim().equals("BYE")) 
	                            break; 
	                        System.out.println("Received: " + msg);
	                        // broadcast the receive message
	                        Server.this.broadcast(msg);
	                    }
	                }
	                incoming.close(); 
	                Server.this.removeClient(out);
	            } catch (Exception e) {
	                if (out != null) {
	                    Server.this.removeClient(out);
	                }
	                e.printStackTrace(); 
	            }
	        }
	    }
}
