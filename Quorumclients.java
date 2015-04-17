package firstTry;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Quorumclients {
	private List<Integer> quorumList;
	private HashMap<Integer, Socket> quorumSockets;
	private boolean beginExecution; //Sets to enter main method
	
	private boolean canSendFail;
	private boolean transferLock;
	AtomicBoolean locked;
	AtomicBoolean sending;
	private int permissiongivento;
	private int permissionGivenToPriority;
	private List<Integer> permissionsPending;
	private boolean recvdFail;
	private String reqWithPermission;
	private boolean canSendInq;
	private boolean cstExec;
	private boolean canSendReq;
	private List<Integer> recvdInqFrom;
	
	public Quorumclients(int myNodeId){
		quorumList = new ArrayList<Integer>();
		quorumSockets = new HashMap<Integer, Socket>();
		permissionsPending = new ArrayList<Integer>();
		recvdInqFrom = new ArrayList<Integer>();
		locked =  new AtomicBoolean(false);
		sending = new AtomicBoolean(false);
		
		setBeginExecution(false); //Cannot begin execution until msg from initiator
		setPermissionGivenTo(myNodeId); //To show which nodeId has the token
		setPermissionGivenToPriority(999); //To show the priority of the request for which token was granted
		setRecvdFail(false); //To show if a FAIL is received
		setReqWithToken(null); //The request to which token was granted 
		setCanSendInq(true); //To show if an INQ msg can be sent
		setCanSendFail(true); //To show if an FAIL msg can be sent
		//setLocked(false); //To show if this node is locked, i.e. has given a TOKEN to a node	
		setTransferLock(false);
		setCstExec(false);
		setCanSendReq(true);
	}
	
	public void addQuorum(int newQuorum){
		getQuorumList().add(newQuorum);
		getPermissionsPending().add(newQuorum);
	}
	
	public void printQuorumList(){
		Iterator<Integer> iterator = quorumList.iterator();
		while(iterator.hasNext())
			System.out.print(iterator.next()+" ");
	}
	
	public void printPermissionsPendingList(){
		Iterator<Integer> iterator = permissionsPending.iterator();
		while(iterator.hasNext())
			System.out.print(iterator.next()+" ");
		System.out.println("\n");
	}
	
	public void addQuorumSockets(Integer ipAddress, Socket socket){
		getQuorumSockets().put(ipAddress, socket);
	}
	
	public void removePermissionPending(int nodeId){
		if(getPermissionsPending().contains(nodeId)){
			int index = getPermissionsPending().indexOf(nodeId);
			getPermissionsPending().remove(index);
		}
	}
	
	public boolean canEnterCritSec(){
		return getPermissionsPending().isEmpty();
	}
	
	//Generated getters and setters
	public List<Integer> getQuorumList() {
		return quorumList;
	}

	public void setQuorumList(List<Integer> quorumList) {
		this.quorumList = quorumList;
	}

	public HashMap<Integer, Socket> getQuorumSockets() {
		return quorumSockets;
	}

	public void setQuorumSockets(HashMap<Integer, Socket> quorumSockets) {
		this.quorumSockets = quorumSockets;
	}

	public boolean isBeginExecution() {
		return beginExecution;
	}

	public void setBeginExecution(boolean beginExecution) {
		this.beginExecution = beginExecution;
	}

	public int getPermissionGivenTo() {
		return permissiongivento;
	}

	public void setPermissionGivenTo(int PermissionGivenTo) {
		this.permissiongivento = PermissionGivenTo;
	}

	public List<Integer> getPermissionsPending() {
		return permissionsPending;
	}

	public void setPermissionsPending(List<Integer> permissionsPending) {
		this.permissionsPending = permissionsPending;
	}

	public int getPermissionGivenToPriority() {
		return permissionGivenToPriority;
	}

	public void setPermissionGivenToPriority(int permissionGivenToPriority) {
		this.permissionGivenToPriority = permissionGivenToPriority;
	}

	public boolean isCanSendInq() {
		return canSendInq;
	}

	public void setCanSendInq(boolean canSendInq) {
		this.canSendInq = canSendInq;
	}

	public boolean isCanSendFail() {
		return canSendFail;
	}

	public void setCanSendFail(boolean canSendFail) {
		this.canSendFail = canSendFail;
	}

	public boolean isRecvdFail() {
		return recvdFail;
	}

	public void setRecvdFail(boolean recvdFail) {
		this.recvdFail = recvdFail;
	}

	public String getReqWithToken() {
		return reqWithPermission;
	}

	public void setReqWithToken(String reqWithPermission) {
		this.reqWithPermission = reqWithPermission;
	}

	public boolean isTransferLock() {
		return transferLock;
	}

	public void setTransferLock(boolean transferLock) {
		this.transferLock = transferLock;
	}

	public boolean isCstExec() {
		return cstExec;
	}

	public void setCstExec(boolean cstExec) {
		this.cstExec = cstExec;
	}

	public boolean isCanSendReq() {
		return canSendReq;
	}

	public void setCanSendReq(boolean canSendReq) {
		this.canSendReq = canSendReq;
	}

	public List<Integer> getRecvdInqFrom() {
		return recvdInqFrom;
	}

	public void setRecvdInqFrom(List<Integer> recvdInqFrom) {
		this.recvdInqFrom = recvdInqFrom;
	}
	
	
}