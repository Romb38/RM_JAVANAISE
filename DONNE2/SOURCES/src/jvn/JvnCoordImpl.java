/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */

package jvn;

import java.rmi.RemoteException;
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
		sharedObject tmp = new sharedObject(jon, (JvnObjectImpl) jo, this);
		tmp.createOrSetLockState(js, LockStates.NL);
		this.sharedObjects.put(this.jvnGetObjectId(), tmp);
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
		for (sharedObject state : this.sharedObjects.values()) {
			if (state.getName().equals(jon)) {
				obj = state;
				break;
			}
		}
		if (obj == null) {
			return null;
		}

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

		state.createOrSetLockState(js, LockStates.R);

		try {
			state.invalidateReadAllOthers(js);
		} catch (RemoteException | JvnException | InterruptedException e) {
			e.printStackTrace();
		}		
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
		
		state.createOrSetLockState(js, LockStates.W);

		
		try {
			state.invalidateWriteAllOthers(js);
		} catch (RemoteException | JvnException | InterruptedException e) {
			e.printStackTrace();
		}
		
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
