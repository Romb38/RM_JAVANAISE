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
import java.util.Comparator;
import java.util.HashMap;
import java.io.Serializable;


public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord{
	

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int COORD_PORT = 1234;
	public static final String COORD_NAME = "coordinator";
	
	private HashMap<String, sharedObject> sharedObjects;

/**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
		// to be completed
		this.sharedObjects = new HashMap<>();
		Registry registry = LocateRegistry.createRegistry(COORD_PORT);
		registry.bind(COORD_NAME, registry);
	}

  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public int jvnGetObjectId()
  throws java.rmi.RemoteException,jvn.JvnException { 
	  return this.sharedObjects.values().stream().max(Comparator.comparingInt(sharedObject::getUid)).get().getUid() + 1;
  }
  
  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
    // to be completed
	  sharedObject tmp = new sharedObject(this.jvnGetObjectId(),jo);
	  tmp.createOrSetLockState(js, LockStates.NL);
	  this.sharedObjects.put(jon, tmp);
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
	  sharedObject obj = this.sharedObjects.get(jon);
	  
	  if (obj == null) {
		 throw new jvn.JvnException("The " + jon + " JVN object doesn't exist");
	  }
	  
	  while (!obj.isReadableBy(js)) {
		  // [TODO] Wait
	  }
	  
	  return obj.getState();
  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockRead(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
	   sharedObject state = null;
	   for (sharedObject obj : this.sharedObjects.values()) {
		   if (obj.getUid() == joi) {
			   state = obj;
			   break;
		   }
	   }
	   
	   if (state == null) {
		   throw new JvnException("L\'objet identifié par " + joi + "n'existe pas");
	   }
	   
	   while(!state.canBeReadBy(js)) {
		   // [TODO] Wait
	   }
	   
	   state.createOrSetLockState(js, LockStates.R);
	   
    return state.getState();
   }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
	   sharedObject state = null;
	   for (sharedObject obj : this.sharedObjects.values()) {
		   if (obj.getUid() == joi) {
			   state = obj;
			   break;
		   }
	   }
	   
	   if (state == null) {
		   throw new JvnException("L\'objet identifié par " + joi + "n'existe pas");
	   }
	   
	   while(!state.canBeWriteBy(js)) {
		   // [TODO] Wait
	   }
	   
	   state.createOrSetLockState(js, LockStates.W);
	   
    return state.getState();
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
    	for (String uid: this.sharedObjects.keySet()) {
    		sharedObject tmp = this.sharedObjects.get(uid);
    		tmp.removeLockState(js);
    	}
	 // to be completed
    }
}

 
