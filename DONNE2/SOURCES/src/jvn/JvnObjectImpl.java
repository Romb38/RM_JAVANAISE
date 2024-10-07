package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

	private static final long serialVersionUID = 1L;

	private int uid;

	private Serializable objValue;

	private LockStates lockState;

	private transient JvnLocalServer localServer;

	public JvnObjectImpl(Serializable o, JvnLocalServer js, Integer uid) {
		this.objValue = o;
		this.localServer = js;
		this.lockState = LockStates.NL;
		this.uid = uid;
	}

	@Override
	public void jvnLockRead() throws JvnException {
		if (!LockStates.R.equals(this.lockState)) {
			this.localServer.jvnLockRead(this.uid);
			this.lockState = LockStates.R;
		}
	}

	@Override
	public void jvnLockWrite() throws JvnException {
		if (!LockStates.W.equals(this.lockState)) {
			this.localServer.jvnLockWrite(this.uid);
			this.lockState = LockStates.W;
		}
	}

	@Override
	public void jvnUnLock() throws JvnException {
		if (LockStates.W.equals(this.lockState)) {
			this.lockState = LockStates.WC;
			this.notifyAll();
		} else if (LockStates.R.equals(this.lockState) || LockStates.WC.equals(this.lockState)) {
			this.lockState = LockStates.RC;
			this.notifyAll();
		}
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
		while (!LockStates.RC.equals(lockState)) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.lockState = LockStates.NL;
	}

	@Override
	public Serializable jvnInvalidateWriter() throws JvnException {
		while (!LockStates.WC.equals(lockState)) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.lockState = LockStates.NL;
		return this.jvnGetSharedObject();
	}

	@Override
	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		while (!LockStates.WC.equals(lockState)) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.lockState = LockStates.R;
		return this.jvnGetSharedObject();
	}

}
