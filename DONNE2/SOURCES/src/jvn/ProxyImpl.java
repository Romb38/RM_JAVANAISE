package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyImpl implements InvocationHandler, Serializable{
	
	private static final long serialVersionUID = 1L;
	JvnObject obj;

	public ProxyImpl(JvnObject obj) {
		this.obj = obj;
	}

	public ProxyImpl(Serializable object, String instanceName) throws Exception {
		JvnServerImpl server = JvnServerImpl.jvnGetServer();
		this.obj = server.jvnLookupObject(instanceName);
		
		// Object doesn't exist, therefore it will be created
		if(this.obj == null) {
			this.obj = server.jvnCreateObject(object);
			if(this.obj == null) {
				throw new JvnException("Unable to create object");
			}
			this.obj.jvnUnLock();
			server.jvnRegisterObject(instanceName, this.obj);
		}
	}
	
	public static Object newInstance(Serializable obj, String instanceName) throws Exception {
		return java.lang.reflect.Proxy.newProxyInstance(
				obj.getClass().getClassLoader(),
				obj.getClass().getInterfaces(),
				new ProxyImpl(obj, instanceName));
	}

	@Override
	public Object invoke(Object proxy, Method meth, Object[] arguments) throws Throwable {
		Object result = null;
		try {
			if (meth.isAnnotationPresent(MyAnotation.class)) {
				switch (meth.getAnnotation(MyAnotation.class).name()) {
				case "read":
					this.obj.jvnLockRead();
					break;
				case "write":
					this.obj.jvnLockWrite();
					break;
				default:
				}
			}
			result = meth.invoke(this.obj.jvnGetSharedObject(), arguments);
			this.obj.jvnUnLock();
		} catch (Exception e) {
		}
		return result;
	}

}
