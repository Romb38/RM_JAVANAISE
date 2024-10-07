/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */

package jvn;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.io.Serializable;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int COORD_PORT = 1234;
	public static final String COORD_NAME = "coordinator";

	private HashMap<Integer, sharedObject> sharedObjects;

	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	public JvnCoordImpl() throws Exception {
		// to be completed
		this.sharedObjects = new HashMap<>();
		Registry registry = LocateRegistry.createRegistry(COORD_PORT);
		registry.bind(COORD_NAME, this);
	}

	/**
	 * Allocate a NEW JVN object id (usually allocated to a newly created JVN
	 * object)
	 * 
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public int jvnGetObjectId() throws java.rmi.RemoteException, jvn.JvnException {
		// Si la HashMap est vide, retourner 1 directement
		// ajout de synchro
		if (this.sharedObjects.isEmpty()) {
			return 1;
		}
		// Trouver la clé maximum et ajouter 1
		int maxKey = Collections.max(this.sharedObjects.keySet());
		return maxKey + 1;
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * 
	 * @param jon : the JVN object name
	 * @param jo  : the JVN object
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
			throws java.rmi.RemoteException, jvn.JvnException {
		// to be completed
		sharedObject tmp = new sharedObject(jon, (JvnObjectImpl) jo);
		tmp.createOrSetLockState(js, LockStates.NL);
		this.sharedObjects.put(this.jvnGetObjectId(), tmp);
	}
	
	private void logRegisteredObjects() {
	    System.out.println("Objet(s) enregistré(s) dans le coordinateur :");
	    for (Entry<Integer, sharedObject> entry : this.sharedObjects.entrySet()) {
	        System.out.println("ID de l'objet" + entry.getKey() + ", Nom : " + entry.getValue().getName());
	    }
	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server
	 * 
	 * @param jon : the JVN object name
	 * @param js  : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public synchronized JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
			throws java.rmi.RemoteException, jvn.JvnException {

		sharedObject obj = null;
		System.out.println(jon);
		logRegisteredObjects();
		for (sharedObject state : this.sharedObjects.values()) {
			if (state.getName().equals(jon)) {
				obj = state;
				break;
			}
		}
		if (obj == null) {
			return null;
		}

//		synchronized (this) {
//			obj.createOrSetLockState(js, LockStates.R);
//			while (!obj.isReadableBy(js)) {
//				try {
//					this.wait();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
		return obj.getState();
	}

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server
	 * 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js)
			throws java.rmi.RemoteException, JvnException {
		sharedObject state = this.sharedObjects.get(joi);

		if (state == null) {
			throw new JvnException("L\'objet identifié par " + joi + "n'existe pas");
		}

//		synchronized (this) {
//			while (!state.canBeReadBy(js)) {
//				try {
//					this.wait();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
	    System.out.println("Instance " + js + " demande un verrou en lecture sur l'objet ID: " + joi);

		state.invalidateReadAllOthers(js);
		state.createOrSetLockState(js, LockStates.R);
		
	    System.out.println("Verrou en lecture accordé à l'instance " + js);

		return state.getState().jvnGetSharedObject();
	}

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server
	 * 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js)
			throws java.rmi.RemoteException, JvnException {
		sharedObject state = this.sharedObjects.get(joi);

		if (state == null) {
			throw new JvnException("L\'objet identifié par " + joi + "n'existe pas");
		}

//		synchronized (this) {
//			while (!state.canBeWriteBy(js)) {
//				try {
//					this.wait();
//
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
		
	    System.out.println("Instance " + js + " demande un verrou en écriture sur l'objet ID: " + joi);
		
		state.invalidateWriteAllOthers(js);
		state.createOrSetLockState(js, LockStates.W);
		
	    System.out.println("Verrou en écriture accordé à l'instance " + js);

		return state.getState().jvnGetSharedObject();
	}

	/**
	 * A JVN server terminates
	 * 
	 * @param js : the remote reference of the server
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		for (Integer uid : this.sharedObjects.keySet()) {
			sharedObject tmp = this.sharedObjects.get(uid);
			tmp.removeLockState(js);
		}
	}
}
