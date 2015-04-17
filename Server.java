package firstTry;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server implements Runnable{

	int myNodeId, myPortNo, cst;
	
	Chkforrequests qThread;
	ServerSocket ss;
	Socket listener;
	Thread t;
	Client client;
	Fileinput file;
	Quorumclients quorum;
	Requestque msgQueue;
	Requests q;
	ExecuteCS csThread;
	ServerCS csClient;
	HashMap<Integer, Socket> csNodeSockets;
	Clock clock;
	
	//This constructor is for the individual nodes
	public Server(int myClientnum, int myPortNo, Fileinput file, Quorumclients quorum, Requestque msgQueue, Clock clock){
		this.myNodeId = myClientnum;
		this.myPortNo = myPortNo;
		this.file = file;
		this.quorum = quorum;
		this.msgQueue = msgQueue;
		this.clock = clock;
	//** Spawn a thread to read the Message Queue continuously
		qThread = new Chkforrequests(myClientnum, myPortNo, msgQueue, quorum, file, clock);
		
		//Thread creation
		t = new Thread(this, "Server");
		t.start();
	}//Constructor ends here
	
	
	public Server(int myNodeId, int myPortNo, Fileinput file, Requests q, HashMap<Integer, Socket> csNodeSockets){
		this.myNodeId = myNodeId;
		this.myPortNo = myPortNo;
		this.file = file;
		this.q = q;
		this.csNodeSockets = csNodeSockets;
		csThread = new ExecuteCS(myNodeId, myPortNo, q, file, csNodeSockets);
		
		//Thread creation
		t = new Thread(this, "CSNode_Server");
		t.start();
	}
	
	@Override
	public void run(){
		try {
			ss = new ServerSocket(myPortNo);
			
			System.out.println("\n\n"+t.getName()+"\tStarting the msgQueue thread...");
			//qThread.start();
			
			System.out.println(t.getName()+"\tServer has started for node "+myNodeId+".");
			System.out.println(t.getName()+"\tNow listening for connections...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(true){
			try {
				listener = ss.accept();

							
				System.out.println(t.getName()+"\tConnection is established");
			} catch (IOException e) {
			// 	TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			if(listener != null){
				System.out.println(t.getName()+"\tCreating a new client thread and starting communication with it...");
				
				if(myNodeId!=999) //If not CsNode then create a simple client
					client = new Client(myNodeId, myPortNo, quorum, msgQueue, listener, file, clock);
				else
					csClient = new ServerCS(myNodeId, myPortNo, q, listener);
			}
			
		}
	}
	
	public void serverShutDown() throws IOException{
		listener.close();
		ss.close();
	}
}