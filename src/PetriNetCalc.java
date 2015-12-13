/**
 * Created by PC on 12/12/2015.
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class PetriNetCalc
{
    //Regex definitions
    // static final String STATE =  "[(]?(\\d+, )*(\\d+)+[)]?";
    static final String POS_INT = "\\d+";





    //Variable Declarations
    Scanner in = new Scanner(System.in);
    String input;
    boolean safeInput=false;

    boolean[] isEnabled;
    int transIn[][];
    int transOut[][];
    String delim = " ";

    String currentMarkingAsString;
    int[] currentMarking;
    Iterator<String> iter;
    ArrayList<String> fired = new ArrayList<String>();
    ArrayList<String> toBeAdded = new ArrayList<String>();

    public static ArrayList<String> markings = new ArrayList<String>(); // Pete public
    public static ArrayList<Integer> toBeAddedLog = new ArrayList<Integer>(); // Pete public

    int transitionLogger = 0; // Pete transition logger int

    public static int noPlaces=0; // Pete public
    public static int noTrans=0; // Pete public

    public PetriNetCalc()
    {
    }





    public void run()
    {
        //Process number of Places
        getNumPlaces();



        //Process number of Transitions
        getNumTrans();



        //Create trans Arrays
        transIn = new int[noTrans][noPlaces];
        transOut = new int[noTrans][noPlaces];



        System.out.println("Enter your input in the format (int, int, . . . int)" +
                "\nYou may also separate your integers by spaces by excluding the comma." +
                "\nNegative values will not be accepted.");



        //Process Transition Inputs
        processTransIn();



        //Process Transition Outputs
        processTransOut();



        getInit();



        do
        {
            iter = toBeAdded.iterator();
            while(iter.hasNext())
            {
                String add = iter.next();
                if(!markings.contains(add))
                    markings.add(add);
                toBeAddedLog.add(transitionLogger); // Pete transiton logger for GUI Tree
                //System.out.println(toBeAddedLog.get(transitionLogger));
            }
            toBeAdded.clear();
            iter = markings.iterator();

            while(iter.hasNext())
            {
                //CHECKPOINT
                currentMarkingAsString = iter.next();
                if(!fired.contains(currentMarkingAsString))
                {
                    fired.add(currentMarkingAsString);
                    currentMarking = convertMarking(currentMarkingAsString);
                    checkEnabled(currentMarking);
                    for(int i=0; i<noTrans; i++)
                    {
                        transitionLogger = i+1; // Pete TreeMap current state of i for current transition

                        if(isEnabled[i])
                        {
                            //CHECKPOINT

                            runMarking(currentMarking, i);
                        }

                    }

                    //CHECKPOINT - DEBUG
                    System.out.println("Entire try executed");


                    //CHECKPOINT - DEBUG

                }
            }
        }while(!toBeAdded.isEmpty());


        //CHECKPOINT - DEBUG
        System.out.println("MADE IT HERE");

        Iterator<String> printer = markings.iterator();

        if(printer.hasNext())
        {
            System.out.println("The following markings are reachable:  ");
            System.out.println("---------------------------------------");

            // Pete TreeMap Debug trace
            /*
            System.out.println("Debug::Size of markings clone ArrayList: " + toBeAddedLog.size());
            for (int i=0;i<toBeAddedLog.size();i++) {
            	System.out.print(" ");
            	System.out.print(toBeAddedLog.get(i));
            }*/

            while(printer.hasNext()) {
                System.out.println(replaceNegs(printer.next()));
            }

            javax.swing.SwingUtilities.invokeLater(new Runnable() { // Pete TreeMap class function call
                public void run() {
                    TreeMap.createAndShowGUI();
                }
            });

        }
        else
        {
            System.out.println("No additional markings were reachable.\n");
            System.out.println("Thank you for using Justonicete, the reachability calculator you can rely on, " +
                    "\nand that was brought to you because it was mandatory.");
        }


        in.close();
    }





    //This method runs through each transition and, given some marking, checks whether or not its preconditions are met, thus determing whether not each one is enabled.
    public void checkEnabled(int[] marking)
    {

        for(int i=0; i<noTrans; i++)
        {

            for(int j=0; j<noPlaces; j++)
            {
                //go through each place for the current marking and current transition. If at any point part of its precondition is not met it stops checking and moves on to the next transition.
                //if it makes it through every transition without "kicking out" it is enabled by the statement isEnabled[i] = true;
                if(transIn[i][j] > marking[j] && marking[j] != -1)
                {
                    isEnabled[i] = false;
                    j = noPlaces;
                }

                if(j == noPlaces - 1)
                {
                    isEnabled[i] = true;
                }
            }
        }

    }





    //String s is any string (the input variable is expected) and String reg is a regular expression (a predefined regex variable is expected)
    private static boolean verify(String s, String reg)
    {
        if(s.matches(reg))
            return true;
        else
            return false;
    }





    //String s is any string (input variable is expected) and int num is any number (noPlaces variable is expected)
    //Method checks a string to make sure it is the in appropriate format for a state OR Transition IO ex:  (W, X, Y, Z)
    //It also makes sure that it has the correct number of places determined by "int num"
    //Typical call would look like verify(input, noPlaces) which will make sure that the input is in the appropriate format
    //  and accounts for the correct number of places
    private boolean verify(String s, int num)
    {
        String tempReg = "[(]?((\\d+, )|(\\d+ )){"+(num-1)+"}(\\d+)+[)]?";

        if(s.matches(tempReg))
            return true;
        else
            return false;
    }





    //Method asks for and stores initial Marking
    private void getInit()
    {
        safeInput = false;
        do
        {
            System.out.println("Please enter the initial marking for your Net:  ");
            input = in.nextLine();

            //NEWEST - CHECKPOINT
            input = input.trim();

            if(verify(input, noPlaces))
            {
                safeInput = true;
                System.out.println();

                input = convertInitial(input);
                markings.add(input);
            }
            else
                System.out.println("\n\n\nERROR - Please enter your marking in the format (int, int, . . . int)" +
                        "\nInclude only positive values and make sure you account for the correct number of places." +
                        "\n----------------------------------------------------------------------------------------\n\n\n");
        }while(!safeInput);
    }





    //Method converts markings into an array of integers for usability
    private int[] convertMarking(String in)
    {

        in = in.replace("(","");
        in = in.replace(",", "");
        in = in.replace(")", "");

        String[] s;
        s = in.split(delim);
        int[] temp = new int[noPlaces];
        for(int i=0; i<noPlaces; i++)
        {
            temp[i] = Integer.parseInt(s[i]);
        }

        return temp.clone();
    }





    //Converts the initial marking to the format (int, int, . . . int)
    private String convertInitial(String in)
    {
        int[] raw = convertMarking(in);
        String retVal = "(" + raw[0];

        for(int i=1; i<raw.length; i++)
        {
            retVal = retVal + ", " + raw[i];
        }
        retVal = retVal + ")";

        return retVal;
    }





    //Method finds runs through the ArrayList markings and uses the markings within it to create a new markings until no
    //unique markings are generated.
    private void runMarking(int[] marking, int trans)
    {
        String newMarking;
        if(marking[0] != -1)
            newMarking=""+(marking[0] - transIn[trans][0] + transOut[trans][0]);
        else
            newMarking="-1";

        for(int i=1; i<noPlaces; i++)
        {
            if(marking[i] != -1)
                newMarking = newMarking + ", " + (marking[i] - transIn[trans][i] + transOut[trans][i]);
            else
                newMarking = newMarking + ", " + "-1";
        }


        //CHECKPOINT - LOOK HERE
        newMarking = convertInitial(newMarking);

        Iterator<String> it = markings.iterator();
        boolean unique=true;
        while(it.hasNext())
        {
            try
            {
                String oldMarking = it.next();

                //DEBUG - CHECKPOINT
                System.out.println("oldMarking = " + oldMarking);
                System.out.println("Iter = " + currentMarkingAsString);
                System.out.println("newMarking = " + newMarking);

                if(newMarking.compareTo(oldMarking) != 0)
                {
                    //CHECKPOINT
                    //System.out.println("NewMarking not equal");

                    int[] old = convertMarking(oldMarking);

                    int[] deltaM = calcDelta(old, convertMarking(newMarking));



                    //CHECKPOINT - DEBUG
                    /*
                    for (int i= 0; i<deltaM.length; i++)
                    {
                        System.out.println(deltaM[i]);
                    }
                    */



                    int noOfZero = 0;

                    for(int i=0; i<noPlaces; i++)
                    {
                        if(deltaM[i] == 0)
                        {
                            noOfZero ++;
                        }
                    }

                    if(noOfZero == noPlaces)
                        unique=false;



                    //CHECKPOINT
                    /*
                    if(unique)
                        System.out.println("Unique");
                    else
                        System.out.println("NOT unique");*/



                    if(unique)
                    {
                        int numberOfPos = 0;
                        int numberOfNeg = 0;

                        for( int i=0; i<noPlaces; i++)
                        {
                            if(deltaM[i] > 0)
                                numberOfPos ++;
                            if(deltaM[i] < 0)
                                numberOfNeg ++;
                            if(numberOfNeg > 0)
                                break;
                        }

                        if(numberOfNeg == 0 && numberOfPos > 0)
                        {
                            int [] replacement = convertMarking(newMarking);
                            calcOmega(deltaM, replacement);


                            newMarking = "(" + replacement[0];

                            for(int i=1; i<noPlaces; i++)
                            {
                                newMarking = newMarking + ", " + replacement[i];
                            }
                            newMarking = newMarking + ")";
                        }
                    }
                }

                //CHECKPOINT
            }
            catch(Exception e)
            {
                continue;
            }

        }

        //ADD MARKINGS HERE
        if(unique)
            if(!toBeAdded.contains(convertInitial(newMarking)));
        toBeAdded.add(convertInitial(newMarking));
    }





    //Calculates the difference between two markings placewise
    private int[] calcDelta(int[] oldMark, int[] newMark)
    {
        int[] retValue = new int[noPlaces];

        for(int i=0; i<noPlaces; i++)
        {
            if(oldMark[i] == -1)
            {
                retValue[i] = 0; //represents an omega token at one of the positions
            }
            else
                retValue[i] = newMark[i] - oldMark[i];
        }

        return retValue.clone();
    }





    //Calculates omega tokens (-1)
    private void calcOmega(int[] delta, int[] newMark)
    {
        for(int i=0; i<noPlaces; i++)
            if(delta[i] > 0)
                newMark[i] = -1;
    }





    //Process Transout
    private void processTransOut()
    {
        for(int i=0; i < noTrans; i++)
        {
            safeInput = false;
            do
            {
                System.out.println("Please enter the outputs for Transition " + (i+1) +":  ");
                input = in.nextLine();


                //NEWEST - CHECKPOINT
                input = input.trim();

                if(verify(input, noPlaces))
                {
                    safeInput = true;
                    System.out.println();
                    input = input.replace("(","");
                    input = input.replace(",", "");
                    input = input.replace(")", "");



                    String[] s;
                    s = input.split(delim);



                    for(int j=0; j<noPlaces; j++)
                    {
                        System.out.println(s[j]);
                        transOut[i][j] = Integer.parseInt(s[j]);
                    }
                }
                else
                    System.out.println("\n\n\nERROR - Please enter your marking in the format (int, int, . . . int)" +
                            "\nInclude only positive values and make sure you account for the correct number of places." +
                            "\n----------------------------------------------------------------------------------------\n\n\n");
            }while(!safeInput);
        }
    }





    //Get Number of Places
    private void getNumPlaces()
    {
        do
        {
            System.out.println("Please enter the number of Places in your net:  ");
            input = in.nextLine();

            //NEWEST - CHECKPOINT
            input = input.trim();

            if(verify(input, POS_INT))
                safeInput = true;
            else
                System.out.println("\n\n\nERROR - Please enter a positive integer!" +
                        "\n----------------------------------------\n\n\n");
        }while(!safeInput);

        //Create Places
        noPlaces = Integer.parseInt(input);
    }





    //Get number of trans
    private void getNumTrans()
    {
        safeInput = false;
        do
        {
            System.out.println("Please enter the number of Transitions in your net:  ");
            input = in.nextLine();

            //NEWEST - CHECKPOINT
            input = input.trim();

            if(verify(input, POS_INT))
                safeInput = true;
            else
                System.out.println("\n\n\nERROR - Please enter a positive integer!" +
                        "\n----------------------------------------\n\n\n");
        }while(!safeInput);



        //Create Transitions
        noTrans = Integer.parseInt(input);
        System.out.println("\n\n\n You entered:  " + noTrans + "\n\n\n");
        isEnabled = new boolean[noTrans];
    }





    //takes a postcondition and determines whether or not each transition is enabled
    public void enabledChecker(String marking)
    {

    }





    public String replaceNegs(String s)
    {
        return s.replace("-1", "w");
    }







    //Process transIn
    private void processTransIn()
    {
        for(int i=0; i < noTrans; i++)
        {

            safeInput = false;
            do
            {
                System.out.println("Please enter the inputs for Transition " + (i+1) +":  ");
                input = in.nextLine();

                //NEWEST - CHECKPOINT
                input = input.trim();

                if(verify(input, noPlaces))
                {
                    safeInput = true;
                    System.out.println();
                    input = input.replace("(","");
                    input = input.replace(",", "");
                    input = input.replace(")", "");



                    String[] s;
                    s = input.split(delim);



                    for(int j=0; j<noPlaces; j++)
                    {
                        System.out.println(s[j]);
                        transIn[i][j] = Integer.parseInt(s[j]);
                    }



                }
                else
                    System.out.println("\n\n\nERROR - Please enter your marking in the format (int, int, . . . int)" +
                            "\nInclude only positive values and make sure you account for the correct number of places." +
                            "\n----------------------------------------------------------------------------------------\n\n\n");
            }while(!safeInput);
        }
    }
}
