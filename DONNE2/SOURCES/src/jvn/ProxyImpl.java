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
	
	public static Object newInstance(JvnObject obj) {
		return java.lang.reflect.Proxy.newProxyInstance(
				obj.getClass().getClassLoader(),
				obj.getClass().getInterfaces(),
				new ProxyImpl(obj));
	}

	@Override
	public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
		Object result = null;
		try {
			if (arg1.isAnnotationPresent(MyAnotation.class)) {
				switch (arg1.getAnnotation(MyAnotation.class).name()) {
				case "read":
					obj.jvnLockRead();
					break;
				case "write":
					obj.jvnLockWrite();
					break;
				default:
				}
			}
			result = arg1.invoke(obj.jvnGetSharedObject(), arg2);
			obj.jvnUnLock();
		} catch (Exception e) {
		}
		return result;
	}

}
