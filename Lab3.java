import java.util.concurrent.Semaphore;

public class Lab3 {
  // Configuration
  final static int PORT0 = 0;
  final static int PORT1 = 1;
  final static int MAXLOAD = 5;

  public static void main(String args[]) {
    final int NUM_CARS = 10;
    int i;

    Ferry fer = new Ferry(PORT0, 10);

    Auto[] automobile = new Auto[NUM_CARS];
    for (i = 0; i < 7; i++) {
      automobile[i] = new Auto(i, PORT0, fer);
    }
    for (; i < NUM_CARS; i++) {
      automobile[i] = new Auto(i, PORT1, fer);
    }

    Ambulance ambulance = new Ambulance(PORT0, fer);

    /* Start the threads */
    fer.start(); // Start the ferry thread.
    for (i = 0; i < NUM_CARS; i++) {
      automobile[i].start(); // Start automobile threads
    }
    ambulance.start(); // Start the ambulance thread.

    try {
      fer.join();
    } catch (InterruptedException e) {} // Wait until ferry terminates.

    System.out.println("Ferry stopped.");
    // Stop other threads.
    for (i = 0; i < NUM_CARS; i++) {
      automobile[i].interrupt(); // Let's stop the auto threads.
    }
    ambulance.interrupt(); // Stop the ambulance thread.
  }
}

class Auto extends Thread { // Class for the auto threads.
  private int id_auto;
  private int port;
  private Ferry fry;

  public Auto(int id, int prt, Ferry ferry) {
    this.id_auto = id;
    this.port = prt;
    this.fry = ferry;
  }

  public void run() {
    while (true) {
      // Delay
      try {
        sleep((int) (300 * Math.random()));
      } catch (Exception e) {
        break;
      }
      System.out.println("Auto " + id_auto + " arrives at port " + port);

      // Board
      fry.autoArrives(port);
      System.out.println("Auto " + id_auto + " boards on the ferry at port " + port);
      fry.boardVehicle(); // increment the ferry load

      // Arrive at the next port
      port = 1 - port;

      // disembark
      fry.disembarkVehicle(); // Reduce load
      System.out.println("Auto " + id_auto + " disembarks from ferry at port " + port);

      // Terminate
      if (isInterrupted()) break;
    }
    System.out.println("Auto " + id_auto + " terminated");
  }
}

class Ambulance extends Thread { // the Class for the Ambulance thread
  private int port;
  private Ferry fry;

  public Ambulance(int prt, Ferry ferry) {
    this.port = prt;
    this.fry = ferry;
  }

  public void run() {
    while (true) {
      // Attente
      try {
        sleep((int) (1000 * Math.random()));
      } catch (Exception e) {
        break;
      }
      System.out.println("Ambulance arrives at port " + port);

      // Board
      fry.ambulanceArrives(port);
      System.out.println("Ambulance boards the ferry at port " + port);
      fry.boardAmbulance(); // increment the load

      // Arrive at the next port
      port = 1 - port;

      // Disembarkment
      fry.disembarkVehicle(); // Reduce load
      System.out.println("Ambulance disembarks the ferry at port " + port);

      // Terminate
      if (isInterrupted()) break;
    }
    System.out.println("Ambulance terminates.");
  }
}

class Ferry extends Thread { // The ferry Class
  private int port = 0; // Start at port 0
  private int load = 0; // Load is zero
  private int numCrossings; // number of crossings to execute
  private static final int MAXLOAD = 5;

  // Semaphores
  private final Semaphore mutex = new Semaphore(1);
  private final Semaphore[] board = {new Semaphore(0), new Semaphore(0)};
  private final Semaphore disembark = new Semaphore(0);
  private final Semaphore ready = new Semaphore(0);

  private boolean ambulanceOnBoard = false;
  private int waitingToDisembark = 0;

  public Ferry(int prt, int nbtours) {
    this.port = prt;
    numCrossings = nbtours;
  }

  public void run() {
    System.out.println("Start at port " + port + " with a load of " + load + " vehicles");
    // numCrossings crossings in our day
    for (int i = 0; i < numCrossings; i++) {
      // The crossing
      load();
      System.out.println("Departure from port " + port + " with a load of " + load + " vehicles");
      System.out.println("Crossing " + i + " with a load of " + load + " vehicles");
      port = 1 - port;
      try {
        sleep((int) (100 * Math.random()));
      } catch (Exception e) {}
      // Arrive at port
      System.out.println("Arrive at port " + port + " with a load of " + load + " vehicles");
      // Disembarkment et loading
      unload(); 
    }
  }

  // methodes to manipulate the load of the ferry
  public int getLoad() {
    return (load);
  }

  public void addLoad() {
    load = load + 1;
  }

  public void reduceLoad() {
    load = load - 1;
  }

  public void autoArrives(int prt) {
    while (true) {
      try {
        mutex.acquire();
        boolean can = (port == prt) && (load < MAXLOAD) && (waitingToDisembark == 0);
        mutex.release();
        if (can) break;
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
    try {
      board[prt].acquire(); // Wait for ferry to allow boarding at port
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public void ambulanceArrives(int prt) {
    while (true) {
      try {
        mutex.acquire();
        boolean can = (port == prt) && (load < MAXLOAD) && (waitingToDisembark == 0);
        mutex.release();
        if (can) break;
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  public void boardVehicle() {
    try {
      mutex.acquire();
      load++;
      if (load == MAXLOAD) ready.release();
      mutex.release();
    } catch (InterruptedException e) {}
  }

  public void boardAmbulance() {
    try {
      mutex.acquire();
      load++;
      ambulanceOnBoard = true;
      ready.release(); // Signal ferry to depart immediately
      mutex.release();
    } catch (InterruptedException e) {}
  }

  public void disembarkVehicle() {
    try {
      disembark.acquire();
      mutex.acquire();
      load--;
      waitingToDisembark--;
      mutex.release();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void load() {
    if (!ambulanceOnBoard) {
      for (int i = 0; i < Lab3.MAXLOAD; i++) {
        board[port].release(); // Allow vehicles to board
      }
      try {
        ready.acquire(); // Wait for load to be full or ambulance forces departure
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private void unload() {
    try {
      mutex.acquire();
      waitingToDisembark = load;
      mutex.release();

      for (int i = 0; i < load; i++) {
        disembark.release(); // Allow vehicles to disembark
      }

      while (true) {
        mutex.acquire();
        if (waitingToDisembark == 0) {
          ambulanceOnBoard = false;
          mutex.release();
          break;
        }
        mutex.release();
        Thread.sleep(10); // Wait for all to disembark
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
