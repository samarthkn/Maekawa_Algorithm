package firstTry;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Iterator;

public class Mainclient {
	@SuppressWarnings("unused")
	public static void main(String args[]) throws FileNotFoundException, IOException, InterruptedException{
		int clientnumber, myPortNo1;
    	Fileinput file;
    	Requestque msgQueue;
		Mainclient run;
		Clock clock;
		Quorumclients quornum;
		Server server;
		
		Thread t = Thread.currentThread();
		t.setName("Main");
		Socket client;
		DataOutputStream Toclient;
		
		//Get the node from the keyboard
		System.out.println("Enter the Client number : ");
		BufferedReader value = new BufferedReader(new InputStreamReader(System.in));
		clientnumber = Integer.parseInt(value.readLine());
		System.out.println("I am client number : "+clientnumber);
		value.close();
				
		//Initialize the quorum object, and all the variables,from its constructor
		quornum = new Quorumclients(clientnumber);
		
		//Initialize the file object
		
		//**** Gets IP addresses and port no's, and print all of them
		file = new Fileinput();
		//*** Read the input file,figure out the nodes of the node to be in quorum
		file.scanInputFile(clientnumber, quornum);
					
				//Get the portNo for the particular node id
		myPortNo1 = file.getPortNo(clientnumber);
		
		//Initialize the msgQueue
		msgQueue = new Requestque();
		
		//Initialize clock
		clock = new Clock();
		
		//Create the server for this node
		server = new Server(clientnumber, myPortNo1, file, quornum, msgQueue, clock);
		
			//Send the "begin execution" to all the nodes if this node is 0
		if(clientnumber == 0){
			
			
			int numofclients = file.getNumNodes();
			
			for(int i=1 ; i<numofclients ; i++){
				String destIpAddress = file.getIpAddressList().get(i);
				int destPortNo = file.getPortList().get(i);
								
			
				client = new Socket(destIpAddress, destPortNo);
				Toclient = new DataOutputStream(client.getOutputStream());
				
				String msg = "BEG";
				Toclient.writeBytes(msg+"\n");
				System.out.println("Sent msg: "+msg);
				
				Toclient.close();
				client.close();
			}
			
			quornum.setBeginExecution(true); //Set own boolean to true to enter man method
			Thread.sleep(1000);
		}//Begin execution ends here

		
		
		while(true){
			if(quornum.isBeginExecution()){
				for(int i=0 ; i<file.getNumReq() ; i++){
				
					while(true){ //Check if REQ can be sent
						
						if(quornum.isCanSendReq()){
							
							synchronized(quornum){

								
								System.out.println(t.getName() + "\t ______Send requests_____");
								//Can send REQ
							    String msgToSend;
							    
							    //Ticking clock for SEND event
							    clock.tick(0, 0);
								int clockTime = clock.getClockVal();
								
								//To request to enter the critical section
								Iterator<Integer> it = quornum.getQuorumList().iterator();
								
								while(it.hasNext()){
									
									int destNode = it.next();
									String destIpAdd = file.getIpAddressList().get(destNode);
									int destPort = file.getPortList().get(destNode);
									
									//Check for socket for the current quorum element
									if(quornum.getQuorumSockets().containsKey(destNode)){
										client = quornum.getQuorumSockets().get(destNode);
									}else{							
										client = new Socket(destIpAdd, destPort);
										quornum.getQuorumSockets().put(destNode, client);
									}
									
									Toclient = new DataOutputStream(client.getOutputStream());
									
									msgToSend = "REQ " + clientnumber + " " + clockTime;
									Toclient.writeBytes(msgToSend+"\n");
									System.out.println(t.getName()+ "\tMsg sent: " + msgToSend + " to node: " + destNode);
									
								}
							
								//Has sent all REQ for CS execution
								quornum.setCanSendReq(false);
								break;
							
							}
						}
						else{
							Thread.sleep(200);
							continue;
						}
					}
					
				}//For loop ends here
				System.out.println("\n\n"+t.getName()+"\t--------------Done with sending requests. Exiting---------------");
				break;
			}
			else{
				//System.out.println(t.getName()+"\tNot entered main method");
				Thread.sleep(200);
				continue;
			}//If ends here
		}//While true ends here
		
		//This part will send the final finished execution to the nodes in its quorum
		Thread.sleep(60000);
		if(clientnumber == 0){
			//clock.tick(0, 0);
			
			int numNodes = file.getNumNodes();
			
			for(int i=0 ; i<numNodes ; i++){
							
				client = quornum.getQuorumSockets().get(i);
				Toclient = new DataOutputStream(client.getOutputStream());
				
				String msg = "REP";
				Toclient.writeBytes(msg+"\n");
				System.out.println("Sent msg: "+msg);
				
			}
			
			Thread.sleep(10);
		}
	}
}