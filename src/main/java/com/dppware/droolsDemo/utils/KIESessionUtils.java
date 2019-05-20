package com.dppware.droolsDemo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.kie.api.KieBase;
import org.kie.api.marshalling.Marshaller;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.marshalling.ObjectMarshallingStrategyAcceptor;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.marshalling.MarshallerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KIESessionUtils {
	
	
	public static void save(KieSession kieSession, File file) {
			try {
				log.info("Saving the KieSession to file: " + file.getCanonicalPath());
			} catch (IOException ioe) {
				log.error("Error retrieving the canonical path of the file.", ioe);
				//Not much we can do here, just an error while logging. So swallow the exception.
				//BTW, if we get an error here, we will probably get errors somewhere further down as well.....
			}
			// TODO: Might want to buffer the writes.
			FileOutputStream fos = null;
			ObjectOutputStream oos = null;
			try {
				fos = new FileOutputStream(file);	
				oos = new ObjectOutputStream(fos);
				oos.writeObject(kieSession.getKieBase());
				/*
				 * It seems that the Marshaller does not persist the actual SessionClock, which is a problem when using the PseudoClock, so we
				 * persist the SessionConfiguration, Environment and clock time to be able to restore the pseudo-clock (if it's used).
				 */
				KieSessionConfiguration kieSessionConfiguration = kieSession.getSessionConfiguration();
				oos.writeObject(kieSessionConfiguration);
				
				Marshaller marshaller = createSerializableMarshaller(kieSession.getKieBase());
				marshaller.marshall(fos, kieSession);
			} catch (FileNotFoundException fnfe) {
				String errorMessage = "Cannot find file to save KieSession.";
				log.error(errorMessage, fnfe);
				throw new RuntimeException(errorMessage, fnfe);
			} catch (IOException ioe) {
				String errorMessage = "Unable to save KieSession.";
				log.error(errorMessage, ioe);
				throw new RuntimeException(errorMessage, ioe);
			} finally {
				if (oos != null) {
					try {
						oos.close();
					} catch (IOException e) {
						log.warn("Unable to close ObjectOutputStream.");
						// Not much we can do here, so swallowing excepion.
					}
				} else {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							log.warn("Unable to close FileOutputStream.");
							// Not much we can here, so swallowing exception.
						}
					}
				}
			}
			log.info("Succesfully saved KieSession to file.");
	}
	
	public static KieSession load(File file) {
		try {
			log.info("Loading KieSession from file: " + file.getCanonicalPath());
		} catch (IOException ioe) {
			log.error("Error retrieving the canonical path of the file.", ioe);
			//Not much we can do here, just an error while logging. So swallow the exception.
			//BTW, if we get an error here, we will probably get errors somewhere further down as well.....
		}
		
		// TODO: We might want to buffer the reads ..
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);

			KieBase kieBase = (KieBase) ois.readObject();
			/*
			 * The KieSessionConfiguration contains, among other things the session clock.
			 * If we were using the PseudoClock, the correct time is already set when deserializing the KieSessionConfiguration.
			 */
			KieSessionConfiguration kieSessionConfiguration = (KieSessionConfiguration) ois.readObject();
			
			Marshaller marshaller = createSerializableMarshaller(kieBase);
			KieSession kieSession = marshaller.unmarshall(fis, kieSessionConfiguration, null);
			return kieSession;
		} catch (FileNotFoundException fnfe) {
			String errorMessage = "Cannot find file to load KieSession.";
			log.error(errorMessage, fnfe);
			throw new RuntimeException(errorMessage, fnfe);
		} catch (ClassNotFoundException cnfe) {
			String errorMessage = "Error loading serialized KieBase from file.";
			log.error(errorMessage, cnfe);
			throw new RuntimeException(errorMessage, cnfe);
		} catch (IOException ioe) {
			String errorMessage = "Error loading stored KieSession.";
			log.error(errorMessage, ioe);
			throw new RuntimeException(errorMessage, ioe);
		} finally {
			if (ois != null) {
				// This will also close the FIS.
				try {
					ois.close();
				} catch (IOException e) {
					log.warn("Unable to close ObjectInputStream.");
					// Not much we can do here, so swallowing exception.
				}
			} else {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						log.warn("Unable to close FileInputStream.");
						// Not much we can do here, so swallowing exception.
					}
				}
			}

		}
}
	
	private static Marshaller createSerializableMarshaller(KieBase kBase) {
			ObjectMarshallingStrategyAcceptor acceptor = MarshallerFactory.newClassFilterAcceptor(new String[] { "*.*" });
			ObjectMarshallingStrategy strategy = MarshallerFactory.newSerializeMarshallingStrategy(acceptor);
			Marshaller marshaller = MarshallerFactory.newMarshaller(kBase, new ObjectMarshallingStrategy[] { strategy });
			return marshaller;
	}
}
