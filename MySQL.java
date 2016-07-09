package de.davidschenk.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.davidschenk.log.ErrorLog;

public class MySQL {
	
	private String host;
	private String port;
	private String dbname;
	private String user;
	private String passwd;
	private String tableName;
	private Connection connection;
	
	private static String filename = "databinf";
	
	
	public MySQL(String table) {
		
		/**
		 * Konstruktor für ein MySQL Objekt.
		 * Beim erzeugen initialisiert er die Credentials f�r dieses Objekt
		 * aus der Datei <code>databinf.txt</code>
		 */
		
		File file = new File(filename);
		
		if (!file.canRead() || !file.isFile())
			System.exit(0);
		
		BufferedReader in = null;
		
		try{
			
			in = new BufferedReader(new FileReader(filename));
			String[] credentials = new String[5];
			String zeile = null;
			int i = 0;
			
			while ((zeile = in.readLine()) != null) {
				credentials[i] = zeile;
				i++;
			}
			
			this.host = credentials[0];
			this.port = credentials[1];
			this.dbname = credentials[2];
			this.user = credentials[3];
			this.passwd = credentials[4];
			in.close();
			
		} catch (IOException ex) {
			System.out.println("Loading database configuration file...[FAILED]");
			ErrorLog.sqlError("I/O Fehler beim auslesen der " + filename);
			
		}
		
		this.tableName = table;
		
		System.out.println("Loading database configuration file...[SUCCESS]");
		System.out.println("Host: "+this.host+"\nPort: "+ this.port+"\n dbname: "+this.dbname+"\n user: "+this.user+"\nTable to collect: "+this.tableName);
		
	}
	

	public void con() {
		
		/**
		 * Verbindung zur MySQL Datenbank und laden der JDBC Treiber
		 */
		
		// Laden der JDBC Treiber
		try {
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			System.out.println("mysql jdbc driver loaded...[TRUE]");
			
		} catch (InstantiationException ex) {
			
			// TODO Auto-generated catch block
			ErrorLog.sqlError("Fehler in der MySQL.con() / init jdbc: " + ex.getMessage());
			System.out.println("InstantiationException!\nmysql jdbc driver loaded...[FALSE]");
			
		} catch (IllegalAccessException ex) {
			
			// TODO Auto-generated catch block
			ErrorLog.sqlError("Fehler in der MySQL.con() / init jdbc: " + ex.getMessage());
			System.out.println("IllegalAccessException!\nmysql jdbc driver loaded...[FALSE]");
			
		} catch (ClassNotFoundException ex) {
			
			ErrorLog.sqlError("Fehler in der MySQL.con() / init jdbc: " + ex.getMessage());
			System.out.println("ClassNotFoundException!\nmysql jdbc driver loaded...[FALSE]");
			
		}
		
		// Verbindung zur Datenbank
		try {
			
			this.connection = DriverManager.getConnection("jdbc:mysql://"+this.host+":"+this.port+"/"+this.dbname+"", this.user, this.passwd);
			System.out.println("database connection...[ESTABLISHED]");
			
		} catch (SQLException ex) {
			
			System.err.println("SQLException!\ndatabase connection...[FAILED]");
		    ErrorLog.sqlError("Fehler in Methode MySQL.con(): " + ex.getMessage() + " VendorErrorCode: " + ex.getErrorCode());
		
		}
		
	}

	
	public void createTable(String ERSTELLE_TABELLE_ANWEISUNG) {
		
		/**
		 * Anlegen einer Tabelle in der Datenbank.
		 * Pr�fung, ob bereits vorhanden, wenn nein, neue erzeugen.
		 */
		
		try{
			DatabaseMetaData metaDaten = connection.getMetaData();
			ResultSet tabellen = metaDaten.getTables(null, "APP", this.dbname, null);
			if(!tabellen.next())
			{
				Statement anweisung = connection.createStatement();
				anweisung.executeUpdate(ERSTELLE_TABELLE_ANWEISUNG);
				anweisung.close();
				System.out.println("table "+ this.dbname +" is created...[TRUE]");
			}
		} catch (SQLException ex) {
			System.err.println("table " + this.dbname + " is created...[FALSE]");
			ErrorLog.sqlError("Fehler in Methode MySQL.createTable(): " + ex.getMessage() + " VendorErrorCode: " + ex.getErrorCode());
		}
	}

	
	public void insert(String userid, String tweetid, String latitude, String longtitude, String user_location, String timestamp, String text, int dq) {
		/**
		 * INSERT Anweisungen, an die Datenbank senden.
		 * Mit prepareStatement(String) ist die Anweisung bereits vorkompiliert und optimiert f�r die SQL-DB --> Geschwindigkeits-Boost
		 * Schutz vor SQL-Injections, durch Platzhalter in der Anweisung.
		 */
		
		// Verbindung zur Datenbank
		this.con();
		
		final String EINFUEGE_ANWEISUNG = "INSERT INTO " + this.tableName + " VALUES(DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try(PreparedStatement einfuegeAnweisung = this.connection.prepareStatement(EINFUEGE_ANWEISUNG);)
		{
			einfuegeAnweisung.setString(1, userid);
			einfuegeAnweisung.setString(2, tweetid);
			einfuegeAnweisung.setString(3, latitude);
			einfuegeAnweisung.setString(4, longtitude);
			einfuegeAnweisung.setString(5, user_location);
			einfuegeAnweisung.setString(6, timestamp);
			einfuegeAnweisung.setString(7, text);
			einfuegeAnweisung.setInt(8, dq);
			einfuegeAnweisung.setNull(9, java.sql.Types.INTEGER);
			einfuegeAnweisung.executeUpdate();
			einfuegeAnweisung.close();
			System.out.println(timestamp + " " + user_location + " DQ:" + dq);
			
		} catch(SQLException ex)
		{
			System.err.println("SQLException!\nINSERT-Statement...[FAILED]");
			ErrorLog.sqlError("Fehler in Methode MySQL.insert(): " + ex.getMessage() + " VendorErrorCode: " + ex.getErrorCode());
			
		} finally {
			
			// Verbindug zur Datenbank wieder terminieren.
			this.close();
		}
	}

	
	public void close() {
		try{
			
			this.connection.close();
			
		} catch (SQLException ex) {
			
			ErrorLog.sqlError("Fehler in der MySQL.close(): " + ex.getMessage() + " VendorErrorCode: " + ex.getErrorCode());
			System.err.println("SQLException!\nDB CLOSING...[FAILED]");
			
		}
		
	}
}