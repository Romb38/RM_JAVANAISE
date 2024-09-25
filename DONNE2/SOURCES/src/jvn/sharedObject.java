package jvn;

import java.util.HashMap;
import java.util.Map.Entry;

public class sharedObject {
	
	// Unique identifier of the object
	private int uid;
	
	// (Value) of the object
	private JvnObject state;
	
	// Lock detain by the different servers
	private HashMap<JvnRemoteServer, LockStates> lockStateByServer;
	
	
	public sharedObject(int uid, JvnObject state) {
		this.uid = uid;
		this.state = state;
		this.lockStateByServer = new HashMap<>();
	}
	
	public int getUid() {
		return uid;
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
	
	public boolean isReadableBy(JvnRemoteServer server) {
		LockStates state = this.lockStateByServer.get(server);
		
		if (state == LockStates.R || state == LockStates.RC || state == LockStates.RWC) {
			return true;
		}
		
		return false;
	}
	
	public boolean canBeReadBy(JvnRemoteServer server) {
		for (Entry<JvnRemoteServer, LockStates> obj: this.lockStateByServer.entrySet()) {
			if (obj.getKey() != server) {
				if (obj.getValue() == LockStates.W) {
					return false;
				}
			}
		}
		return true;
	}

	
	public boolean canBeWriteBy(JvnRemoteServer server) {
		for (Entry<JvnRemoteServer, LockStates> obj: this.lockStateByServer.entrySet()) {
			if (obj.getKey() != server) {
				if (obj.getValue() == LockStates.R) {
					return false;
				}
			}
		}
		return true;
	}
	

}
