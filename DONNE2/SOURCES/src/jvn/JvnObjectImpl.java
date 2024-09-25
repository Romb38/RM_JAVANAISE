package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject{
	
	private static final long serialVersionUID = 1L;

	public JvnObjectImpl(Serializable o, JvnLocalServer js) {
		
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Serializable jvnGetSharedObject() throws JvnException {
		// TODO Auto-generated method stub
		return null;
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
