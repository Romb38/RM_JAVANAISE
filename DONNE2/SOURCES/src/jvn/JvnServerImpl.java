/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.io.*;



public class JvnServerImpl 	
              extends UnicastRemoteObject 
							implements JvnLocalServer, JvnRemoteServer{ 
	
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;
	
	private JvnRemoteCoord coord = null;
	private Registry registery = null;
	
	private String name = "";
	private HashMap<Integer, JvnObject> objects = new HashMap<>();

  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		this.registery = LocateRegistry.getRegistry(JvnCoordImpl.COORD_PORT);
		this.coord = (JvnRemoteCoord) registery.lookup(JvnCoordImpl.COORD_NAME);
		this.name = "";
		registery.bind(this.name, this);
	}
	
  /**
    * Static method allowing an application to get a reference to 
    * a JVN server instance
    * @throws JvnException
    **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				return null;
			}
		}
		return js;
	}
	
	/**
	* The JVN service is not used anymore
	* @throws JvnException
	 * @throws  
	**/
	public  void jvnTerminate()
	throws jvn.JvnException {
		try {
			this.coord.jvnTerminate(this);	
		} catch (RemoteException e){
			throw new RuntimeException(e);
		}
		this.objects.clear();
		
    // to be completed 
	} 
	
	/**
	* creation of a JVN object
	* @param o : the JVN object state
	* @throws JvnException
	**/
	public  JvnObject jvnCreateObject(Serializable o)
	throws jvn.JvnException { 
		
		Integer uid;
		
		try {
			uid = this.coord.jvnGetObjectId();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// to be completed 
		JvnObject jvnObject = new JvnObjectImpl(o, this, uid);
		return jvnObject; 
	}
	
	/**
	*  Associate a symbolic name with a JVN object
	* @param jon : the JVN object name
	* @param jo : the JVN object 
	* @throws JvnException
	**/
	public  void jvnRegisterObject(String jon, JvnObject jo)
	throws jvn.JvnException {
		try {
			this.coord.jvnRegisterObject(jon, jo, this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	* Provide the reference of a JVN object beeing given its symbolic name
	* @param jon : the JVN object name
	* @return the JVN object 
	* @throws JvnException
	**/
	public  JvnObject jvnLookupObject(String jon)
	throws jvn.JvnException {
		try {
			return this.coord.jvnLookupObject(jon, this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
	
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockRead(int joi)
	 throws JvnException {
	   JvnObject obj = this.objects.get(joi);
	   if (obj != null) {
		   obj.jvnLockRead();
		   return obj.jvnGetSharedObject();
	   }
	   return null;
	  
	}	
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockWrite(int joi)
		   throws JvnException {
	   JvnObject obj = this.objects.get(joi);
	   if (obj != null) {
		   obj.jvnLockWrite();
		   return obj.jvnGetSharedObject();
	   }
	   return null;
	  
	}	

	
  /**
	* Invalidate the Read lock of the JVN object identified by id 
	* called by the JvnCoord
	* @param joi : the JVN object id
	* @return void
	* @throws java.rmi.RemoteException,JvnException
	**/
  public void jvnInvalidateReader(int joi)
	throws java.rmi.RemoteException,jvn.JvnException {
	   JvnObject obj = this.objects.get(joi);
	   if (obj != null) {
		   obj.jvnInvalidateReader();
	   }
	};
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException,jvn.JvnException { 
	   JvnObject obj = this.objects.get(joi);
	   if (obj != null) {
		   obj.jvnInvalidateWriter();
		   return obj.jvnGetSharedObject();
	   }
	   return null;
	};
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
	 throws java.rmi.RemoteException,jvn.JvnException { 
	   JvnObject obj = this.objects.get(joi);
	   if (obj != null) {
		   obj.jvnInvalidateWriterForReader();
		   return obj.jvnGetSharedObject();
	   }
	   return null;
	 };

}

 
