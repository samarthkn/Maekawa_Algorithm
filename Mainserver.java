package firstTry;

import java.io.IOException;
//import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

// 

public class Mainserver {

	public static void main(String args[]) throws NumberFormatException, IOException{
		int clientnum, myPortNo;
		Fileinput file;
		Requests msgQueue;
		HashMap<Integer, Socket> csNodeSockets = new HashMap<Integer, Socket>(); 
		
		//Assigning nodeId = 999 to csNode. This is hard coded
		clientnum = 999;
		
		//Initialize the file object
		file = new Fileinput();
		file.scanInputFile(clientnum);
		
		//Get my portNo
		myPortNo = file.getPortNo(clientnum);
		
		//Initialize the msgQueue
		msgQueue = new Requests();
				
		//Create the server for this node
		new Server(clientnum, myPortNo, file, msgQueue, csNodeSockets);
	}
}
