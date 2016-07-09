package de.davidschenk.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.davidschenk.log.ErrorLog;
import de.davidschenk.sql.MySQL;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterAPI {
	
	private static String consumerKey;
	private static String consumerSecret;
	private static String accessToken;
	private static String accessTokenSecret;
	
	private static String filename = "twitterapi";

	public static TwitterStream con() throws TwitterException {
		
		TwitterStream stream = new TwitterStreamFactory(TwitterAPI.conf().build()).getInstance();
		return stream;
	}
	
	private final static ConfigurationBuilder conf() {
		
		File file = new File(filename);
		
		if (!file.canRead() || !file.isFile())
			System.exit(0);
		
		BufferedReader in = null;
		
		try{
			in = new BufferedReader(new FileReader(filename));
			String[] credentials = new String[4];
			String zeile = null;
			int i = 0;
			while ((zeile = in.readLine()) != null) {
				credentials[i] = zeile;
				i++;
			}
			
			consumerKey = credentials[0];
			consumerSecret = credentials[1];
			accessToken = credentials[2];
			accessTokenSecret = credentials[3];
			in.close();
			
		} catch (IOException ex) {
			ErrorLog.twitterError("I/O Fehler beim auslesen der " + filename);
		}
		
		/**
		 * Twitter Konfig.
		 * 
		 * Initialisierung der Verbindung ueber die ConfigurationBuilder Class
		 * http://twitter4j.org/javadoc/twitter4j/conf/ConfigurationBuilder.html
		 * 
		 * @object cb
		 * 
		 * Konfiguration der HTTP-Connection
		 * Connection Timeout: 	20000 ms
		 * Read Timeout:		120000 ms
		 * Streaming Timeout:	300000 ms
		 * HTTP retries:		0
		 * HTTP retry interval:	5
		 * JSON debug output:	false
		 * 
		 */
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey(consumerKey);
		cb.setOAuthConsumerSecret(consumerSecret);
		cb.setOAuthAccessToken(accessToken);
		cb.setOAuthAccessTokenSecret(accessTokenSecret);
		
		return cb;
	}

	public static StatusListener listener(MySQL db) throws TwitterException, ClassNotFoundException {
		
		/**
		 * In dieser Methode kann der Listener programmiert werden.
		 */
		
		final MySQL objectDB = db;
		
		StatusListener statusListener = new StatusListener() {
			@Override
	         public void onStatus(Status status) {				
				
				Timestamp tstamp = new Timestamp(System.currentTimeMillis()); 
				
				// Retweet-Protection
				if(status.isRetweeted() || status.isRetweet()) {
					System.out.println(tstamp + " retweet prevented");
				} else {
				
					// Data Quality Check
					
					/**
					 * Daten Qualität wird bewertet, anhand der ermittelten Informationen.
					 * Stufe 1: Keine Geo Informationen
					 * Stufe 2: Location aus dem Profil
					 * Stufe 3: Place im Tweet
					 * Stufe 4: Breiten- und L�ngengrad
					 */
					
					int dq = 1;
					
					// USER-ID
					Long uid = status.getUser().getId();
					String userid = uid.toString();
				
					// TWEET-ID
					Long tid = status.getId();
					String tweetid = tid.toString();
				
					// LONG LAT
					
					/**
					 * Breiten- und L�ngengrad aus dem Tweet in seperate Variablen speichern.
					 */
					
					String latitude = "N/A";
					String longtitude = "N/A";
				
					if(status.getGeoLocation() != null) {
						Double lat = status.getGeoLocation().getLatitude();
						Double longe = status.getGeoLocation().getLongitude();
						latitude = lat.toString();
						longtitude = longe.toString();
						dq = 4;
					}
					
					// USER_LOCATION
					
					/**
					 * Erst wird die Location aus dem Profil genommen, falls vorhanden.
					 * Dannach wird geschaut, ob der Tweet nen Place hat, falls ja wird user_location �berschrieben.
					 */
					
					String user_location = "N/A";
					
					if(status.getUser().getLocation() != null) {
						user_location = status.getUser().getLocation();
						if(dq < 2) {
							dq = 2;
						}
					} 
					
					if(status.getPlace() != null) {
						user_location = status.getPlace().getFullName();
						if(dq < 3) {
							dq = 3;
						}
					}
				
					// TEXT
					String text = status.getText();
				
					// TIMESTAMP
					Date dt = new Date();
					SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					String timestamp = df.format(dt); // Formatierung zum String
				
					/**
					 * Einfügen der Daten aus dem Tweet in MySQL Object
					 */
				
					objectDB.insert(userid, tweetid, latitude, longtitude, user_location, timestamp, text, dq);
				}
	        }
	               @Override
	               public void onDeletionNotice(StatusDeletionNotice sdn) {
	                   throw new UnsupportedOperationException("Not supported yet."); 
	               }

	               @Override
	               public void onTrackLimitationNotice(int i) {
	                   throw new UnsupportedOperationException("Not supported yet."); 
	               }

	               @Override
	               public void onScrubGeo(long l, long l1) {
	                   throw new UnsupportedOperationException("Not supported yet."); 
	               }

	               @Override
	               public void onStallWarning(StallWarning sw) {
	                   throw new UnsupportedOperationException("Not supported yet.");
	               }
	                
	               @Override
	               public void onException(Exception ex) {
	            	   ErrorLog.twitterError("Fehler in der TwitterAPI.onStatus(): " + ex.getMessage() + " Cause: " + ex.getCause());
	               }
		};
		
		return statusListener;
	}
	
}
