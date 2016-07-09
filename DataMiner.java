package de.davidschenk.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import twitter4j.FilterQuery;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import de.davidschenk.log.ErrorLog;
import de.davidschenk.sql.MySQL;
import de.davidschenk.twitter.TwitterAPI;

public class DataMiner {

	public static void main(String[] args) {
		
		String input = "";
		String table = "";
		String keyword = "";
		BufferedReader cli;
		
		// Aufruf der Consolas Class
		Consolas.welcome();
		Consolas.instruction();
		
		// While-loop für commands
		while (!input.equals("start") || !input.equals("exit")) {
			
			try{
				
				cli = new BufferedReader(new InputStreamReader(System.in));
				System.out.print("cli: ");
				input = cli.readLine();
				
			} catch (IOException ex) {
				
				System.err.println("CLI Syntax Error");
				
			} finally {
				if (input.equals("exit")) {
					System.exit(0);
				} else if (input.equals("help")) {
					Consolas.commandlist();
				} else if (input.equals("start")) {
					// Consolas Class Start Sequenz
					Consolas.start();
					break;
				}
			}
		
		}
		
		try{
			// Eingabe der Tabelle und des Keywords
			cli = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Name of DB/Table to write: ");
			table = cli.readLine();
			
			System.out.print("Keyword to filter twitter: ");
			keyword = cli.readLine();
			
		} catch (IOException ex) {
			
			System.err.println("CLI Syntax Error");
			
		}
		
		
		try{
			// MySQL Objekt initialisiert sich mit credentials
			MySQL db = new MySQL(table);
			
			// Verbindung zu Twitter
			TwitterStream stream = TwitterAPI.con();
			System.out.println("Connection with Twitter...[ESTABLISHED]");
			
			// Listener konfigurieren
			StatusListener statusListener = TwitterAPI.listener(db);
			FilterQuery fq = new FilterQuery();
			
			fq.track(keyword);
			
			stream.addListener(statusListener);
			stream.filter(fq);
			
			
		} catch (TwitterException ex) {
			
			ErrorLog.twitterError("Fehler in der TwitterAPI.con(): " + ex.getErrorMessage() + " ErroCode: " + ex.getErrorCode());
			System.err.println("Connection with Twitter...[FAILED]");
			
		} catch (ClassNotFoundException ex) {
			
			ErrorLog.twitterError("Fehler in der Listener Methode: " + ex.getMessage() + " Cause: " + ex.getCause());
			
		}

	}

}
