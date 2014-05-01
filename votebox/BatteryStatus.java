/**
  * This file is part of VoteBox.
  * 
  * VoteBox is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as published by
  * the Free Software Foundation.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VoteBox, found in the root of any distribution or
  * repository containing all or part of VoteBox.
  * 
  * THIS SOFTWARE IS PROVIDED BY WILLIAM MARSH RICE UNIVERSITY, HOUSTON,
  * TX AND IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS, IMPLIED OR
  * STATUTORY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, WARRANTIES OF
  * ACCURACY, COMPLETENESS, AND NONINFRINGEMENT.  THE SOFTWARE USER SHALL
  * INDEMNIFY, DEFEND AND HOLD HARMLESS RICE UNIVERSITY AND ITS FACULTY,
  * STAFF AND STUDENTS FROM ANY AND ALL CLAIMS, ACTIONS, DAMAGES, LOSSES,
  * LIABILITIES, COSTS AND EXPENSES, INCLUDING ATTORNEYS' FEES AND COURT
  * COSTS, DIRECTLY OR INDIRECTLY ARISING OUR OF OR IN CONNECTION WITH
  * ACCESS OR USE OF THE SOFTWARE.
 */

package votebox;

import java.io.*;

/**
 * Utility for reading battery status of device
 *
 * @author Montrose, Matt Bernhard, Mircea C. Berechet
 */
public class BatteryStatus {

    //public static final String ROOT_JARS = "Votebox.jar";

    /**
     * Reads the batter status based on the operating system
     * @param OS - This way we know which protocol to use based on the OS
     * @return - the percentage of battery as an integer
     */
	public static int read(String OS){
        try{
            if(OS.equals("Windows")){
                String batteryCommandString = "@ECHO OFF\nSETLOCAL\n\nFOR /F \"tokens=*  delims=\"  %%A IN ('WMIC /NameSpace:\"\\\\root\\WMI\" Path BatteryStatus              Get PowerOnline^,RemainingCapacity  /Format:list ^| FIND \"=\"')     DO SET  Battery.%%A\nFOR /F \"tokens=*  delims=\"  %%A IN ('WMIC /NameSpace:\"\\\\root\\WMI\" Path BatteryFullChargedCapacity Get FullChargedCapacity             /Format:list ^| FIND \"=\"')     DO SET  Battery.%%A\n\n:: Calculate runtime capacity\nSET /A Battery.RemainingCapacity = ( %Battery.RemainingCapacity%00 + %Battery.FullChargedCapacity% / 2 ) / %Battery.FullChargedCapacity%\n\n:: Display results\nECHO %Battery.RemainingCapacity%\nGOTO:EOF\n\n:: End localization\nIF \"%OS%\"==\"Windows_NT\" ENDLOCAL\n";
                //This will let us set the working directory for the command prompt
                File file = new File("BatteryStatus.bat");

                // If the file does not exist, then create it.
                if (!file.exists())
                {
                    try
                    {
                        file.createNewFile();
                    }
                    catch (IOException e)
                    {
                        System.out.println("Unable to create file.");
                    }
                }

                // Create the writer.
                BufferedWriter writer;
                try
                {
                    writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
                    writer.write(batteryCommandString);

                    writer.flush();
                    writer.close();
                }
                catch (IOException e)
                {
                    System.out.println("Unable to create BufferedWriter.");
                }

                Process child = Runtime.getRuntime().exec(file.getAbsolutePath().split("!!!"));



                BufferedReader out = new BufferedReader(new InputStreamReader(child.getInputStream()));

                String s = "";
                //This should be at most one line
                while((s = out.readLine()) != null)
                {
                    //TODO Figure out what actually happens when the batch file returns something that isn't an integer...
                    //if(s.contains("error"))
                        return 100;
                    //else
                      //  return Integer.parseInt(s); //format the acpi output to truncate

                }


            }
            else
            {
                if(OS.equals("Linux")){

                    String cmd = "acpi -b";
                    ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
                    pb.redirectErrorStream(true);
                    Process child = pb.start();



                    BufferedReader out = new BufferedReader(new InputStreamReader(child.getInputStream()));

                    String s = "";
                    //This should be at most one line
                    while((s = out.readLine()) != null){
                      //  if(s.contains("power_supply"))
                            return 100;
                        //else
                           // return Integer.parseInt(s.substring(s.indexOf("%")- 2, s.indexOf("%"))); //format the acpi output to truncate

                    }




                }

                else
                {
                    if(OS.equals("OS X"))
                    {
                        return 100;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return 0;
	}
}