package ballotscanner;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A UI Panel which displays relevant election information, such as the date or precinct,
 * as well as the STAR logo.png, which are read in from the jar file or local file system.
 */
public class ElectionInfoPanel extends JPanel {

    /** The UI onto which this panel will be placed */
    private final BallotScannerUI context;

    /** A panel for holding the logos */
    private JPanel logoPanel;

    /** A panel for displaying information about the election */
    private JPanel infoPanel;

    /** A BuffereImage version of the STAR-Vote logo */
    private BufferedImage logo;

    /** Fonts for rendering text on the screen */
    private Font fontBig   = new Font("Arial Unicode", Font.BOLD, 20);
    private Font fontSmall = new Font("Arial Unicode", Font.PLAIN, 18);

    /** List of possible jar files that may contain images */
    public static final String ROOT_JARS[] = {"Scanner.jar"};

    /** Necessary for displaying the date of the election */
    private DateFormat dateFormat;
    private Date date;

    /**
     * Constructor for the panel, loads the file and sets up the GUI components
     *
     * @param ballotScannerUI the context on which the panel is placed
     */
    public ElectionInfoPanel(BallotScannerUI ballotScannerUI){
        context = ballotScannerUI;
        setPreferredSize(new Dimension(600,200));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());

        /* Find the logo file and wrap it as a Java File */
        File file = getFile("images/logo.png");

        /* Read in the file to a BufferedImage */
        try{
            logo = ImageIO.read(file);
        }catch(IOException ioe){
            System.out.println(ioe.getMessage());
            System.err.println("BallotScannerUI: Could not locate logo image");
            logo = null;
        }

        /* Set up the logo panel by drawing the logo on it */
        logoPanel = new JPanel(){
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                if(logo != null){
                    g.drawImage(logo, 100, 5, 400, 90, null);
                }
            }
        };



        logoPanel.setPreferredSize(new Dimension(600, 100));

        /* Set up the infoPanel, scaling it and adding the necessary labels to it */
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(600, 100));

        JLabel info;

        /* Put the election name on the panel */
        info = new JLabel(context.electionName);
        info.setFont(fontBig);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(info);

        /* put the date on the panel */
        dateFormat = new SimpleDateFormat("MMMM d, y");
        date = new Date();

        info = new JLabel(dateFormat.format(date));
        info.setFont(fontSmall);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(info);

        /* Put a message on the panel */
        info = new JLabel("Welcome to this Ballot Scanner Console");
        info.setFont(fontSmall);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(info);

        add(logoPanel);
        add(infoPanel);
    }


    /**
     * Opens the jar containing this code and finds the file with name fileName, then wraps it
     * in a Java File and returns it. If the file is not in the jar, this method will look in the
     * path of the code to find it. If it does not find it, this method will print an error message
     * and then return a null file.
     *
     * @param fileName the name of the desired file
     * @return a Java File of the requested file. If the file is not found, this will be null.
     *
     * NOTE: Most of this is cribbed from @see auditorium.SimpleKeyStore.getInput
     */
    public static File getFile(String fileName){
        /* TODO Finish commenting this? */
        boolean[] jarsExist = new boolean[ROOT_JARS.length];

        String fileOutName = fileName;

        if(fileName.contains("images/"))
            fileOutName = fileName.substring(7);

        File file = new File(fileOutName);
        FileOutputStream fileOutputStream = null;
        System.out.println("Attempting to read file "  + fileOutName);


        InputStream in = null;

        for(int i = 0; i < ROOT_JARS.length; i++){

            File jarFile = new File(ROOT_JARS[i]);

            if(jarFile.exists())
                jarsExist[i] = true;

            try{
                 in = null;


                if(jarFile.exists()){
                    JarFile vbJar = new JarFile(jarFile);

                    JarEntry jEntry;

                    if(fileName.startsWith("/"))
                        jEntry = vbJar.getJarEntry(fileName.substring(1));
                    else{
                        jEntry = vbJar.getJarEntry(fileName);
                    }

                    in = vbJar.getInputStream(jEntry);
                }//if


                if(in != null){
                    fileOutputStream = new FileOutputStream(file);

                    int read;

                    byte[] bytes = new byte[1024];

                    while ((read = in.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, read);
                    }

                }
            }catch(Exception e){
                System.out.println(e.getMessage());
                continue;
            }finally {
                try{
                    if(in != null)
                        in.close();
                    if(fileOutputStream != null)
                        fileOutputStream.close();
                } catch (Exception e){
                    System.err.println("There was a problem loading the image " + fileName + " from a file");
                }
            }
        }

        try{
            File rootFile = new File(fileName.replace('/', File.separatorChar));

            if(!rootFile.exists() && fileName.startsWith("/"))
                rootFile = new File(fileName.substring(1).replace('/', File.separatorChar));

            in = new FileInputStream(rootFile);



            if(in != null){
                fileOutputStream = new FileOutputStream(file);

                int read = 0;

                byte[] bytes = new byte[1024];

                while ((read = in.read(bytes)) != -1) {
                    fileOutputStream.write(bytes, 0, read);
                }

            }
        }catch(Exception e){
            String msg = "load(); path = \""+fileName+"\"";

            for(int i = 0; i < ROOT_JARS.length; i++)
                if(jarsExist[i])
                    msg+=" with \""+ROOT_JARS[i]+"\" found";

            System.err.println("Exception " + msg + " due to  " + e.getMessage());

        }finally {
            try{
                if(in != null)
                    in.close();
                if(fileOutputStream != null)
                    fileOutputStream.close();
            } catch (Exception e){
                System.err.println("There was a problem loading the image " + fileName + " from a file");
            }
        }

        return file;
    }
}