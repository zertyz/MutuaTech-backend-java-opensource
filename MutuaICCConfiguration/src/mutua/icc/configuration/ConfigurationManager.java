package mutua.icc.configuration;

import static mutua.icc.configuration.ConfigurationInstrumentationMethods.*;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import mutua.icc.configuration.annotations.ConfigurableElement;
import mutua.icc.instrumentation.Instrumentation;
import mutua.serialization.SerializationRepository;

/** <pre>
 * ConfigurationManager.java
 * =========================
 * (created by luiz, Jan 29, 2015)
 *
 * Responsible for loading & logging the configurations read
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class ConfigurationManager {
	
	
	private final Class<?>[] configurableClasses;
	
	
	public ConfigurationManager(Class<?>... configurableClasses) {
		this.configurableClasses = configurableClasses;
	}

	
	protected String serializeStaticFields(Class<?> configurableClass) throws IllegalArgumentException, IllegalAccessException {
		StringBuffer buffer = new StringBuffer();
		Field[] fields = configurableClass.getDeclaredFields();
		for (Field f : fields) {
			
			// proceed only for static fields
			if (!Modifier.isStatic(f.getModifiers())) {
				continue;
			}

			ConfigurableElement configurableAnnotation = f.getAnnotation(ConfigurableElement.class);
			if (configurableAnnotation == null) {
				continue;
			}
			String[] comments = configurableAnnotation.value();
			if (comments.length == 0) {
				String commentReferenceField   = configurableAnnotation.sameAs();
				String commentReferenceMethod  = configurableAnnotation.sameAsMethod();
				if (!commentReferenceField.equals("")) {
					String className = commentReferenceField.replaceAll("(.*)\\.(.*)", "$1");
					String fieldName = commentReferenceField.replaceAll("(.*)\\.(.*)", "$2");
					try {
						// resolve class and field names
						Class<?> referencedClass = Class.forName(className);
						Field referencedField = referencedClass.getField(fieldName);
						ConfigurableElement referencedConfigurableAnnotation = referencedField.getAnnotation(ConfigurableElement.class);
						comments = referencedConfigurableAnnotation.value();
					} catch (Throwable t) {t.printStackTrace();
						comments = new String[] {"same as field '"+commentReferenceField+"' (reference not found) -- "+t.getMessage()};
					}
				} else if (!commentReferenceMethod.equals("")) {
					String className  = commentReferenceMethod.replaceAll("(.*)\\.(.*)", "$1");
					String methodName = commentReferenceMethod.replaceAll("(.*)\\.(.*)", "$2");
					try {
						// resolve class and field names
						Class<?> referencedClass = Class.forName(className);
						for (Method referencedMethod : referencedClass.getDeclaredMethods()) {
							if (referencedMethod.getName().equals(methodName)) {
								ConfigurableElement referencedConfigurableAnnotation = referencedMethod.getAnnotation(ConfigurableElement.class);
								if (referencedConfigurableAnnotation != null) {
									comments = referencedConfigurableAnnotation.value();
									break;
								}
							}
						}
					} catch (Throwable t) {
						comments = new String[] {"same as method '"+commentReferenceMethod+"' (reference not found)"};
					}
				}
			}
			for (String comment : comments) {
				buffer.append("# ").append(comment).append('\n');
			}
			String fName   = f.getName();
			Object fValue  = f.get(null);
			Class<?> fType = f.getType();
			if ((fType == int.class) || (fType == long.class) || (fType == double.class) || (fType == float.class) || (fType == short.class) || (fType == byte.class) || 
			    (fType == boolean.class)) {
				buffer.append(fName).append("=").append(fValue).append("\n");
			} else if (fType == String.class) {
					SerializationRepository.serialize(buffer.append(fName).append("="), (String)fValue).append("\n");
			} else if (fType == String[].class) {
				String[] ss = fValue != null ? (String[])fValue : new String[0];
				if (ss.length == 0) {
					buffer.append('#').append(fName).append("+=...\n");
				} else {
					for (String s : ss) {
						SerializationRepository.serialize(buffer.append(fName).append("+="), s).append("\n");
					}
				}
			} else if (fType.isEnum()) {
				// add to the comment line
				buffer.deleteCharAt(buffer.length()-1);
				buffer.append(" (possible values: ");
				boolean isFirst = true;
				for (Object enumConstant: fType.getEnumConstants()) {
					if (isFirst) {
						isFirst = false;
					} else {
						buffer.append(", ");
					}
					buffer.append(enumConstant.toString());
				}
				if (fValue == null) {
					Instrumentation.reportDebug("WARNING while serializing '"+configurableClass.getCanonicalName()+"': field '"+fName+"' is null");
				}
				buffer.append(")\n").append(fName).append('=').append(fValue).append('\n');
			} else if (fType.isArray() && fType.getComponentType().isEnum()) {
				// add to the comment line
				buffer.deleteCharAt(buffer.length()-1);
				buffer.append(" (possible values: ");
				boolean isFirst = true;
				Class<?> elementsType = fType.getComponentType();
				for (Object enumConstant: elementsType.getEnumConstants()) {
					if (isFirst) {
						isFirst = false;
					} else {
						buffer.append(", ");
					}
					buffer.append(enumConstant.toString());
				}
				buffer.append(")\n");
				// declaration
				Enum<?>[] ee = fValue != null ? (Enum[])fValue : new Enum[0];
				if (ee.length == 0) {
					buffer.append('#').append(fName).append("+=...\n");
				} else {
					for (Enum<?> e : ee) {
						buffer.append(fName).append("+=").append(e.name()).append("\n");
					}
				}
			} else {
				buffer.append("// don't know how to serialize field '").append(fName).
				       append("', an instance of '").append(fType.getCanonicalName()).append("'");
				for (Class<?> clazz : fType.getClasses()) {
					buffer.append(", subclass of '").append(clazz.getCanonicalName()).append("'");
				}
				buffer.append('\n');
			}
		}
		return buffer.toString();
	}
	
	public String serializeConfigurableClasses() throws IllegalArgumentException, IllegalAccessException {
		StringBuffer buffer = new StringBuffer();
		for (Class<?> configurableClass : configurableClasses) {
			buffer.append("\n\n// ").append(configurableClass.getCanonicalName()).
			       append("\n/////////////////////////////////////////////////\n\n").
			       append(serializeStaticFields(configurableClass));
		}
		return buffer.toString();
	}
	
	private String requestScalarValue(Field f, ConfigurationParser cp) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		String fieldName = f.getName();
		String[] values = cp.getValues(fieldName);
		// detect errors
		if (values == null) {
			logFieldIsNotPresentOnConfiguration(f.getType().getCanonicalName(), f);
		} else if ((values.length == 0) || (values.length > 1)) {
			logScalarFieldDeclaredAsVector(f, values);
		} else {
			// if everything is ok
			return values[0];
		}
		throw new java.lang.NoSuchFieldException();
	}

	private String[] requestVectorValue(Field f, ConfigurationParser cp) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		String fieldName = f.getName();
		String[] values = cp.getValues(fieldName);
		// detect errors
		if (values == null) {
			logFieldIsNotPresentOnConfiguration(f.getType().getCanonicalName(), f);
			throw new java.lang.NoSuchFieldException();
		} else {
			return values;
		}
	}

	protected void desserializeStaticFields(Class<?> configurableClass, ConfigurationParser cp) throws IllegalArgumentException, IllegalAccessException {
		
		// load the pertinent fields
		Field[] fields = configurableClass.getDeclaredFields();
		for (Field f : fields) try {
			
			// proceed only for static fields
			if (!Modifier.isStatic(f.getModifiers())) {
				continue;
			}
			
			String fName   = f.getName();
			Class<?> fType = f.getType();
			if (fType == String.class) try {
				String s = requestScalarValue(f, cp);
				s = SerializationRepository.deserialize(s);
				f.set(null, s);
				reportConfigureStringField(fName, s);
			} catch (NoSuchFieldException e) {
			} else if (fType == long.class) try {
				String s = requestScalarValue(f, cp);
				long l = Long.parseLong(s);
				f.set(null, l);
				reportConfigureNumberField(fName, l);
			} catch (NoSuchFieldException e) {
			} else if (fType == int.class) try {
				String s = requestScalarValue(f, cp);
				int i = Integer.parseInt(s);
				f.set(null, i);
				reportConfigureNumberField(fName, i);
			} catch (NoSuchFieldException e) {
			} else if (fType == String[].class) try {
				String[] ss = requestVectorValue(f, cp);
				for (int i=0; i<ss.length; i++) {
					String s = SerializationRepository.deserialize(ss[i]);
					ss[i] = s;
				}
				f.set(null, ss);
				reportConfigureStringArrayField(fName, ss);
			} catch (NoSuchFieldException e) {
			} else if (fType == boolean.class) try {
				String s = requestScalarValue(f, cp);
				boolean b = Boolean.parseBoolean(s);
				f.set(null, b);
				reportConfigureBooleanField(fName, b);
			} catch (NoSuchFieldException e) {
			} else if (fType.isEnum()) DONE: try {
				String e = requestScalarValue(f, cp);
				for (Object enumConstant: fType.getEnumConstants()) {
					if (enumConstant.toString().equals(e)) {
						f.set(null, enumConstant);
						reportConfigureEnumerationField(fName, enumConstant);
						break DONE;
					}
				}
				reportDeserializationError("Value '"+e+"' is not recognized as a valid value for Enum '"+fType.getCanonicalName()+"'");
			} catch (NoSuchFieldException e) {
			} else {
				reportDeserializationError("Don't know how to desserialize type '"+fType.getCanonicalName()+"' for field '"+f.getName()+"'");
			}
		} catch (NumberFormatException e) {
			Instrumentation.reportThrowable(e, "Error while attempt to configure field '"+f.getName()+"'");
		}
	}
	
	public void deserializeConfigurableClasses(String serializedFields) throws IllegalArgumentException, IllegalAccessException {

		ConfigurationParser cp = new ConfigurationParser(serializedFields);
		
		// log configuration line format errors
		for (Object[] errorLine : cp.getErrorLines()) {
			int    errorLineNumber  = (Integer) errorLine[0];
			String errorLineContent = (String)  errorLine[1];
			reportDeserializationError("Configuration format error at line #"+errorLineNumber+": "+errorLineContent);
		}
		
		try {
			for (Class<?> configurableClass : configurableClasses) {
				reportConfigureClass(configurableClass);
				desserializeStaticFields(configurableClass, cp);
			}
		} catch (Throwable t) {
			Instrumentation.reportThrowable(t, "Exception while attempting to load values into one of the provided configurable classes");
			t.printStackTrace();
			String originalSerializedFields = serializeConfigurableClasses();
			Instrumentation.reportDebug("Dumping the configuration model with default values for the provided classes: " + originalSerializedFields);
		}
			
	}

	public void loadFromFile(String filePath) throws IOException, IllegalArgumentException, IllegalAccessException {
		reportLoadConfigurationFile(filePath);
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		StringBuffer fileContents = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) {
			fileContents.append(line).append('\n');
		}
		br.close();
		deserializeConfigurableClasses(fileContents.toString());
	}

	public void saveToFile(String filePath) throws IOException, IllegalArgumentException, IllegalAccessException {
		String serializedFields = serializeConfigurableClasses();
		FileOutputStream fout = new FileOutputStream(filePath);
		fout.write(serializedFields.getBytes());
		fout.close();
	}
}