package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private Serializable objValue;

	private LockStates lockState;
	
	private JvnLocalServer localServer;

	public JvnObjectImpl(Serializable o, JvnLocalServer js, Integer uid) {
		this.objValue = o;
		this.localServer = js;
		this.lockState = LockStates.NL;
		this.uid = uid;
	}

	@Override
	public void jvnLockRead() throws JvnException {
		// TODO Auto-generated method stub

	}

	@Override
	public void jvnLockWrite() throws JvnException {
		// TODO Auto-generated method stub

	}

	@Override
	public void jvnUnLock() throws JvnException {
		// TODO Auto-generated method stub

	}

	@Override
	public int jvnGetObjectId() throws JvnException {
		return this.uid;
	}

	@Override
	public Serializable jvnGetSharedObject() throws JvnException {
		return objValue;
	}

	@Override
	public void jvnInvalidateReader() throws JvnException {
		// TODO Auto-generated method stub

	}

	@Override
	public Serializable jvnInvalidateWriter() throws JvnException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		// TODO Auto-generated method stub
		return null;
	}

}
