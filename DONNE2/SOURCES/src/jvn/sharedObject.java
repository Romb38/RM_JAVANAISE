package jvn;

import java.util.HashMap;

public class sharedObject {
	
	// Unique identifier of the object
	private String uid;
	
	// (Value) of the object
	private JvnObject state;
	
	// Lock detain by the different servers
	private HashMap<JvnRemoteServer, LockStates> lockStateByServer;
	
	
	public sharedObject(String uid, JvnObject state) {
		this.uid = uid;
		this.state = state;
		this.lockStateByServer = new HashMap<>();
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public JvnObject getState() {
		return state;
	}

	public void setState(JvnObject state) {
		this.state = state;
	}
	
	/**
	 * Set the lock state of the data in a corresponding server
	 * @param server Corresponding server
	 * @param lockState New state for the lock
	 */
	public void createOrSetLockState(JvnRemoteServer server, LockStates lockState) {
		this.lockStateByServer.put(server,lockState);
	}
	
	/**
	 * Get the lock state for the given server
	 * @param server
	 * @return Lock state for the server
	 */
	public LockStates getLockState(JvnRemoteServer server) {
		return this.lockStateByServer.get(server);
	}
	
	/**
	 * Remove the lock for a server and notify the others if needed
	 * @param server
	 */
	public void removeLockState(JvnRemoteServer server) {
		LockStates state = this.lockStateByServer.get(server);
		if (state != null) {
			// [TODO] Notify all the others servers, depending of the type of lock
		}
		this.lockStateByServer.remove(server);
	}
	
	

}
