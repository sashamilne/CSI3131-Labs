import java.util.Random;
import java.util.concurrent.Semaphore;

public class Lab3
{
	// Configuration
        final static int PORT0 = 0;
	final static int PORT1 = 1;
	final static int CHARGEMAX = 5;

	public static void main(String args[]) 
	{
		final int NOMBRE_AUTOS = 10;
		int i;

		Traversier trav = new Traversier(PORT0,10);

		Auto [] automobile = new Auto[NOMBRE_AUTOS];
		for (i=0; i< 7; i++) automobile[i] = new Auto(i,PORT0,trav);
		for ( ; i<NOMBRE_AUTOS ; i++) automobile[i] = new Auto(i,PORT1,trav);

		Ambulance ambulance = new Ambulance(PORT0,trav);

			/* Partons les fils */
 		trav.start();
		for (i=0; i<NOMBRE_AUTOS; i++) automobile[i].start();  // Démarre les fils automobiles
		ambulance.start();  // Démarre le fil ambulance.

		try {trav.join();} catch(InterruptedException e) { }; // On attend que le traversier termine.
		System.out.println("Traversier arreter");
		// Arrete les autos
		for (i=0; i<NOMBRE_AUTOS; i++) automobile[i].interrupt(); // On arrête les fils automobiles.
		ambulance.interrupt(); // On arrête le fil ambulance.
	}
}


class Auto extends Thread { // la classe pour les fils automobiles

	private int id_auto;
	private int port;
	private Traversier tr;

	public Auto(int id, int prt, Traversier traversier)
	{
		this.id_auto = id;
		this.port = prt;
		this.tr = traversier;
	}

	public void run() 
        {

	   while (true) 
           {
		// Attente
		try {sleep((int) (300*Math.random()));} catch (Exception e) { break;}
		System.out.println("Auto " + id_auto + " arrive au port " + port);

		// Embarquement
		System.out.println("Auto " + id_auto + " monte sur le traversier au port " + port);
		tr.ajouteCharge();  // augmente valeur de la charge
 		
		// Arrive au prochain port
		port = 1 - port ;   
		
		//Debarquement		
		System.out.println("Auto " + id_auto + " descend du traversier au port " + port);
		tr.enleveCharge();   // Diminue charge

		// Terminer
		if(isInterrupted()) break;
	   }
	   System.out.println("Auto "+id_auto+" terminer");
	}
 
}

class Ambulance extends Thread { // la classe pour l'ambulance 

	private int port;
	private Traversier tr;

	public Ambulance(int prt, Traversier traversier)
	{
		this.port = prt;
		this.tr = traversier;
	}

	public void run() 
        {
	   while (true) 
           {
		// Attente
		try {sleep((int) (1000*Math.random()));} catch (Exception e) { break;}
		System.out.println("Ambulance arrive au port " + port);

		// Embarquement
		System.out.println("Ambulance monte sur le traversier au port " + port);
		tr.ajouteCharge();  // augmente valeur de la charge
 		
		// Arrive au prochain port
		port = 1 - port ;   
		
		//Debarquement		
		System.out.println("Ambulance descend du traversier au port " + port);
		tr.enleveCharge();   // Diminue charge

		// Terminer
		if(isInterrupted()) break;
	   }
	   System.out.println("Ambulance terminer");
	}
 
}

class Traversier extends Thread // la classe pour le traversier
{
	private int port=0;  // On commence au port 0
	private int charge=0;  // Aucune charge au debut
	private int nombreTours;  // nombres de tours a executer
	// Semaphores


	public Traversier(int prt, int nbtours)
	{
		this.port = prt;
		nombreTours = nbtours;
	}

	public void run() 
        {
	   int i;
	   int dort;
	   System.out.println("Commence au port " + port + " avec la charge de " + charge + " vehicules");

	   // 10 traverses dans notre journee
	   for(i=0 ; i < nombreTours ; i++)
           {
		// La traverse
		System.out.println("Depart du port " + port + " avec la charge de " + charge + " vehicules");
		System.out.println("Traversee " + i + " avec la charge de " + charge + " vehicules");
		port = 1 - port;
		dort = (int) (100*Math.random());
		try {sleep(dort);} catch (Exception e) { }
		// Arrive au port
		System.out.println("Arrive au port " + port + " avec la charge de " + charge + " vehicules");
		// Debarquement et embarquement
	   }
	}

	// methodes pour manipuler la charge du traversier
	public int getCharge()      { return(charge); }
	public void ajouteCharge()  { charge = charge + 1; }
	public void enleveCharge()  { charge = charge - 1 ; }
}
