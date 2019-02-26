package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerHandler  extends Thread   {
	
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
    final Socket s;
    private int id;
    // Constructor 
    public ServerHandler(Socket s, ObjectInputStream ois, ObjectOutputStream oos, int port)  throws IOException 
    { 
        this.s = s; 
        this.ois = ois; 
        this.oos = oos;
    }
    
    @Override
    public void run() {
    	
    }

}
