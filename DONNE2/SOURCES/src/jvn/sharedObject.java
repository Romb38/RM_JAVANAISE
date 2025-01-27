package jvn;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class sharedObject {

	// Unique identifier of the object
	private String name;

	// (Value) of the object
	private JvnObjectImpl state;
	
	private JvnCoordImpl coord;

	// Lock detain by the different servers
	private HashMap<JvnRemoteServer, LockStates> lockStateByServer;

	public sharedObject(String name, JvnObjectImpl state, JvnCoordImpl coord) {
		this.name = name;
		this.state = state;
		this.coord = coord;
		this.lockStateByServer = new HashMap<>();
	}

	public String getName() {
		return this.name;
	}

	public JvnObjectImpl getState() {
		return state;
	}

	public void setState(JvnObjectImpl state) {
		this.state = state;
	}

	/**
	 * Set the lock state of the data in a corresponding server
	 * 
	 * @param server    Corresponding server
	 * @param lockState New state for the lock
	 */
	public void createOrSetLockState(JvnRemoteServer server, LockStates lockState) {
		this.lockStateByServer.put(server, lockState);
	}

	/**
	 * Get the lock state for the given server
	 * 
	 * @param server
	 * @return Lock state for the server
	 */
	public LockStates getLockState(JvnRemoteServer server) {
		return this.lockStateByServer.get(server);
	}

	/**
	 * Remove the lock for a server and notify the others if needed
	 * 
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
		for (Entry<JvnRemoteServer, LockStates> obj : this.lockStateByServer.entrySet()) {
			if (obj.getKey() != server) {
				if (obj.getValue() == LockStates.W) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean canBeWriteBy(JvnRemoteServer server) {
		for (Entry<JvnRemoteServer, LockStates> obj : this.lockStateByServer.entrySet()) {
			if (obj.getKey() != server) {
				if (obj.getValue() == LockStates.R) {
					return false;
				}
			}
		}
		return true;
	}

	public synchronized void invalidateReadAllOthers(JvnRemoteServer server) throws RemoteException, JvnException, InterruptedException {
		Integer count = 0;
		System.out.println("Invalidation des verrous en écriture");
		for (Entry<JvnRemoteServer, LockStates> obj : this.lockStateByServer.entrySet().stream()
				.filter(object -> object.getKey() != server && LockStates.W.equals(object.getValue()))
				.collect(Collectors.toList())) {
			for (int i = 0; i<5; i++) {
				count ++;
				System.out.println("Serveur: " + obj.getKey() + " - Test n°"+count.toString());
				try {
					state.setObjValue(obj.getKey().jvnInvalidateWriterForReader(state.jvnGetObjectId()));
					break;
				} catch (Exception e){
					Thread.sleep(1000);
				}
			}
			if (count == 5) {
				System.out.println("Serveur" + obj.getKey() + "non-disponible");
				this.coord.jvnTerminate(obj.getKey());
			}
			count = 0;
		}
	}

	public synchronized void invalidateWriteAllOthers(JvnRemoteServer server) throws RemoteException, JvnException, InterruptedException {
		Integer count = 0;
		ArrayList<JvnRemoteServer> indisponible = new ArrayList<>();
		System.out.println("Invalidation des verrous en lecture et en écriture");
		for (Entry<JvnRemoteServer, LockStates> obj : this.lockStateByServer.entrySet().stream()
				.filter(object -> object.getKey() != server && LockStates.R.equals(object.getValue()))
				.collect(Collectors.toList())) {
			for (int i = 0; i<5; i++) {
				count ++;
				System.out.println("Serveur: " + obj.getKey() + " - Test n°"+count.toString());
				try {
					obj.getKey().jvnInvalidateReader(state.jvnGetObjectId());
					break;
				} catch (Exception e){
					Thread.sleep(1000);
				}
			}
			if (count == 5) {
				System.out.println("Serveur" + obj.getKey() + "non-disponible");
				this.coord.jvnTerminate(obj.getKey());
				indisponible.add(obj.getKey());
			}
			count = 0;
		}
		
		count = 0;
		
		for (Entry<JvnRemoteServer, LockStates> obj : this.lockStateByServer.entrySet().stream()
				.filter(object -> !indisponible.contains(object.getKey()))
				.filter(object -> object.getKey() != server && LockStates.W.equals(object.getValue()))
				.collect(Collectors.toList())) {
			
			for (int i = 0; i<5; i++) {
				count ++;
				System.out.println("Serveur: " + obj.getKey() + " - Test n°"+count.toString());
				try {
					obj.getKey().jvnInvalidateWriter(state.jvnGetObjectId());
					break;
				} catch (Exception e){
					Thread.sleep(1000);
				}
			}
			if (count == 5) {
				System.out.println("Serveur" + obj.getKey() + "non-disponible");
				this.coord.jvnTerminate(obj.getKey());
			}
			count = 0;
		}
	}
}
