package de.davidschenk.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

/**
 * Klasse mit der automatisch error log files angelegt werden.
 * Diese sollten am besten in den Exceptions genutzt werden,
 * um geworfene Fehler zu dokumentieren.
 * 
 * @author David Schenk
 * 
 * @version 1.0
 * 
 * @param log Instanz der eigenen Klasse zum ausführen der eigenen Methoden
 * @param twitterFile Dateiname der Twitter ErrorLog
 * @param sqlFile Dateiname der Twitter ErrorLog
 */

public class ErrorLog {
	
	// ErrorLog Instanz zur Ausführung der eigenen Methoden
	static ErrorLog log = new ErrorLog();
	static String twitterFile = "twitter_error.txt";
	static String sqlFile = "sql_error.txt";
	
	
	public static void sqlError(String errorMsg) {
		
		/**
		 * Legt einen sql error log an, falls nicht schon vorhanden.
		 * In diesen wird mit einem Timestamp die error message aus
		 * der Esception geschrieben.
		 * 
		 * @param errorMsg Message muss in der Exception mit übergeben werden
		 */
		
		Timestamp tstamp = new Timestamp(System.currentTimeMillis()); 
		log.createFile(sqlFile);
		try(
			BufferedWriter file = new BufferedWriter(new FileWriter(new File(sqlFile), true))) {
			file.write("[" + tstamp + "] " + errorMsg);
			file.newLine();
			file.close();
		} catch (IOException ex) {
			System.err.println("IOException while BufferedWrite on " + sqlFile);
		}
	
	}
	
	
	public static void twitterError(String errorMsg) {
		
		/**
		 * Legt einen twitter error log an, falls nicht schon vorhanden.
		 * In diesen wird mit einem Timestamp die error message aus
		 * der Esception geschrieben.
		 * 
		 * @param errorMsg Message muss in der Esxception mit übergeben werden
		 */
		
		Timestamp tstamp = new Timestamp(System.currentTimeMillis()); 
		log.createFile(twitterFile);
		try(
			BufferedWriter file = new BufferedWriter(new FileWriter(new File(twitterFile), true))) {
			file.write("[" + tstamp + "] " + errorMsg);
			file.newLine();
			file.close();
		} catch (IOException ex) {
			System.err.println("IOException while BufferedWrite on " + twitterFile);
		}
		
	}
	
	
	public void createFile(String filename) {
		
		/**
		 * Erstellt eine Datei auf dem Dateisystem, falls nicht schon vorhanden
		 * 
		 * @param filename Name der Datei die erstellt werden soll
		 */
		
		if(log.checkFile(new File(filename)))
			System.out.println(filename + " created");
	}
	

	public boolean checkFile(File file) {
		
		/**
		 * Prüft ob es die Datei bereits gibt, falls nein wird diese angelegt.
		 * Prüft auch ob Lese- und Schreibzugriff vom System gewährt wird.
		 * 
		 * @param file Objektreferenz auf die anzulegende Datei
		 */
		
		if (file != null) {
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
			} catch (IOException ex) {
				System.err.println("Error creating " + file.toString());
			}
			if (file.isFile() && file.canWrite() && file.canRead())
				return true;
		}
		
		return false;
	}

}