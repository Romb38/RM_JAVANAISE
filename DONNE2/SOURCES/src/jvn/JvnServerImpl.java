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

public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// A JVN server is managed as a singleton
	private static JvnServerImpl js = null;

	private JvnRemoteCoord coord = null;
	private Registry registery = null;

	private HashMap<Integer, JvnObject> objects = new HashMap<>();

	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	private JvnServerImpl() throws Exception {
		super();
		this.registery = LocateRegistry.getRegistry(JvnCoordImpl.COORD_PORT);
		try {
			this.coord = (JvnRemoteCoord) registery.lookup(JvnCoordImpl.COORD_NAME);
			System.out.println("Connexion au coordinateur réussie : " + coord);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Static method allowing an application to get a reference to a JVN server
	 * instance
	 * 
	 * @throws JvnException
	 **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null) {
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
	 * 
	 * @throws JvnException
	 * @throws
	 **/
	public void jvnTerminate() throws jvn.JvnException {
		try {
			this.coord.jvnTerminate(js);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		this.objects.clear();

		// to be completed
	}

	/**
	 * creation of a JVN object
	 * 
	 * @param o : the JVN object state
	 * @throws JvnException
	 **/
	public JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException {
	    Integer uid;
	    try {
	        uid = this.coord.jvnGetObjectId();
	        System.out.println("Création d'un nouvel objet avec ID: " + uid);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	    JvnObject jvnObject = new JvnObjectImpl(o, js, uid);
	    this.objects.put(uid, jvnObject);
	    return jvnObject;
	}


	/**
	 * Associate a symbolic name with a JVN object
	 * 
	 * @param jon : the JVN object name
	 * @param jo  : the JVN object
	 * @throws JvnException
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo) throws jvn.JvnException {
	    try {
	        System.out.println("Enregistrement de l'objet: " + jon + " avec ID: " + jo.jvnGetObjectId());
	        this.coord.jvnRegisterObject(jon, jo, js);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}

	/**
	 * Provide the reference of a JVN object beeing given its symbolic name
	 * 
	 * @param jon : the JVN object name
	 * @return the JVN object
	 * @throws JvnException
	 **/
	public JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
	    JvnObject obj = null;
	    JvnObjectImpl nObj = null;
	    try {
	        obj = this.coord.jvnLookupObject(jon, js);
	        if (obj != null) {
	            nObj = new JvnObjectImpl(obj.jvnGetSharedObject(), this, obj.jvnGetObjectId());
	            this.objects.put(nObj.jvnGetObjectId(), nObj);
	        }
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	    return nObj;
	}

	/**
	 * Get a Read lock on a JVN object
	 * 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public synchronized Serializable jvnLockRead(int joi) throws JvnException {
	    JvnObjectImpl jvnObject = (JvnObjectImpl) this.objects.get(joi);
	    try {
	        Serializable obj = this.coord.jvnLockRead(joi, js);
	        jvnObject.setObjValue(obj);
	    } catch (RemoteException e) {
	        e.printStackTrace();
	    }
	    return jvnObject.jvnGetSharedObject();
	}


	/**
	 * Get a Write lock on a JVN object
	 * 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnLockWrite(int joi) throws JvnException {
	    System.out.println("Demande de verrou en écriture pour l'objet ID: " + joi);
	    JvnObjectImpl jvnObject = (JvnObjectImpl) this.objects.get(joi);
	    try {
	        Serializable obj = this.coord.jvnLockWrite(joi, js);
	        System.out.println("Verrou en écriture obtenu pour l'objet ID: " + joi);
	        jvnObject.setObjValue(obj);
	    } catch (RemoteException e) {
	        e.printStackTrace();
	    }
	    return jvnObject.jvnGetSharedObject();
	}

	/**
	 * Invalidate the Read lock of the JVN object identified by id called by the
	 * JvnCoord
	 * 
	 * @param joi : the JVN object id
	 * @return void
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		JvnObject obj = this.objects.get(joi);
		if (obj != null) {
			obj.jvnInvalidateReader();
		}
	};

	/**
	 * Invalidate the Write lock of the JVN object identified by id
	 * 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		JvnObject obj = this.objects.get(joi);
		if (obj != null) {
			obj.jvnInvalidateWriter();
			return obj.jvnGetSharedObject();
		}
		return null;
	};

	/**
	 * Reduce the Write lock of the JVN object identified by id
	 * 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		JvnObject obj = this.objects.get(joi);
		if (obj != null) {
			obj.jvnInvalidateWriterForReader();
			return obj.jvnGetSharedObject();
		}
		return null;
	};

}
