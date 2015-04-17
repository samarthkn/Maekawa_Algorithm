package firstTry;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;

public class Client implements Runnable{

	static int threadNo = 0;
	static int reqId = 0, noToks = 0, noRels = 0, noFails = 0, noInqs = 0, noYlds = 0;
	int myNodeId, myPortNo;
	Fileinput file;
	Quorumclients quorum;
	Requestque q;
	Thread t;
	Socket client;
	BufferedReader inFromClient = null;
    DataOutputStream outToClient = null;
    String msgFromClient;
    Clock clock;
	
    //This constructor is for individual nodes
	public Client(int myNodeId, int myPortNo, Quorumclients quorum, Requestque msgQueue, Socket client, Fileinput file, Clock clock){
		this.myNodeId = myNodeId;
		this.myPortNo = myPortNo;
		this.quorum = quorum;
		this.q = msgQueue;
		this.client = client;
		this.file = file;
		this.clock = clock;
		msgFromClient = null;
		
		//Thread creation
		t = new Thread(this, "Client-"+threadNo++);
		t.start();
	}//Constructor ends here
	
	public void run(){
		
		try {
			inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		};;
		
		while(true){
			try {
				
				synchronized(this){
					msgFromClient = inFromClient.readLine();
				}
				
				if(msgFromClient==null){
					continue;
				}else{
					
					
					System.out.println("\n\n"+t.getName()+"\tNew clock value: "+clock.getClockVal());
					
					//Msg received from client
					System.out.println(t.getName()+"\tMsg from client: " + msgFromClient);
					
					
					
					if(msgFromClient.contains("BEG")){
						quorum.setBeginExecution(true);
						System.out.println(t.getName()+"\t----------BEGIN EXECUTION------------");
					}
					if(msgFromClient.contains("REQ")){
						//Function call for a request
						processReq();
						continue;
					}
					if(msgFromClient.contains("TOK")){
						noToks++;
						
						String words[] = msgFromClient.split(" ");
						int tokenFromNode = Integer.parseInt(words[1]);
						
						if(quorum.getPermissionGivenTo()==tokenFromNode)
							System.out.println(t.getName()+"\t********* Received PERMISSION of OWN node *********");
						else
							System.out.println(t.getName()+"\t********* Received PERMISSION from ANOTHER node *********");
						
						//Remove the nodeId from the list of tokenPending
						quorum.removePermissionPending(tokenFromNode);
						
						//Check if it can enter critical section
						synchronized(this){
							if(quorum.canEnterCritSec() && !quorum.isCstExec()){
								critSecExec(myNodeId, quorum, file, clock);
								
								//Setting boolean to show that currently executing CS
								quorum.setCstExec(true);
							}
							else{
								System.out.println(t.getName() + "\tCAN'T ENTER CRITICAL SECTION YET. PERMISSIONS PENDING FROM: ");
								quorum.printPermissionsPendingList();
							}
						}
					}
					else if(msgFromClient.equals("ACK "+myNodeId)){
						System.out.println(t.getName() + "\tReceived ACK msg from CsNode");
						
						//Setting boolean to send requests for next CS execution
						quorum.setCanSendReq(true);
						
						//Setting boolean to show not in CS
						quorum.setCstExec(false);
						
						//Clearing recvFailed boolean
						quorum.setRecvdFail(false);
						
						//***** Re-populating tokensPending list will be done from the following method ******
						
						//Sending RELEASE to the nodes of the quorum
						sendReleaseMsgs(myNodeId, quorum, clock);
						int rnd= random();
						Thread.sleep(rnd); //file.getInterReq()
					}
					else if(msgFromClient.contains("REL")){
						processRel();
					}
					else if(msgFromClient.contains("FAIL")){ 
						noFails++;
						
						//Setting recvdFail boolean to true
						quorum.setRecvdFail(true);
						
						synchronized(this){
							//Send YLD to all those nodes whose INQ is yet to be answered
							if(quorum.getRecvdInqFrom().size() > 0){
								System.out.println(t.getName() + "\tFound pending INQ. Sending FAIL to them");
								sendYldForInq(myNodeId, quorum, clock);
							}
						}
						
					}
					else if(msgFromClient.contains("INQ")){
						noInqs++;
						
						//INQ reqNodeId clockTime
						String words[] = msgFromClient.split(" ");
						int reqNodeId = Integer.parseInt(words[1]);
						
						if(quorum.isCstExec()){
							//Ignore this INQ as a REL will be sent later on
						}
						else{
							if(quorum.getPermissionsPending().contains(reqNodeId)){
								//If token from the node that sent INQ has not been received, the INQ has no meaning.
								//Do nothing
								continue;
							}
							else{ 							
								//If token from the node that sent INQ has been received
								if(quorum.isRecvdFail()){
									//If a FAIL has already been received
									sendYield(myNodeId, reqNodeId, quorum, clock);
									
									//It sent a YIELD and so the token from that node is pending again
									quorum.getPermissionsPending().add(reqNodeId);
									
								}
								else
									quorum.getRecvdInqFrom().add(reqNodeId);
							
							}
						}
											
					}
					//Cases for YLD and REQ are solved here
					else if(msgFromClient.contains("YLD")){
						
						//Have to add the request from the node that yielded into the msgQueue again because it has not been serviced
						msgFromClient = quorum.getReqWithToken();
						//Setting canTransferLock to grant TOKEN to headOfQueue
						quorum.setTransferLock(true);
						//The following part will take care to add it into the request queue again
												
						synchronized (q) {
							q.put(msgFromClient); //Put method
							System.out.println(t.getName()+"\tAfter putting, size of msgQueue: " + q.getQueue().size());						
						}
					}
					else if(msgFromClient.contains("REP")){
						reportToNode();
					}
					else if(msgFromClient.contains("FIN")){
						
						processFin(msgFromClient);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//continue;
		}
	}
	//Run ends here
	
	public void critSecExec(int myNodeId, Quorumclients quorum, Fileinput file, Clock clock) throws InterruptedException, UnknownHostException, IOException{
		System.out.println(t.getName()+"\t__________ CRITICAL SECTION EXECUTION REQUEST____________");
		
		int csNode = 999;
		Socket client=null;
		DataOutputStream outToClient;
	    String msgToSend;
	    
		String destIpAdd = file.getIpAddressList().get(csNode);
		int destPort = file.getPortList().get(csNode);
		
		//Ticking clock for SEND event
		//clock.tick(0, 0);
		
		if(quorum.getQuorumSockets().containsKey(csNode))
			client = quorum.getQuorumSockets().get(csNode);
		else{
			client = new Socket(destIpAdd, destPort);
			quorum.getQuorumSockets().put(csNode, client);
		}
		
		try {
			outToClient = new DataOutputStream(client.getOutputStream());
			msgToSend = myNodeId + " " + reqId++ + " " + file.getCritSecTime();
			outToClient.writeBytes(msgToSend+"\n");
			System.out.println(t.getName()+"\tSent Msg: "+msgToSend+" to: "+csNode);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendReleaseMsgs(int myNodeId, Quorumclients quorum, Clock clock) throws UnknownHostException, IOException, InterruptedException{
		System.out.println("__________ SENDING RELEASE MSGS ____________");
		
		Socket client = null;
	    DataOutputStream outToClient = null;
	    String msgToSend;
		Iterator<Integer> it = quorum.getQuorumList().iterator();
		
		while(it.hasNext()){
			noRels++;
			
			int destNode = it.next();
			String destIpAdd = file.getIpAddressList().get(destNode);
			int destPort = file.getPortList().get(destNode);
			System.out.println(t.getName() + "\tdestPort: " + destPort);
			
			//Ticking clock for SEND event
			//clock.tick(0, 0);
			
			//Check for socket for the current quorum element
			if(quorum.getQuorumSockets().containsKey(destNode)){
				System.out.println(t.getName() + "\tGetting old socket");
				client = quorum.getQuorumSockets().get(destNode);
			}else{			
				System.out.println(t.getName() + "\tCreating new socket");
				client = new Socket(destIpAdd, destPort);
				quorum.getQuorumSockets().put(destNode, client);
			}
			
			try {
				outToClient = new DataOutputStream(client.getOutputStream());
				msgToSend = "REL " + myNodeId + " " + clock.getClockVal();
				outToClient.writeBytes(msgToSend+"\n");
				System.out.println(t.getName()+"\tSent Msg: "+msgToSend+" to: "+destNode);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
			//Add destNode to the tokens pending list
			quorum.getPermissionsPending().add(destNode);
		}
		
		//Print the list of tokens pending
		System.out.println(t.getName() + "\tAfter sending RELEASE msgs POPULATING tokensPendingList: ");
		quorum.printPermissionsPendingList();
	}
	
	public void sendYield(int myNodeId, int destNode, Quorumclients quorum, Clock clock) throws InterruptedException, UnknownHostException, IOException{
		System.out.println("______SENDING YIELD MSG______");
		noYlds++;
		
		Socket client;
	    DataOutputStream outToClient;
	    String msgToSend;
	    
	    String destIpAdd = file.getIpAddressList().get(destNode);
		int destPort = file.getPortList().get(destNode);
		System.out.println("\n"+t.getName()+"\tDestNode: "+destNode+" DestIpAdd: "+destIpAdd+" DestPort: "+destPort);
		
		//clock.tick(0, 0);
		
		//Check for socket for the current quorum element
		if(quorum.getQuorumSockets().containsKey(destNode)){
			client = quorum.getQuorumSockets().get(destNode);
		}else{
			client = new Socket(destIpAdd, destPort);
			quorum.getQuorumSockets().put(destNode, client);
		}
		
		outToClient = new DataOutputStream(client.getOutputStream());
		
		msgToSend = "YLD " + myNodeId + " " + clock.getClockVal(); 
		outToClient.writeBytes(msgToSend+"\n");
		System.out.println(t.getName()+ "\tMsg sent: " + msgToSend);
	}
	
	public void sendInq(int myNodeId, int destNode, Quorumclients quorum, Clock clock) throws UnknownHostException, IOException, InterruptedException{
		System.out.println("_____SENDING INQUIRE MSG____");
		
		Socket client;
		//BufferedReader inFromClient;
	    DataOutputStream outToClient;
	    String msgToSend;
		//To request to enter the critical section
	    
	    String destIpAdd = file.getIpAddressList().get(destNode);
		int destPort = file.getPortList().get(destNode);
		
		//clock.tick(0, 0);
		
		//Check for socket for the current quorum element
		if(quorum.getQuorumSockets().containsKey(destNode)){
			client = quorum.getQuorumSockets().get(destNode);
		}else{
			client = new Socket(destIpAdd, destPort);
			quorum.getQuorumSockets().put(destNode, client);
		}
		
		outToClient = new DataOutputStream(client.getOutputStream());
		
		msgToSend = "INQ " + myNodeId + " " + clock.getClockVal();
		outToClient.writeBytes(msgToSend+"\n");
		System.out.println(t.getName()+ "\tMsg sent: " + msgToSend + " to node: " + destNode);
	}
	
	public void sendFail(int myNodeId, int destNode, Quorumclients quorum, Clock clock) throws UnknownHostException, IOException, InterruptedException{
		System.out.println("______SENDING FAILED MSG______");
		
		Socket client;
	    DataOutputStream outToClient;
	    String msgToSend;
	    
	    String destIpAdd = file.getIpAddressList().get(destNode);
		int destPort = file.getPortList().get(destNode);
		
		//clock.tick(0, 0);
		
		//Check for socket for the current quorum element
		if(quorum.getQuorumSockets().containsKey(destNode)){
			client = quorum.getQuorumSockets().get(destNode);
		}else{
			client = new Socket(destIpAdd, destPort);
			quorum.getQuorumSockets().put(destNode, client);
		}
		
		outToClient = new DataOutputStream(client.getOutputStream());
		
		msgToSend = "FAIL " + myNodeId + " " + clock.getClockVal();
		outToClient.writeBytes(msgToSend+"\n");
		System.out.println(t.getName()+ "\tMsg sent: " + msgToSend + " to node: " + destNode);
	}
	
	public void sendYldForInq(int myNodeId, Quorumclients quorum, Clock clock) throws InterruptedException, UnknownHostException, IOException{
		System.out.println("____ SENDING YLD FOR PENDING INQ MSGS ______");
		
		Socket client = null;
	    DataOutputStream outToClient = null;
	    String msgToSend;
		Iterator<Integer> it = quorum.getRecvdInqFrom().iterator();
		
		while(it.hasNext()){
			noYlds++;
			
			int destNode = it.next();
			String destIpAdd = file.getIpAddressList().get(destNode);
			int destPort = file.getPortList().get(destNode);
			System.out.println(t.getName() + "\tdestPort: " + destPort);
			
			//Ticking clock for SEND event
			//clock.tick(0, 0);
			
			//Check for socket for the current quorum element
			if(quorum.getQuorumSockets().containsKey(destNode)){
				client = quorum.getQuorumSockets().get(destNode);
			}else{			
				client = new Socket(destIpAdd, destPort);
				quorum.getQuorumSockets().put(destNode, client);
			}
			
			try {
				outToClient = new DataOutputStream(client.getOutputStream());
				msgToSend = "YLD " + myNodeId + " " + clock.getClockVal();
				outToClient.writeBytes(msgToSend+"\n");
				System.out.println(t.getName()+"\tSent Msg: "+msgToSend+" to: "+destNode);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
		}
		
		//Clear the list 
		quorum.getRecvdInqFrom().clear();
	}
	
	public synchronized void processReq() throws UnknownHostException, IOException, InterruptedException{

		//If one client processes request, no one else should
		synchronized(quorum.locked){

			String words[] = msgFromClient.split(" ");
			int reqNode = Integer.parseInt(words[1]);
			int reqPriority = Integer.parseInt(words[2]);
			
			//Ticking clock for reciept of request
			try {
				clock.tick(0, reqPriority);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(quorum.locked.get()){
				System.out.println(t.getName() + "\t ****** Quorum is LOCKED for "+ quorum.getPermissionGivenTo() + " ****** ");
				//**************** If locked, then add to the queue and send INQ or FAIL msgs ***********************
				
				//Putting in queue
				synchronized(q){
					q.put(msgFromClient);
					System.out.println(t.getName() + "\tSize of queue: " + q.getQueue().size());
					System.out.println(t.getName() + "\tCONTENTS OF THE QUEUE: ");
					q.printQueue();
				}										
				
				//Check whether to send INQ or FAIl						
				int currNode = quorum.getPermissionGivenTo();
				int currPriority = quorum.getPermissionGivenToPriority();
				
				//If new request has higher priority, send INQ to the node with the token, JUST ONCE
				if(reqPriority < currPriority && quorum.isCanSendInq()){
					System.out.println(t.getName() + "\t___New REQ has HIGHER priority___");
					sendInq(myNodeId, currNode, quorum, clock);
					
					//Sent an INQ already to node whose is request is being processed. Can't send anymore INQ
					quorum.setCanSendInq(false);
				}
				else if(reqPriority == currPriority && reqNode < currNode && quorum.isCanSendInq()){
					System.out.println(t.getName() + "\t_____ New REQ has SAME priority but LOWER NodeId___");
					sendInq(myNodeId, currNode, quorum, clock);
					
					//Sent an INQ already to node whose is request is being processed. Can't send anymore INQ
					quorum.setCanSendInq(false);
				}
				//If new request has lower priority, send FAIL to requesting node, JUST ONCE
				else{
					
					System.out.println(t.getName() + "\t___FAILED__");
					sendFail(myNodeId, reqNode, quorum, clock);
					
					//If a fail has been sent to the node, further fails can't be sent
					//quorum.getSentFailTo().add(reqNode);
				}
			}
			else{
				System.out.println(t.getName() + "\t ****** Quorum is NOT LOCKED for "+ quorum.getPermissionGivenTo() + " ****** ");
				
				//Sending TOKEN to self
				if(reqNode == myNodeId){
					System.out.println(t.getName()+"\t********* Received TOKEN of OWN node *********");
					
					//Remove the nodeId from the list of tokenPending
					quorum.removePermissionPending(reqNode);
					
					//Check if it can enter critical section
					if(quorum.canEnterCritSec())
						critSecExec(myNodeId, quorum, file, clock);
					else{
						System.out.println(t.getName() + "\tCAN'T ENTER CRITICAL SECTION YET. PERMISSIONS PENDING FROM: ");
						quorum.printPermissionsPendingList();
					}
					
					//Setting details of request for which the TOKEN was sent
					quorum.setReqWithToken(msgFromClient);
					quorum.setPermissionGivenTo(reqNode);
					quorum.setPermissionGivenToPriority(reqPriority);
					
					//New TOKEN granted and so canSendInq and canSendFail booleans should be allowed
					quorum.setCanSendInq(true);
					//quorum.setCanSendFail(true);
					
					//Locking node for current request
					quorum.locked.compareAndSet(false, true);

					//continue;
				}
				else{
					//Sending TOKEN to others
					String destIpAdd = file.getIpAddressList().get(reqNode);
					int destPort = file.getPortList().get(reqNode);
					System.out.println(t.getName() + "\tDestIpAdd: " + destIpAdd + " DestPortNo: " + destPort);
					
					//Setting client according to the quorum hashMap
				    if(quorum.getQuorumSockets().containsKey(reqNode)){
				    	System.out.println(t.getName() + "\tGetting old socket");
						client = quorum.getQuorumSockets().get(reqNode);
				    }
					else{
						try {
							System.out.println(t.getName() + "\tCreating new socket");
							client = new Socket(destIpAdd, destPort);
							quorum.getQuorumSockets().put(reqNode, client);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				    
				    //Ticking clock for SEND event
				    /*try {
						clock.tick(0, 0);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}*/
				    
					//****************** If unlocked, check for no pending requests ***********************

					outToClient = new DataOutputStream(client.getOutputStream());
					String msgToSend = "TOK " + myNodeId + " " + clock.getClockVal();
					
					synchronized(outToClient){
						outToClient.writeBytes(msgToSend+"\n");
					}
					
					System.out.println(t.getName()+ "\t***************** PERMISSION GRANTED TOKEN TO: " + reqNode + " ******************");
					
					//Setting details of request for which the TOKEN was sent
					quorum.setReqWithToken(msgFromClient);
					quorum.setPermissionGivenTo(reqNode);
					quorum.setPermissionGivenToPriority(reqPriority);
					
					//New TOKEN granted and so canSendInq and canSendFail booleans should be allowed
					quorum.setCanSendInq(true);
					
					//Locking node for current request
					quorum.locked.compareAndSet(false, true);
					
				}
			}
		}
	
	}
	
	public synchronized void processRel(){

		System.out.println(t.getName() + "\t =============== RELEASE is received ================");
		String words[] = msgFromClient.split(" ");
		
		int relFromNode = Integer.parseInt(words[1]);
		
		if(quorum.getPermissionGivenTo()==relFromNode){
			//Setting canTransferLock to grant token to the headOfQueue 
			synchronized (q) {
				if(q.isEmpty())
					quorum.locked.compareAndSet(true, false);
				else
					quorum.setTransferLock(true);
			}
		}
		else
			System.out.println(t.getName() + "\t!!!!!!! ERROR: Received REL from node that wasn't given the PERMISSION !!!!!!!!");
		
		System.out.println(t.getName() + "\tPENDING REQUESTS: ");
		q.printQueue();
	
	}
	
	public void reportToNode() throws IOException{
		Socket client;
		DataOutputStream outToClient;
		
		System.out.println(t.getName() + "\t =============== SENDING REPORT TO SERVER1 ================");
		
		client = quorum.getQuorumSockets().get(0);
		outToClient = new DataOutputStream(client.getOutputStream());
		
		String msgToSend = "FIN "+myNodeId+" "+file.getNumReq()+" "+noToks+" "+noRels+" "+noFails+" "+noInqs+" "+noYlds;
		outToClient.writeBytes(msgToSend+"\n");
		
	}
	
	public synchronized void processFin(String msgFromClient){
		String words[] = msgFromClient.split(" ");
		
		int nodeId = Integer.parseInt(words[1]);
		int noReqs = Integer.parseInt(words[2]);
		int noToks = Integer.parseInt(words[3]);
		int noRels = Integer.parseInt(words[4]);
		int noFails = Integer.parseInt(words[5]);
		int noInqs = Integer.parseInt(words[6]);
		int noYlds = Integer.parseInt(words[7]);
		
		System.out.println("\n\n" + t.getName() + "\t =============== Report from node " + nodeId + " ================");
		System.out.println(t.getName() + "\tNo. of REQUESTs: " + noReqs);
		System.out.println(t.getName() + "\tNo. of REPLYs: " + noToks);
		System.out.println(t.getName() + "\tNo. of RELEASEs: " + noRels);
		System.out.println(t.getName() + "\tNo. of FAILs: " + noFails);
		System.out.println(t.getName() + "\tNo. of ENQUIREs: " + noInqs);
		System.out.println(t.getName() + "\tNo. of YIELDs: " + noYlds);
	}


public int random() {
	
		try{
	int randomNumber = ( int )( Math.random()*51 );
	if(randomNumber<10)
	{
		randomNumber=10;
	}
	System.out.println(randomNumber);
	return randomNumber;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
		}
}

