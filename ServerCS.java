package firstTry;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerCS implements Runnable{

	int myNodeId, myPortNo;
	Requests msgQueue;
	Socket client;
	String msgFromClient;
	Thread t;
	BufferedReader inFromClient = null;
    DataOutputStream outToClient = null;
	
	//This constructor is for individual nodes
	public ServerCS(int myNodeId, int myPortNo, Requests msgQueue, Socket client){
		this.myNodeId = myNodeId;
		this.myPortNo = myPortNo;
		this.msgQueue = msgQueue;
		this.client = client;
		msgFromClient = null;
		
		//Thread creation
		t = new Thread(this, "CSNodeClient");
		t.start();
	}//Constructor ends here

	@Override
	public void run() {
		
		while(true){
			try {
				inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
				
				msgFromClient = inFromClient.readLine();
				
				if(msgFromClient==null){
					continue;
				}
				else{
					System.out.println("\n\n"+t.getName()+"\tMsg from client: " + msgFromClient);
					
					//Format of the string will be "nodeId reqId cst"
							
					synchronized (msgQueue) {
							msgQueue.put(msgFromClient); //Put method
							System.out.println(t.getName()+"\tAfter putting, size of msgQueue: " + msgQueue.getMsgQueue().size());
						}
					}
			} catch (IOException e) {
				//TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
