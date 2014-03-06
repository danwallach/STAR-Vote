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

public class ElectionInfoPanel extends JPanel {

    private final BallotScannerUI context;

    private JPanel logoPanel;
    private JPanel infoPanel;

    private BufferedImage logo;
    private Font fontBig   = new Font("Arial Unicode", Font.BOLD, 20);
    private Font fontSmall = new Font("Arial Unicode", Font.PLAIN, 18);

    public static final String ROOT_JARS[] = {"Scanner.jar"};

    private DateFormat dateFormat;
    private Date date;

    public ElectionInfoPanel(BallotScannerUI ballotScannerUI){
        context = ballotScannerUI;
        setPreferredSize(new Dimension(600,200));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());

        File file = getFile("images/logo.png");

        try{
            logo = ImageIO.read(file);
        }catch(IOException ioe){
            System.out.println(ioe.getMessage());
            System.err.println("BallotScannerUI: Could not locate logo image");
            logo = null;
        }

        logoPanel = new JPanel(){
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                if(logo != null){
                    g.drawImage(logo, 100, 5, 400, 90, null);
                }
            }
        };



        logoPanel.setPreferredSize(new Dimension(600, 100));

        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(600, 100));

        JLabel info;

        info = new JLabel(context.electionName);
        info.setFont(fontBig);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(info);

        dateFormat = new SimpleDateFormat("MMMM d, y");
        date = new Date();

        info = new JLabel(dateFormat.format(date));
        info.setFont(fontSmall);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(info);

        info = new JLabel("Welcome to this Ballot Scanner Console");
        info.setFont(fontSmall);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(info);

        add(logoPanel);
        add(infoPanel);
        //add(Box.createRigidArea(new Dimension(600, 50)));
    }

    public static File getFile(String fileName){
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

                    JarEntry jEntry = null;

                    if(fileName.startsWith("/"))
                        jEntry = vbJar.getJarEntry(fileName.substring(1));
                    else{
                        jEntry = vbJar.getJarEntry(fileName);
                    }

                    in = vbJar.getInputStream(jEntry);
                }//if


                if(in != null){
                    fileOutputStream = new FileOutputStream(file);

                    int read = 0;

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
        }//for

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