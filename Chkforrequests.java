package firstTry;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Chkforrequests implements Runnable{
	
	int myNodeId, myPortNo;
	Requestque q;
	Thread t;
	Quorumclients quorum;
	Fileinput file;
	Clock clock;
	
	//This constructor is for the individual nodes
	public Chkforrequests(int myNodeId, int myPortId, Requestque msgQueue, Quorumclients quorum, Fileinput file, Clock clock){
		this.myNodeId = myNodeId;
		this.myPortNo = myPortId;
		q = msgQueue;
		this.quorum = quorum;
		this.clock = clock;
		this.file = file;
		
		//Thread creation
		t = new Thread(this, "ReadThrd");
		t.start();
	}//Constructor ends here
	
	public void run(){
		System.out.println("----------READ MSG QUEUE THREAD-------------");
		System.out.println("*********************************************");
		System.out.println("### *IF* Queue is empty, so I will sleep for 200ms");
		while(true){
			if(q.isEmpty()){
				
				//Mark node as unlocked
				//quorum.locked.compareAndSet(true, false);
				
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			else{
				while(true){
					if(quorum.isTransferLock()){
						Socket client = null;
					    DataOutputStream outToClient;
					    
						//If queue is not empty, process the request at the head of the queue
						String headOfQueue = q.take();
						System.out.println("\n\n"+t.getName()+"\tReceived head of the queue: " + headOfQueue);
						
						String words[] = headOfQueue.split(" ");
						int reqNode = Integer.parseInt(words[1]);
						int reqPriority = Integer.parseInt(words[2]);
						String destIpAdd = file.getIpAddressList().get(reqNode);
						int destPort = file.getPortList().get(reqNode);
						
						//Setting client according to the quorum hashMap
					    if(quorum.getQuorumSockets().containsKey(reqNode))
							client = quorum.getQuorumSockets().get(reqNode);
						else{
							try {
								client = new Socket(destIpAdd, destPort);
								quorum.getQuorumSockets().put(reqNode, client);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					    
					    
					    
					    try {
							outToClient = new DataOutputStream(client.getOutputStream());
							String msgToSend = "TOK " + myNodeId + " " + clock.getClockVal();
							outToClient.writeBytes(msgToSend+"\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					    
					    System.out.println(t.getName()+ "\t*****()()()()s*********GRANTED TOKEN TO: " + reqNode + "******************");
						
						//Setting details of request for which the TOKEN was sent
						quorum.setReqWithToken(headOfQueue);
						quorum.setPermissionGivenTo(reqNode);
						quorum.setPermissionGivenToPriority(reqPriority);
						
						//New TOKEN granted and so canSendInq and canSendFail booleans should be allowed
						quorum.setCanSendInq(true);
						//quorum.setCanSendFail(true);
						
						//Locking node for current request
						quorum.setTransferLock(false);
					}
					else{
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			}
		}
	}
}
			