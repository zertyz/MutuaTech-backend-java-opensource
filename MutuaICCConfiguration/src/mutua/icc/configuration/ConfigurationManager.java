package mutua.icc.configuration;

import static mutua.icc.instrumentation.MutuaICCConfigurationInstrumentationEvents.*;
import static mutua.icc.instrumentation.MutuaICCConfigurationInstrumentationProperties.*;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;

import mutua.icc.configuration.annotations.ConfigurableElement;
import mutua.icc.instrumentation.Instrumentation;
import mutua.icc.instrumentation.MutuaICCConfigurationInstrumentationEvents;

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
	
	
	private final Instrumentation<?, ?> log;
	private final Class<?>[] configurableClasses;
	
	
	public ConfigurationManager(Instrumentation<?, ?> log, Class<?>... configurableClasses) {
		this.log = log;
		log.addInstrumentableEvents(MutuaICCConfigurationInstrumentationEvents.values());
		this.configurableClasses = configurableClasses;
	}

	
	protected String serializeStaticFields(Class<?> configurableClass) throws IllegalArgumentException, IllegalAccessException {
		StringBuffer buffer = new StringBuffer();
		Field[] fields = configurableClass.getDeclaredFields();
		for (Field f : fields) {
			ConfigurableElement configurableAnnotation = f.getAnnotation(ConfigurableElement.class);
			if (configurableAnnotation == null) {
				continue;
			}
			String comment = configurableAnnotation.value();
			if (comment.equals("")) {
				String commentReferenceField = configurableAnnotation.sameAs();
				String className = commentReferenceField.replaceAll("(.*)\\.(.*)", "$1");
				String fieldName = commentReferenceField.replaceAll("(.*)\\.(.*)", "$2");
				try {
					// resolve class and field names
					Class<?> referencedClass = Class.forName(className);
					Field referencedField = referencedClass.getField(fieldName);
					ConfigurableElement referencedConfigurableAnnotation = referencedField.getAnnotation(ConfigurableElement.class);
					comment = referencedConfigurableAnnotation.value();
				} catch (Throwable t) {
					comment = "same as field '"+commentReferenceField+"' (reference not found)";
				}
			}
			buffer.append("# ").append(comment).append('\n');
			String fName   = f.getName();
			Object fValue  = f.get(null);
			Class<?> fType = f.getType();
			if (fType == String.class) {
				String s = (String)fValue;
				s = s.replaceAll("\\\\", "\\\\\\\\").
				      replaceAll("\n",   "\\\\n").
				      replaceAll("\r",   "\\\\t").
				      replaceAll("\t",   "\\\\t");
				buffer.append(fName).append("=").append(s).append("\n");
			} else if (fType == int.class) {
				int i = (Integer)fValue;
				buffer.append(fName).append("=").append(i).append("\n");
			} else if (fType == long.class) {
				long l = (Long)fValue;
				buffer.append(fName).append("=").append(l).append("\n");
			} else if (fType == String[].class) {
				String[] ss = (String[])fValue;
				for (String s : ss) {
					s = s.replaceAll("\\\\", "\\\\\\\\").
					      replaceAll("\n",   "\\\\n").
					      replaceAll("\r",   "\\\\t").
					      replaceAll("\t",   "\\\\t");
					buffer.append(fName).append("+=").append(s).append("\n");
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
				buffer.append(")\n").append(fName).append('=').append(fValue.toString()).append('\n');
			} else {
				buffer.append("// don't know how to serialize field '").append(fName).
				       append("', an instance of '").append(fType.getCanonicalName()).append("'");
				for (Class<?> clazz : fType.getClasses()) {
					buffer.append(", subclass of '").append(clazz.getCanonicalName()).append("'");
				}
			}
		}
		return buffer.toString();
	}
	
	protected String serializeConfigurableClasses() throws IllegalArgumentException, IllegalAccessException {
		StringBuffer buffer = new StringBuffer();
		for (Class<?> configurableClass : configurableClasses) {
			buffer.append("\n\n// ").append(configurableClass.getCanonicalName()).
			       append("\n/////////////////////////////////////////////////\n\n").
			       append(serializeStaticFields(configurableClass));
		}
		return buffer.toString();
	}

	protected void desserializeStaticFields(Class<?> configurableClass, String serializedFields) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = configurableClass.getDeclaredFields();
		for (Field f : fields) try {
			String fName   = f.getName();
			Class<?> fType = f.getType();
			if (fType == String.class) {
				String s = serializedFields.replaceAll("(?s).*?\n?"+fName+"=([^\n]*).*", "$1");
				s = s.replaceAll("\\\\n",   "\n").
				      replaceAll("\\\\r",   "\t").
				      replaceAll("\\\\t",   "\t").
				      replaceAll("\\\\\\\\", "\\\\");
				f.set(null, s);
				log.reportEvent(IE_CONFIGURING_STRING_PROPERTY, IP_CONFIGURATION_FIELD_NAME, fName, IP_CONFIGURATION_STRING_FIELD_VALUE, s);
			} else if (fType == long.class) {
				String s = serializedFields.replaceAll("(?s).*?\n?"+fName+"=([^\n]*).*", "$1");
				long l = Long.parseLong(s);
				f.set(null, l);
				log.reportEvent(IE_CONFIGURING_NUMBER_PROPERTY, IP_CONFIGURATION_FIELD_NAME, fName, IP_CONFIGURATION_NUMBER_FIELD_VALUE, l);
			} else if (fType == int.class) {
				String s = serializedFields.replaceAll("(?s).*?\n?"+fName+"=([^\n]*).*", "$1");
				int i = Integer.parseInt(s);
				f.set(null, i);
				log.reportEvent(IE_CONFIGURING_NUMBER_PROPERTY, IP_CONFIGURATION_FIELD_NAME, fName, IP_CONFIGURATION_NUMBER_FIELD_VALUE, i);
			} else if (fType == String[].class) {
				String a = serializedFields.replaceAll("(?s)(\n?)"+fName+"\\+=([^\n]*)", "$1;@<!$2:#_%").
				                            replaceAll("(?s).*?(;@<!.*:#_%).*", "$1").
				                            replaceAll("(?s);@<!", "").
				                            replaceAll("(?s):#_%", "");
				String[] ss = a.split("\n");
				for (int i=0; i<ss.length; i++) {
					String s = ss[i].replaceAll("\\\\n",   "\n").
					                 replaceAll("\\\\r",   "\t").
					                 replaceAll("\\\\t",   "\t").
					                 replaceAll("\\\\\\\\", "\\\\");
					ss[i] = s;
				}
				f.set(null, ss);
				log.reportEvent(IE_CONFIGURING_STRING_ARRAY_PROPERTY, IP_CONFIGURATION_FIELD_NAME, fName, IP_CONFIGURATION_STRING_ARRAY_FIELD_VALUE, ss);
			} else if (fType.isEnum()) DONE: {
				String e = serializedFields.replaceAll("(?s).*?\n?"+fName+"=([^\n]*).*", "$1");
				for (Object enumConstant: fType.getEnumConstants()) {
					if (enumConstant.toString().equals(e)) {
						f.set(null, enumConstant);
						log.reportEvent(IE_CONFIGURING_ENUMERATION_PROPERTY, IP_CONFIGURATION_FIELD_NAME, fName, IP_CONFIGURATION_ENUMERATION_FIELD_VALUE, enumConstant);
						break DONE;
					}
				}
				log.reportEvent(IE_DESSERIALIZATION_ERROR, IP_ERROR_MSG, "Value '"+e+"' is not recognized as a valid value for Enum '"+fType.getCanonicalName()+"'");
			} else {
				log.reportEvent(IE_DESSERIALIZATION_ERROR, IP_ERROR_MSG, "Don't know how to desserialize type '"+fType.getCanonicalName()+"'");
			}
		} catch (NumberFormatException e) {
			log.reportThrowable(e, "Error while attempt to configure field '"+f.getName()+"'");
		}
	}
	
	protected void deserializeConfigurableClasses(String serializedFields) throws IllegalArgumentException, IllegalAccessException {
		for (Class<?> configurableClass : configurableClasses) {
			desserializeStaticFields(configurableClass, serializedFields);
		}
	}

	public void loadFromFile(String filePath) throws IOException, IllegalArgumentException, IllegalAccessException {
		log.reportEvent(IE_LOADING_CONFIGURATION_FILE, IP_FILE_NAME, filePath);
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		StringBuffer fileContents = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) {
			fileContents.append(line).append('\n');
		}
		br.close();
		try {
			for (Class<?> configurableClass : configurableClasses) {
				log.reportEvent(IE_CONFIGURING_CLASS, IP_CLASS, configurableClass);
				desserializeStaticFields(configurableClass, fileContents.toString());
			}
		} catch (Throwable t) {
			log.reportThrowable(t, "Exception while attempting to load values into one of the provided configurable classes");
			t.printStackTrace();
			String modelFilePath = filePath + ".model";
			String serializedFields = serializeConfigurableClasses();
			log.reportDebug("Dumping the configuration model with default values for the provided classes: " + serializedFields);
		}
	}

	public void saveToFile(String filePath) throws IOException, IllegalArgumentException, IllegalAccessException {
		String serializedFields = serializeConfigurableClasses();
		FileOutputStream fout = new FileOutputStream(filePath);
		fout.write(serializedFields.getBytes());
		fout.close();
	}
}