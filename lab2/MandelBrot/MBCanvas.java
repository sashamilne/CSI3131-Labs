import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*-----------------------------------------------------------
 *
 * This is the component onto which the diagram is displayed
 *
 * ----------------------------------------------------------*/

public class MBCanvas extends Canvas
{
   final private MBGlobals mg;   // reference to global definitions
   private ExecutorService executor; // thread pool
   private List<Thread> threadList; // Store threads to join later
   
   private enum ThreadUsageType { 
      // Define any specific usage types if needed
      ThreadPool,
      ThreadList
   };
   
   // Change thread usage type as needed
   private final ThreadUsageType THREAD_USAGE_TYPE = ThreadUsageType.ThreadList;
   
   public MBCanvas(MBGlobals mGlob)
   {
	mg = mGlob;
        setSize(mg.pixeldim, mg.pixeldim);
   }

   @Override
   public void paint(Graphics g)  // this method paints the canvas
   {
	   /* reset screen to blank */
      g.setColor(Color.white);
	   g.fillRect(0,0,mg.pixeldim, mg.pixeldim);

      if(THREAD_USAGE_TYPE == ThreadUsageType.ThreadPool) {
            
         // Create a fixed thread pool (adjust number as needed)
         executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

      /* Call method to add MandelBrot pattern */
      /* Run MBCompute in this thread */
         Rectangle nrect = new Rectangle(0,0,mg.pixeldim,mg.pixeldim);
         findRectangles(nrect);

         // Wait for all threads to finish
         executor.shutdown();
         try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
      
      else if(THREAD_USAGE_TYPE == ThreadUsageType.ThreadList) {
         // Initialize thread list
         threadList = new ArrayList<>();
         
         // Call method to add MandelBrot pattern
         Rectangle nrect = new Rectangle(0,0,mg.pixeldim,mg.pixeldim);
         findRectangles(nrect);
         
         // Wait for all threads to finish
         for (Thread thread : threadList) {
            try {
               thread.join();
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }
      }
   }


   private void findRectangles(Rectangle mrect)
   {
      // Compute the maximum pixel values for hor (i) and vert (j) 
      int maxi = mrect.x + mrect.width;
      int maxj = mrect.y + mrect.height;

      // Only when the square is small enough do we fill
      if( (maxi - mrect.x) <= mg.minBoxSize)  
      {
         if(THREAD_USAGE_TYPE == ThreadUsageType.ThreadPool) {
            // Submit the MBPaint task to the executor
            executor.submit(() -> {
               new MBPaint(this, mg, mrect).run();
            });
         } 
         else if(THREAD_USAGE_TYPE == ThreadUsageType.ThreadList) {
            // Create a new thread for the MBPaint task and add it to the list
            Thread thread = new Thread(() -> {
               new MBPaint(this, mg, mrect).run();
            });
            threadList.add(thread);
            thread.start();
         }
        return;
      }

      // recursiverly compute the four subquadrants
      int midw = mrect.width/2;
      int wover = mrect.width % 2;  // for widths not divisable by 2 
      int midh = mrect.height/2;
      int hover = mrect.height % 2;  // for heights not divisable by 2 

      	    // First quadrant
      Rectangle[] nrects =  {
         new Rectangle(mrect.x, mrect.y, midw, midh), // First quadrant
         new Rectangle(mrect.x+midw, mrect.y, midw+wover, midh), // Second quadrant
         new Rectangle(mrect.x, mrect.y+midh, midw, midh+hover), // Third quadrant
         new Rectangle(mrect.x+midw, mrect.y+midh, midw+wover, midh+hover) // Fourth quadrant
      };

      // Submit each rectangle to the executor for processing
      for (Rectangle rect : nrects) {
         findRectangles(rect);
      }
   }

}
