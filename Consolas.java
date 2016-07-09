package de.davidschenk.main;

public class Consolas {

	public static void welcome() {
		System.out.println("####################################");
		System.out.println("#                                  #");
		System.out.println("#                                  #");
		System.out.println("#       WELCOME TO DATAMINER       #");
		System.out.println("#           VERSION 2.0            #");
		System.out.println("#                                  #");
		System.out.println("#       DEV BY DAVID SCHENK        #");
		System.out.println("#                                  #");
		System.out.println("#                                  #");
		System.out.println("#         MINE THE WORLD           #");
		System.out.println("#                                  #");
		System.out.println("####################################");
		System.out.println("\n\n");
	}
	
	public static void instruction() {
		System.out.println("By typing commands u can control the dataminer");
		System.out.println("If u need help ... write help in the console");
		System.out.println("For better experience start the dataminer in a screen session like \"screen -S <name>\" ");
	}
	
	public static void commandlist() {
		System.out.println("------- Command List -------");
		System.out.println("\n");
		System.out.println("start\t- start the dataminer");
		System.out.println("exit\t- close the dataminer");
	}
	
	public static void start() {
		System.out.println("The dataminer will start now ...");
		System.out.println("Please follow the instructions ...");
		System.out.println("\n\n");
	}
}
