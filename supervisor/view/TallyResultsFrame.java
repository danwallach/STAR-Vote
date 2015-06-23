package supervisor.view;

import tap.BallotImageHelper;
import votebox.AuditoriumParams;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class is the graphical user interface that displays the results of the election to officials as soon
 * as the polls are closed, or as soon as the final results have been tallied. The graphical interface is a JTree
 * containing JTables in its nodes. For each race in an election, the candidateIDs, candidate names, votes received
 * and percentage of total votes received are displayed to the election officials. Additionally, the winning candidate's
 * frame is colored green.
 *
 * TODO The below comment is kind of silly. Maybe develop a unit test for this?
 * This interface can be tested independently by modifying the main method and running the class as stand-alone. A ballot
 * is needed, as well as a custom mapping between all RaceID strings (The "B..." numbers) and a BigInteger representing
 * votes received by that RaceID.
 *
 * @author mrdouglass95
 */
public class TallyResultsFrame extends JFrame{

    /**
     * Constructor for TallyResultsFrame. Automatically handles setting size and visibility.
     *
     * @param parent panel used in determining initial position of frame
     * @param results represents a mapping from RaceIDs to the number of votes received by that ID
     * @param ballot  absolute path of .zip ballot file being used
     */
     public TallyResultsFrame(JPanel parent, Map<String, BigInteger> results, String ballot){
         /* Rely on the super to name the window */
         super("Election Results Window");

         /* Position and set the layout */
         setLocationRelativeTo(parent);
         setLayout(new GridBagLayout());
         setAlwaysOnTop(true);
         GridBagConstraints c = new GridBagConstraints();

         /* Create and add an Election Results label */
         JLabel title = new MyJLabel("Election Results:");
         c.gridx = 0;
         c.gridy = 0;
         c.anchor = GridBagConstraints.LINE_START;
         c.insets = new Insets(10, 10, 0, 10);
         add(title, c);

         /*
          * This will be the graphical component that displays the results. Until we know how the
          * Tally display is configured we can't really say anymore about it.
          */
         JComponent resultsField;

         /* Here is where we read in various data from the ballot: the language,
          * pre-rendered images for the candidates, and the images for the race titles.
          */
         java.util.List<String> languages = BallotImageHelper.getLanguages(ballot);
         Map<String, Image> candidateImgMap = loadBallotRaces(ballot, languages);
         Map<String, Image> titleImgMap = BallotImageHelper.loadBallotTitles(ballot);

         /* Now we read in the configuration for the results display */
         AuditoriumParams params = new AuditoriumParams("supervisor.conf");

         /* If there are no provided images for the results, use the simply version of the tally view. */
         if(candidateImgMap == null || params.getUseSimpleTallyView())
             resultsField = createBasicTable(results);
         else{
             /* Otherwise, if there aren't images for the race titles, use a FancyTable */
             if(titleImgMap == null || params.getUseTableTallyView())
                 resultsField = createFancyTable(results, candidateImgMap);
             /*
              * If there are race title images, create a tree table that can expand and collapse races based on the
              * race title images
              */
             else
                 resultsField = createFancyTreeTable(results, candidateImgMap, titleImgMap);
         }

         /* Now that we know what kind of table we're going to display, set its font */
         resultsField.setFont(new Font("Monospace", Font.PLAIN, 12));

         /* Now put the results table in a JScrollPane, using GridBagLayout, and add the pane to the frame */
         c.gridy = 1;
         c.weightx = 1;
         c.weighty = 1;
         c.fill = GridBagConstraints.BOTH;
         JScrollPane pane = new JScrollPane(resultsField);
         pane.getVerticalScrollBar().setUnitIncrement(8);
         add(pane, c);

         /* Add a button that will allow the user to close the dialog */
         JButton okButton = new MyJButton("OK");
         okButton.setFont(okButton.getFont().deriveFont(Font.BOLD));
         okButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 setVisible(false);
             }
         });
         c.gridy = 2;
         c.weightx = 0;
         c.weighty = 0;
         c.anchor = GridBagConstraints.CENTER;
         c.fill = GridBagConstraints.NONE;
         c.insets = new Insets(10, 10, 10, 10);
         add(okButton, c);

         /* Set the size and then display the frame */
         setSize((int)Math.max(400, getPreferredSize().getWidth()), 400);
         setVisible(true);
     }

    /**
     * Creates a fancy JTree with an invisible root node, where each child of root
     * is an image of the title for that race and each child of the title is a JTree
     * of votes and candidate image columns with no header.
     *
     * @param results map of each race id to a total total votes for that race
     * @param candidateImgMap map of each race id to a candidate name image
     * @param titleImgMap map of each race id to a title label image
     * @return a JTree displaying all this data as described above
     */
    private JTree createFancyTreeTable(Map<String, BigInteger> results, Map<String, Image> candidateImgMap, Map<String, Image> titleImgMap) {

        /* The underlying structure of the sub-sub-JTrees, a map of maps of results to JTables */
        final Map<Map<String, BigInteger>, JTable> modelToView = new HashMap<>();

        /* The structure of the sub-JTree, a map of images to a list of names of title headers */
        final Map<Image, java.util.List<String>> titleToRaces = new HashMap<>();

        /* invisible root node of tree */
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root", true);

        /*
         * Populate the titleToRaces map by looking through the map of titles
         * to images and mapping the title to raceIDs
         */
        for(Image title : titleImgMap.values()){
            /*First build an array of all the raceids that are race titles */
            java.util.List<String> raceIds = new ArrayList<>();
            for(String raceId : titleImgMap.keySet()){
                if(titleImgMap.get(raceId) == title)
                    raceIds.add(raceId);
            }

            /* Now that we've found all the raceIDs corresponding to this title, add it to our map */
            titleToRaces.put(title, raceIds);
        }

        /*
         * Building the tree model by using the found title images as root
         * nodes to trees with candidate name  image nodes
         */
        for(Image titleImg : titleToRaces.keySet()){
            /* Create a new tree node for each title image */
            DefaultMutableTreeNode title = new DefaultMutableTreeNode(titleImg, true);
            Map<String, BigInteger> subResults = new HashMap<>();

            /* Get all of the results for that title image's corresponding race */
            for(String raceId : titleToRaces.get(titleImg))
                subResults.put(raceId, results.get(raceId));

            /* Create a tree which will hold candidate images and results and add it to the race title root */
            DefaultMutableTreeNode res = new DefaultMutableTreeNode(subResults, false);

            root.add(title);
            title.add(res);

            /* not put this new subtree in the overall results tree table */
            modelToView.put(subResults, createFancyTable(subResults, candidateImgMap));
        }

        /* Our model will be an anonymously defined inner class overriding the DefaultTreeModel class */
        @SuppressWarnings("CanBeFinal") TreeModel model = new DefaultTreeModel(root){

            /* An array of children */
            DefaultMutableTreeNode[] rootChildren = new DefaultMutableTreeNode[getChildCount(getRoot())];

            /* TODO Holy crap this is java style I've never seen before...it should probably be fixed...or understood... */

            {
                /* Populate the array of children */
                for(int i=0; i<rootChildren.length; i++){
                    rootChildren[i] = (DefaultMutableTreeNode)super.getChild(getRoot(), i);
                }

                /* If the tree contains children, override its comparison function */
                if(rootChildren.length > 1){
                    Arrays.sort(rootChildren, new Comparator<DefaultMutableTreeNode>() {

                        /*
                         * The new comparison method will compare the two lowest raceID's so that lower raceID's will get
                         * put into lower number nodes so they get displayed at the top of the tree model. This
                         * enforces consistency between the race orders across the preptool, voting session, and here.
                         */
                        public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2){
                            return minRaceId((ArrayList<String>)titleToRaces.get(o1.getUserObject()))
                                 - minRaceId((ArrayList<String>)titleToRaces.get(o2.getUserObject()));
                        }
                    });
                }
            }

            /**
             * Returns the child node at the specified index into the specified parent node's list of children.
             *
             * @param parent the parent node
             * @param index the index into the parent's list of children
             *
             * @return the child at position i of the parent
             */
            @Override
            public Object getChild(Object parent, int index){
                /* If the parent is a root, just look at its list of children */
                if(((DefaultMutableTreeNode)parent).isRoot()){
                    return rootChildren[index];
                }
                /* Otherwise call recursively until a root node is found */
                else {
                    return super.getChild(parent, index);
                }
            }
        };

        /* Create a new tree */
        JTree tree = new JTree(model);
        tree.setEditable(false);
        tree.setRootVisible(false);

        /* Override the way the tree will display itself*/
        tree.setCellRenderer(new TreeCellRenderer(){
            /**
             * This method will render a given cell based on the various properties provided.
             *
             * @param tree the tree containing the cell
             * @param cell the cell to be rendered
             * @param sel whether or not the cell is selected
             * @param expanded whether or not the cell is expanded
             * @param leaf whether or not the cell is a leaf
             * @param row the number of the row that contains this cell
             * @param hasFocus whether or not this cell is focused
             * @return a renderer for the desired component
             */
            public Component getTreeCellRendererComponent(JTree tree,
                                                          Object cell,
                                                          boolean sel,
                                                          boolean expanded,
                                                          boolean leaf,
                                                          int row,
                                                          boolean hasFocus){

                /* We're expecting this to be a tree node */
                if(!(cell instanceof DefaultMutableTreeNode))
                    throw new RuntimeException("Expected DefaultMutableTreeNode, found "+cell);

                /* Now cast the cell to a node, since we know it is one */
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)cell;

                /* Get the user specified data out of the node */
                cell = node.getUserObject();

                /* If the data is a non-null map, it must be a sub-tree */
                if(cell != null && cell instanceof Map){
                    /* Since we have a sub-tree, pull out its table information */
                    JTable table = modelToView.get(cell);
                    table.setMinimumSize(table.getPreferredSize());

                    /* Create a pane for this tree and return it */
                    JScrollPane pane = new JScrollPane(table);
                    pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                    pane.setPreferredSize(table.getPreferredSize());
                    return pane;
                }

                /* If the data is an image, then add it as a JLabel */
                if(cell != null && cell instanceof Image){
                    /* Pull out the image, put it on a JLabel, and return it to "render" it */
                    Image img = (Image)cell;

                    JLabel label = new JLabel(new ImageIcon(img));

                    label.setMinimumSize(new Dimension(img.getWidth(null), img.getHeight(null)));

                    return label;
                }

                /* If neither a map or an image is found, something has gone wrong */
                /* TODO throw an error here instead? */
                return null;
            }
        });

        /* Make sure all of the rows are rendered expanded so all of the results can be viewed initially */
        for(int i = 0; i < tree.getRowCount(); i++)
            tree.expandRow(i);

        return tree;
    }

    /**
     * Creates a table with the columns "votes" & "candidates".
     * Votes holds the number of votes a candidate received.
     * Candidates holds an image representing the candidate that was extracted from the raceImgMap.
     *
     * @param results A map of race-ids to vote totals
     * @param raceImgMap A map of race-ids to images
     * @return A fancy new JTable.
     */
    private JTable createFancyTable(final Map<String, BigInteger> results, final Map<String, Image> raceImgMap) {
        /* First we instantiate an empty table, and some other useful bookkeeping variables */
        JTable fancyTable = new JTable();

        /* The candidate with the most votes */
        String max = "";

        /* The candidate with the second most votes */
        String second = "";

        /* The total number of votes cast */
        int sum = 0;

        /* Look through the results and fill in relevant data */
        for(String race: results.keySet()){
            /* Keep track of the total number of votes */
            sum += results.get(race).intValue();

            /* This is mostly debugging info, I think. TODO Remove it */
            if(race.equals("B" + minRaceId(new ArrayList<>(results.keySet())))){
                System.out.println(new ArrayList<>(results.keySet()));
                System.out.println(race);
                continue;
            }

            /* If we haven't set up the maximum, do so now */
            if(max.equals("")){
                max = race;
            }

            /*If we have, make sure that we haven't found a candidate with more votes */
            else if(results.get(race).intValue() > results.get(max).intValue()){
                second = max;
                max = race;
            } else if(second.equals("") || results.get(race).intValue() > results.get(second).intValue()){
                second = race;
            }
        }

        /* Now we build the table model with the data computed above */
        final int totalVotes = sum;

        /* Grab the image for the winner */
        final Image winnerImg = raceImgMap.get(max);

        /* This is so we can show totals as percentages */
        final DecimalFormat percentFormat = new DecimalFormat("0.000%");

        /* The overridden DefaultTableModel */
        fancyTable.setModel(new DefaultTableModel(){
            /* An array of the entries */
            Map.Entry[] entries = null;

            /* TODO Again, more wacky java... */

            {
                /* Find all of the entries in the result set, i.e. every candidate */
                Set<Map.Entry<String, BigInteger>> var = results.entrySet();
                entries = var.toArray(new Map.Entry[var.size()]);

                /* If there are more than one entries, sort the list */
                if(entries.length > 1){
                    Arrays.sort(entries, new Comparator<Map.Entry>(){

                        public int compare(Map.Entry arg0, Map.Entry arg1) {

                            /* Push any null entries to the back of the list */
                            if(arg0.getValue() == null && arg1.getValue() == null)
                                return 0;

                            if(arg0.getValue() == null)
                                return -1;

                            if(arg1.getValue() == null)
                                return 1;

                            /* Otherwise the candidate with more votes get put at the front */
                            return ((BigInteger)arg1.getValue()).compareTo((BigInteger)arg0.getValue());
                        }

                    });
                }
            }

            /** This table will have 4 columns */
            public int getColumnCount(){ return 4; }

            /**
             * @return the number of rows, i.e. the number of candidates
             */
            public int getRowCount(){ return results.keySet().size(); }

            /**
             * Retrieves a value from the table
             *
             * @param row specifies which candidate to get data from
             * @param col specifies if the entry is a String, image, integer number, or percentage
             * @return the requested value at (row, col) in the table
             */
            @Override
            public Object getValueAt(int row, int col){

                Map.Entry entry = entries[row];

                switch(col){
                    case 0: return entry.getKey();
                    case 1: return raceImgMap.get(entry.getKey());
                    case 2: return entry.getValue();
                    case 3: return percentFormat.format(((BigInteger)entries[row].getValue()).intValue()/(double)totalVotes);
                    default: throw new RuntimeException(col + " >= 4 column value requested.");
                }
            }

            /**
             * No cells are editable.
             *
             * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
             */
            @Override
            public boolean isCellEditable(int row, int col){
                return false;
            }
        });

        /* Set override the rendering for the 2nd column, i.e. the candidate images, in the table */
        TableColumn column = fancyTable.getColumnModel().getColumn(1);

        column.setCellRenderer(new DefaultTableCellRenderer(){

            /**
             * Allow the values in this column (Images) to be overwritten
             *
             * @param value the new image to use
             *
             * @see javax.swing.table.DefaultTableCellRenderer#setValue(Object)
             */
            @Override
            public void setValue(Object value){
                if(value instanceof Image){
                    setIcon(new ImageIcon((Image)value));
                    return;
                }

                super.setValue(value);
            }

            /**
             * Set the backgrounds of winner's cells to green
             *
             * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, Object, boolean, boolean, int, int)
             */
            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                      Object value,
                                                      boolean isSelected,
                                                      boolean hasFocus,
                                                      int row,
                                                      int column) {

                /* Hopefully the value is a non-null Image */
                if(value != null && value instanceof Image){

                    /* Add the image to a JLabel */
                    Image img = (Image)value;

                    JLabel label = new JLabel(new ImageIcon(img));

                    label.setMinimumSize(new Dimension(img.getWidth(null), img.getHeight(null)));

                    /* Add the label to a JPanel, which will be returned */
                    JPanel panel = new JPanel();
                    panel.setLayout(new GridBagLayout());
                    panel.add(label);

                    /* If the image is the image for the winning candidate, paint the background of the panel green */
                    if(img == winnerImg) panel.setBackground(new Color(120,200,120)); else panel.setBackground(Color.white);

                    return panel;
                }else{
                    /* If this isn't an image, something has gone wrong. TODO Throw an error? */
                    return null;
                }

            }
        });


        /* Set various size and proportions for the table based on the image sizes */
        fancyTable.setRowHeight(getTallestImageHeight(raceImgMap));
        column.setWidth(getWidestImageWidth(raceImgMap));
        column.setMinWidth(getWidestImageWidth(raceImgMap));

        /* The second column will be of candidate images, so title it accordingly */
        column.setHeaderValue("Candidate");

        /* The first column will be the ID of the candidate as generated in the preptool (for instance, B1) */
        fancyTable.getColumnModel().getColumn(0).setHeaderValue("Candidate ID");
        fancyTable.getColumnModel().getColumn(0).setWidth(100);
        fancyTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer(){
            {setHorizontalAlignment(JLabel.CENTER);}
        });

        /* The third column will be the raw number of votes that candidate received */
        fancyTable.getColumnModel().getColumn(2).setHeaderValue("Votes Received");
        fancyTable.getColumnModel().getColumn(2).setWidth(175);
        fancyTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer(){
            {setHorizontalAlignment(JLabel.CENTER);}
        });

        /* The fourth column will be a percentage the total votes (in this race) that candidate received */
        fancyTable.getColumnModel().getColumn(3).setHeaderValue("Percentage");
        fancyTable.getColumnModel().getColumn(3).setWidth(100);
        fancyTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer(){
            {setHorizontalAlignment(JLabel.CENTER);}
        });

        /* Set the sizes and fonts, and return the constructed table */
        fancyTable.setPreferredSize(new Dimension(fancyTable.getColumnModel().getColumn(1).getWidth() + 375,
                                                  fancyTable.getRowHeight()*fancyTable.getRowCount() + 20));
        fancyTable.setFont(new Font("Courier New", Font.BOLD, 20));

        return fancyTable;
    }


    /**
     * @param images a Map of images
     * @return the width of the widest image in images.
     */
    private int getWidestImageWidth(Map<String, Image> images){
        int widest = -1;

        /* Look through all the images to find the widest one */
        for(Image img : images.values()){
            if(img.getWidth(null) > widest)
                widest = img.getWidth(null);
        }

        return widest;
    }

    /**
     * @param images a Map of images
     * @return the height of the tallest image in images.
     */
    private int getTallestImageHeight(Map<String, Image> images){
        int tallest = -1;

        /* Look through all the images to find the tallest one */
        for(Image img : images.values()){
            if(img.getHeight(null) > tallest)
                tallest = img.getHeight(null);
        }

        return tallest;
    }

    /**
     * Taking in a ballot location, load all relevant images into a map of race-ids to Images.
     *
     * @param ballot The ballot file to read
     * @param languages The list of languages on the ballot
     * @return a map of race-ids to images, or null if an error was encountered.
     */
    private Map<String, Image> loadBallotRaces(String ballot, java.util.List<String> languages) {
        try {
            /* Since the ballot is a zip file, we will treat it accordingly */
            Map<String, Image> racesToImageMap = new HashMap<>();

            ZipFile file = new ZipFile(ballot);

            Enumeration<? extends ZipEntry> entries = file.entries();

            /* This is pretty standard .zip unzipping. */
            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();

                /* If these are the droids we are looking for, add them to our map of images */
                if(isRaceImage(entry.getName(), languages)){
                    racesToImageMap.put(getRace(entry.getName()), ImageIO.read(file.getInputStream(entry)));
                }
            }

            return racesToImageMap;
        } catch (IOException e) {
            /* TODO Better erroring... */
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Determines if a given file is in fact a race image, i.e. one that is used in a voting session to represent a
     * candidate or a race name.
     *
     * @param entryName the Zip entry to consider
     * @param langs the list of languages to pull the results from
     * @return true if entryName is in the form "media_B*_selected_*.png", false otherwise
     */
    private boolean isRaceImage(String entryName, java.util.List<String> langs){
        /* The beginning of the file name should always be this */
        /* TODO This falls into the file path problems we've been having... */
        if(!entryName.startsWith("media/vvpat/B"))
            return false;

        /* If the file isn't a .png, we have no business with it */
        if(!entryName.endsWith(".png"))
            return false;

        /* We only want images that have been "selected". They have green check marks! */
        if(!entryName.contains("_selected_"))
            return false;

        /* If there is a language specified... */
        if (langs != null)
            /* ...blatantly ignore it and just take the first name in any language */
            if(!entryName.contains(langs.get(0)))
                return false;

        return true;
    }

    /**
     * Extracts a race-id from a zip entry of a race image.
     *
     * @param name the entry of the race image.
     * @return A string in the form B* (e.g. "B4"), that is a valid race id
     */
    private String getRace(String name) {
        int start = name.indexOf('B');
        int end = name.indexOf('_');

        return name.substring(start, end);
    }

    /**
     * Creates a basic table for displaying vote totals.
     * Takes for form "race id" "votes"
     *
     * @param results A map of race ids to vote totals.
     * @return A basic JTable to display
     */
    private JTable createBasicTable(final Map<String, BigInteger> results) {
        /* Override DefaultTableModel as needed */
        TableModel model = new DefaultTableModel(){

            /**
             * We will only have 2 columns
             *
             * @see javax.swing.table.DefaultTableModel#getColumnCount()
             */
            public int getColumnCount(){ return 2; }

            /**
             * @see javax.swing.table.DefaultTableModel#getRowCount()
             */
            public int getRowCount(){ return results.keySet().size(); }

            /**
             * Our columns will be the candidate images and the number of votes received
             *
             * @see javax.swing.table.DefaultTableModel#getColumnName(int)
             */
            public String getColumnName(int column){
                switch(column){
                    case 0: return "Candidate";
                    case 1: return "Votes";
                    default: throw new RuntimeException(column +" >= 2 column name requested.");
                }
            }

            /**
             * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
             */
            public Object getValueAt(int row, int col){
                Set<Map.Entry<String, BigInteger>> var = results.entrySet();
                Map.Entry entry = var.toArray(new Map.Entry[var.size()])[row];

                switch(col){
                    case 0: return entry.getKey();
                    case 1: return entry.getValue();
                    default: throw new RuntimeException("Column " + col + "is an invalid column request.");
                }
            }
        };

        /* return the newly constructed table */
        return new JTable(model);
    }

    /**
     * Determines the raceID with the minimum value number appended to "B"
     * e.g. minRaceID({"B1", "B2", "B3"}) = 1
     *
     * @param s List of RaceIDs of form "B" + some int
     * @return integer value of least appended integer
     */

    private int minRaceId(ArrayList<String> s){
        try{
            /* First grab the first number and set it as minimum. Note that we have to convert it to an integer */
            int min = Integer.parseInt(s.get(0).substring(1));

            /* Standard "find the minimum" algorithm */
            for(int i=1; i<s.size(); i++){
                if(Integer.parseInt(s.get(i).substring(1)) < min){
                    min = Integer.parseInt(s.get(i).substring(1));
                }
            }

            return min;
        }catch(NumberFormatException ex){
            /* TODO Should this throw a RuntimeException? */
            System.out.println("Cannot Order Races");
            return 0;
        }
    }

    /**
     * Use for testing interface as stand alone.
     *
     * TODO Maybe put this in a unit test suite
     *
     * @param args command line args
     */
    public static void main(String[] args){
        Map<String, BigInteger> resMap = new TreeMap<>();
        resMap.put("B1", new BigInteger("1"));
        resMap.put("B2", new BigInteger("2"));
        resMap.put("B3", new BigInteger("3"));
        resMap.put("B4", new BigInteger("45"));
        resMap.put("B5", new BigInteger("5"));
        resMap.put("B6", new BigInteger("6"));
        resMap.put("B7", new BigInteger("7"));
        resMap.put("B8", new BigInteger("8"));
        resMap.put("B9", new BigInteger("9"));
        resMap.put("B10", new BigInteger("10"));
        resMap.put("B11", new BigInteger("11"));
        resMap.put("B12", new BigInteger("12"));
        resMap.put("B13", new BigInteger("13"));
        resMap.put("B14", new BigInteger("14"));
        TallyResultsFrame frame1 = new TallyResultsFrame(new JPanel(), resMap, "/home/mrdouglass95/Dropbox/Votebox/futurama_es006.zip");
        frame1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
