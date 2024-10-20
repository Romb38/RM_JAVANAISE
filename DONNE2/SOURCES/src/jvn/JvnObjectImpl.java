package jvn;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;

public class JvnObjectImpl implements JvnObject {

	private static final long serialVersionUID = 1L;

	private int uid;

	private Serializable objValue;

	private LockStates lockState;

	private transient JvnLocalServer localServer;

	private ReentrantLock lock;

	public JvnObjectImpl(Serializable o, JvnLocalServer js, Integer uid) {
		this.objValue = o;
		this.localServer = js;
		this.lockState = LockStates.NL;
		this.uid = uid;
		this.lock = new ReentrantLock();
	}

	public void setObjValue(Serializable obj) {
		this.objValue = obj;
	}

	@Override
	public void jvnLockRead() throws JvnException {
		if (LockStates.NL.equals(this.lockState)) {
			this.lock.lock();
			this.objValue = this.localServer.jvnLockRead(this.uid);
		}

		switch (this.lockState) {
		case NL:
			this.lockState = LockStates.R;
			break;
		case W:
			this.lockState = LockStates.R;
			break;
		case RC:
			this.lockState = LockStates.R;
		default:
			break;
		}
	}

	@Override
	public void jvnLockWrite() throws JvnException {
		if (LockStates.NL.equals(this.lockState)) {
			this.lock.lock();
		}

		switch (this.lockState) {
		case WC:
			this.lockState = LockStates.W;
			break;
		case W:
			break;
		default:
			this.objValue = this.localServer.jvnLockWrite(this.uid);
			this.lockState = LockStates.W;
		}
	}

	@Override
	public void jvnUnLock() throws JvnException {
		synchronized (this) {
			switch (this.lockState) {
			case R:
				this.lockState = LockStates.RC;
				break;
			case W:
				this.lockState = LockStates.WC;
				break;
			default:
				break;
			}
			if (this.lock.isLocked()) {
				this.lock.unlock();
			}
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
		if (LockStates.R.equals(this.lockState)) {
			this.lock.lock();
			this.lock.unlock();

		}
		this.lockState = LockStates.NL;
		setObjValue(null);
		System.out.println("Object (id:"+this.uid+"): Verrou en lecture libéré");
	}

	@Override
	public Serializable jvnInvalidateWriter() throws JvnException {
		if (LockStates.W.equals(this.lockState)) {
			this.lock.lock();
			this.lock.unlock();
		}
		this.lockState = LockStates.NL;
		Serializable val = this.jvnGetSharedObject();
		setObjValue(null);
		System.out.println("Object (id:"+this.uid+"): Verrou en écriture libéré");
		return val;
	}

	@Override
	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		switch (this.lockState) {
		case W:
			this.lock.lock();
			this.lock.unlock();
			this.lockState = LockStates.RC;
			break;
		case RWC:
		case WC:
			this.lockState = LockStates.RC;
			break;
		default:

		}
		System.out.println("Object (id:"+this.uid+"): Verrou en écriture transformé en verrou de lecture");
		return this.objValue;
	}

}
