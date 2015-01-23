package mutua.imi;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

/** <pre>
 * IndirectMethodInvoker.java
 * ==========================
 * (created by luiz, Jan 23, 2015)
 *
 * Invokes methods represented by 'IndirectMethodInvocationInfo'.
 * 
 * This class is needed only and only if METHOD_ID_TYPE != Method.class
 *
 * @see IndirectMethodInvocationInfo
 * @version $Id$
 * @author luiz
 */

public class IndirectMethodInvoker<METHOD_ID_TYPE> {
	
	private final Object methodsClassInstance;
	private final Hashtable<METHOD_ID_TYPE, Method> methodIdToMethodMap;
	private final Class<? extends Annotation>[] annotationClasses;
	
	
	private Method findMethodByNameOrAnnotations(Class<?> c, String methodName) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Method[] methods = c.getMethods();
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				return method;
			} else for (Class<? extends Annotation> annotationClass : annotationClasses) {
				if (method.isAnnotationPresent(annotationClass)) {
					Annotation annotationInstance = method.getAnnotation(annotationClass);
					Object annotationValue = annotationClass.getMethod("value").invoke(annotationInstance);
					if (annotationValue instanceof String) {
						String methodId = (String)annotationValue;
						if (methodId.equals(methodName)) {
							return method;
						}
					} else if (annotationValue instanceof String[]) {
						String[] methodIds = (String[])annotationValue;
						for (String methodId: methodIds) {
							if (methodId.equals(methodName)) {
								return method;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	
//	/** methodIdsAndMethods := { {methodId, Method}, ... } */
//	public IndirectMethodInvoker(Object methodsClassInstance, Object[][] methodIdsAndMethods) {
//		this.methodsClassInstance = methodsClassInstance;
//		methodIdToMethodMap = new Hashtable<METHOD_ID_TYPE, Method>();
//		for (Object[] methodMapping : methodIdsAndMethods) {
//			METHOD_ID_TYPE methodId = (METHOD_ID_TYPE)methodMapping[0];
//			Method         method   = (Method)methodMapping[1];
//			methodIdToMethodMap.put(methodId, method);
//		}
//	}
	
	public IndirectMethodInvoker(Object methodsClassInstance, Class<METHOD_ID_TYPE> enumeration, Class<? extends Annotation>... annotationClasses) throws IndirectMethodNotFoundException {
		this.methodsClassInstance = methodsClassInstance;
		this.annotationClasses    = annotationClasses;
		methodIdToMethodMap = new Hashtable<METHOD_ID_TYPE, Method>();
		METHOD_ID_TYPE[] methodIds = enumeration.getEnumConstants();
		Class<?> c = methodsClassInstance.getClass();
		for (int i=0; i<methodIds.length; i++) try {
			String methodName = ((Enum<?>)methodIds[i]).name();
			Method method     = findMethodByNameOrAnnotations(c, methodName);
			if (method == null) {
				//throw new IndirectMethodNotFoundException(this, methodName);
			} else {
				methodIdToMethodMap.put((METHOD_ID_TYPE)methodIds[i], method);
			}
		} catch (Exception e) {
			throw new IndirectMethodNotFoundException(e);
		}
	}

	public Object invokeMethod(IndirectMethodInvocationInfo<METHOD_ID_TYPE> invocationInfo) throws IndirectMethodNotFoundException {
		Method m = methodIdToMethodMap.get(invocationInfo.getMethodId());
		if (m == null) {
			throw new IndirectMethodNotFoundException(this, invocationInfo);
		} else try {
			return m.invoke(methodsClassInstance, invocationInfo.getParameters());
		} catch (Exception e) {
			throw new IndirectMethodNotFoundException(this, invocationInfo, e);
		}
	}
}
