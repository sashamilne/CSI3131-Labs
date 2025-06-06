import java.io.*;

public class MandelBrot 
{
    public static void main(String[] args) throws IOException 
    {

	    if(args.length < 5)
	    {
		    System.out.println("Usage: java MandelBrot <CornerX> <CornerY> <Size> <SizePixels> <MinBox>");
		    System.exit(1);
	    }

	    try 
	    {
	            System.out.println("Hello - Creating MandelBrot Diagram");
		        MBFrame frame = new MBFrame(Double.parseDouble(args[0]),
			    	       Double.parseDouble(args[1]),
			    	       Double.parseDouble(args[2]),
			               Integer.parseInt(args[3]),
			               Integer.parseInt(args[4]));
	    }
	    catch(NumberFormatException e)
	    {
		    System.out.println("Error in arguments" + e);
		    System.out.println("Usage: java MandelBrot <CornerX> <CornerY> <Size> <SizePixels> <MinBox>");
	    }

    }
}

