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
			this.objValue=this.localServer.jvnLockRead(this.uid);
		}
//		if (!LockStates.R.equals(this.lockState) && !LockStates.RC.equals(this.lockState)) {
//			this.lockState = LockStates.R;
//		} else if (LockStates.RC.equals(this.lockState)) {
//			this.lockState = LockStates.R;
//		}
		
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
//		if (!LockStates.W.equals(this.lockState) && !LockStates.WC.equals(this.lockState)) {
//			this.objValue = this.localServer.jvnLockWrite(this.uid);
//			this.lockState = LockStates.W;
//		} else if (LockStates.WC.equals(this.lockState)) {
//			this.lockState = LockStates.W;
//		}
		
		switch (this.lockState) {
		case WC:
			this.lockState = LockStates.W;
			break;
		case W: 
			break;
		default:
			this.objValue=this.localServer.jvnLockWrite(this.uid);
			this.lockState=LockStates.W;
		}	
	}

	@Override
	public void jvnUnLock() throws JvnException {
		synchronized (this) {
			switch(this.lockState) {
			case R:
				this.lockState=LockStates.RC;
				break;
			case W:
				this.lockState=LockStates.WC;
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
	}

	@Override
	public Serializable jvnInvalidateWriter() throws JvnException {
		if (LockStates.W.equals(this.lockState)) {
			this.lock.lock();
			this.lock.unlock();
		}
		this.lockState = LockStates.NL;
		return this.jvnGetSharedObject();
	}

	@Override
	public Serializable jvnInvalidateWriterForReader() throws JvnException {		
		switch (this.lockState){
        case W:
			this.lock.lock();
			this.lock.unlock();
            this.lockState = LockStates.RC;
            break;
        case RWC:
        case WC:
            this.lockState=LockStates.RC;
            break;
        default:
            
    }
    return this.objValue;
	}

}
