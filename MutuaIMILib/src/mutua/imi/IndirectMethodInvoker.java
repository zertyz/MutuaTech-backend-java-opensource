package mutua.imi;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

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
	// TODO create reentrant and performance tests & replace this with a HashMap
	private final HashMap<METHOD_ID_TYPE, Method> methodIdToMethodMap;
	private final Class<? extends Annotation>[] annotationClasses;
	
	
	private Method findMethodByAnnotations(Class<?> c, Enum<?> methodEnumerationValue) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Method[] methods = c.getMethods();
		for (Method method : methods) {
			for (Class<? extends Annotation> annotationClass : annotationClasses) {
				if (method.isAnnotationPresent(annotationClass)) {
					Annotation annotationInstance = method.getAnnotation(annotationClass);
					
					// invoke the annotation
					Method am = annotationClass.getMethod("value");
					am.setAccessible(true);		// this allows inner class annotations
					Object annotationValue = am.invoke(annotationInstance);
					
					if (annotationValue instanceof Enum<?>) {
						if (annotationValue == methodEnumerationValue) {
							return method;
						}
					} else if (annotationValue instanceof Enum<?>[]) {
						Enum<?>[] enumValues= (Enum<?>[])annotationValue;
						for (Enum<?> enumValue : enumValues) {
							if (enumValue == methodEnumerationValue) {
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
		methodIdToMethodMap = new HashMap<METHOD_ID_TYPE, Method>();
		METHOD_ID_TYPE[] methodIds = enumeration.getEnumConstants();
		Class<?> c = methodsClassInstance.getClass();
		for (int i=0; i<methodIds.length; i++) try {
			Enum<?> methodEnumerationValue = ((Enum<?>)methodIds[i]);
			Method method                  = findMethodByAnnotations(c, methodEnumerationValue);
			if (method == null) {
				//throw new IndirectMethodNotFoundException(this, methodName);
			} else {
				method.setAccessible(true);
				methodIdToMethodMap.put((METHOD_ID_TYPE)methodIds[i], method);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object invokeMethod(IndirectMethodInvocationInfo<METHOD_ID_TYPE> invocationInfo) throws IndirectMethodNotFoundException {
		Method m = methodIdToMethodMap.get(invocationInfo.getMethodId());
		if (m == null) {
			throw new IndirectMethodNotFoundException(this, invocationInfo);
		} else try {
			return m.invoke(methodsClassInstance, invocationInfo.getParameters());
		} catch (Throwable t) {
			throw new RuntimeException("Error invoking method '"+invocationInfo.toString()+"'", t);
		}
	}
	
	@Override
	public String toString() {
		return methodsClassInstance.getClass().getName();
	}
}
