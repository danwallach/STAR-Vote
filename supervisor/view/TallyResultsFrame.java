package supervisor.view;

import tap.BallotImageHelper;
import votebox.AuditoriumParams;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
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
 * Created with IntelliJ IDEA.
 * User: mrdouglass95
 * Date: 7/31/13
 * To change this template use File | Settings | File Templates.
 *
 * This Class is the graphical user interface that displays the results of the election to officials as soon as the polls
 * are closed, or as soon as the final results have been tallied. It takes in the String absolute path to the ballot being
 * used and a Map<String, BigInteger> representing the results of an election. This graphical interface is a JTree
 * containing JTables in it's nodes. For each Race in an election, the candidateIDs, Candidate names, Votes Recieved and
 * percentage of total votes received are displayed to the election officials.
 *
 * This interface can be tested independently by modifying the main method and running the class as stand-alone. A ballot
 * is needed, as well as a custom mapping between all RaceID strings (The "B..." numbers) and a BigInteger representing
 * votes received by that RaceID.
 *
 */

public class TallyResultsFrame extends JFrame{

    /**
     * Only Constructor for TallyResultsFrame. Automatically handles setting size and visibility.
     *
     * @param parent panel used in determining initial position of frame
     * @param results represents a mapping from RaceIDs to the number of votes received by that ID
     * @param ballot  absolute path of .zip ballot file being used
     */
     public TallyResultsFrame(JPanel parent, Map<String, BigInteger> results, String ballot){
         super("Election Results Window");

         setLocationRelativeTo(parent);
         setLayout(new GridBagLayout());
         setAlwaysOnTop(true);
         GridBagConstraints c = new GridBagConstraints();

         JLabel title = new MyJLabel("Election Results:");
         c.gridx = 0;
         c.gridy = 0;
         c.anchor = GridBagConstraints.LINE_START;
         c.insets = new Insets(10, 10, 0, 10);
         add(title, c);

         JComponent resultsField = null;
         java.util.List<String> languages = BallotImageHelper.getLanguages(ballot);
         Map<String, Image> candidateImgMap = loadBallotRaces(ballot, languages);
         Map<String, Image> titleImgMap = BallotImageHelper.loadBallotTitles(ballot);

         AuditoriumParams params = new AuditoriumParams("supervisor.conf");

         System.out.println("Tally Results Frame: " + results);

         if(candidateImgMap == null || params.getUseSimpleTallyView())
             resultsField = createBasicTable(results);
         else{
             if(titleImgMap == null || params.getUseTableTallyView())
                 resultsField = createFancyTable(results, candidateImgMap);
             else
                 resultsField = createFancyTreeTable(results, candidateImgMap, titleImgMap);
         }//if

         resultsField.setFont(new Font("Monospace", Font.PLAIN, 12));
         // resultsField.setBackground(new Color(140,180,255));
         c.gridy = 1;
         c.weightx = 1;
         c.weighty = 1;
         c.fill = GridBagConstraints.BOTH;
         JScrollPane pane = new JScrollPane(resultsField);
         pane.getVerticalScrollBar().setUnitIncrement(8);
         // pane.setBackground(new Color(48,149,242));
         add(pane, c);

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

         setSize((int)Math.max(400, getPreferredSize().getWidth()), 400);
         setVisible(true);

         System.out.println("Results: "+results);
     }

    /**
     * Creates a fancy JTree with an invisible root node, where each child of root
     * is an image of the title for that race and each child of the title is a JTree with no header
     * of votes and candidate image columns.
     *
     * @param results - map of each race id to a total total
     * @param candidateImgMap - map of each race id to a candidate image name
     * @param titleImgMap - map of each race id to a title label
     * @return a JTree displaying all this data as described above
     */
    private JTree createFancyTreeTable(Map<String, BigInteger> results, Map<String, Image> candidateImgMap, Map<String, Image> titleImgMap) {
        final Map<Map<String, BigInteger>, JTable> modelToView = new HashMap<Map<String, BigInteger>, JTable>();

        //invisible root node of tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root", true);

        final Map<Image, java.util.List<String>> titleToRaces = new HashMap<Image, java.util.List<String>>();

        System.out.println(titleImgMap);

        for(Image title : titleImgMap.values()){
            java.util.List<String> raceIds = new ArrayList<String>();
            for(String raceId : titleImgMap.keySet()){
                if(titleImgMap.get(raceId) == title)
                    raceIds.add(raceId);
            }//for

            titleToRaces.put(title, raceIds);
        }//for

        System.out.println(titleToRaces);

        //Building the tree model
        for(Image titleImg : titleToRaces.keySet()){
            DefaultMutableTreeNode title = new DefaultMutableTreeNode(titleImg, true);
            Map<String, BigInteger> subResults = new HashMap<String, BigInteger>();

            for(String raceId : titleToRaces.get(titleImg))
                subResults.put(raceId, results.get(raceId));

            DefaultMutableTreeNode res = new DefaultMutableTreeNode(subResults, false);

            root.add(title);
            title.add(res);
            System.out.println("Fancy tree table: " + subResults);
            modelToView.put(subResults, createFancyTable(subResults, candidateImgMap));
        }//for

        TreeModel model = new DefaultTreeModel(root){

            DefaultMutableTreeNode[] rootChildren = new DefaultMutableTreeNode[getChildCount(getRoot())];

            {
                for(int i=0; i<rootChildren.length; i++){
                    rootChildren[i] = (DefaultMutableTreeNode)super.getChild(getRoot(), i);
                }

                if(rootChildren.length > 1){
                    Arrays.sort(rootChildren, new Comparator<DefaultMutableTreeNode>() {

                        public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2){
                            return minRaceId((ArrayList<String>)titleToRaces.get(o1.getUserObject()))
                                 - minRaceId((ArrayList<String>)titleToRaces.get(o2.getUserObject()));
                        }
                    });
                }
            }

            @Override
            public Object getChild(Object parent, int index){
                if(((DefaultMutableTreeNode)parent).isRoot()){
                    return rootChildren[index];
                } else {
                    return super.getChild(parent, index);
                }
            }
        };

        JTree tree = new JTree(model);
        tree.setEditable(false);
        tree.setRootVisible(false);
        tree.setCellRenderer(new TreeCellRenderer(){
            public Component getTreeCellRendererComponent(JTree tree,
                                                          Object value,
                                                          boolean sel,
                                                          boolean expanded,
                                                          boolean leaf,
                                                          int row,
                                                          boolean hasFocus){

                if(!(value instanceof DefaultMutableTreeNode))
                    throw new RuntimeException("Expected DefaultMutableTreeNode, found "+value);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;

                value = node.getUserObject();

                if(value != null && value instanceof Map){
                    JTable table = modelToView.get(value);
                    table.setMinimumSize(table.getPreferredSize());

                    JScrollPane pane = new JScrollPane(table);
                    pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                    pane.setPreferredSize(table.getPreferredSize());
                    return pane;
                }//if

                if(value != null && value instanceof Image){
                    Image img = (Image)value;

                    JLabel label = new JLabel(new ImageIcon(img));

                    label.setMinimumSize(new Dimension(img.getWidth(null), img.getHeight(null)));

                    return label;
                }//if

                return null;
            }//getTreeCellRendererComponent
        });

        for(int i = 0; i < tree.getRowCount(); i++)
            tree.expandRow(i);

        return tree;
    }//createFancyTreeTable

    /**
     * Creates a table with the columns "votes" & "candidates".
     * Votes holds the number of votes a candidate received.
     * Candidates holds an image representing the candidate that was extracted from the raceImgMap.
     *
     * @param results - A map of race-ids to vote totals
     * @param raceImgMap - A map of race-ids to images
     * @return A fancy new JTable.
     */
    private JTable createFancyTable(final Map<String, BigInteger> results, final Map<String, Image> raceImgMap) {
        JTable fancyTable = new JTable();
        String max = "";
        String second = "";
        int sum = 0;
        for(String race: results.keySet()){
            sum += results.get(race).intValue();
            if(race.equals("B" + minRaceId(new ArrayList<String>(results.keySet())))){
                System.out.println(new ArrayList<String>(results.keySet()));
                System.out.println(race);
                continue;
            }
            if(max.equals("")){
                max = race;
            }else if(results.get(race).intValue() > results.get(max).intValue()){
                second = max;
                max = race;
            } else if(second.equals("") || results.get(race).intValue() > results.get(second).intValue()){
                second = race;
            }
        }

        final int totalVotes = sum;

        final Image winnerImg = raceImgMap.get(max);

        final double marginOfVictory = (results.get(max).intValue() - results.get(second).intValue())/(double)totalVotes;

        final DecimalFormat percentFormat = new DecimalFormat("0.000%");

        fancyTable.setModel(new DefaultTableModel(){
            Map.Entry[] entries = null;

            {
                entries = results.entrySet().toArray(new Map.Entry[0]);
                if(entries.length > 1){
                    Arrays.sort(entries, new Comparator<Map.Entry>(){

                        public int compare(Map.Entry arg0, Map.Entry arg1) {
                            if(arg0.getValue() == null && arg1.getValue() == null)
                                return 0;

                            if(arg0.getValue() == null)
                                return -1;

                            if(arg1.getValue() == null)
                                return 1;

                            return ((BigInteger)arg1.getValue()).compareTo((BigInteger)arg0.getValue());
                        }

                    });
                }//if
            }

            public int getColumnCount(){ return 4; }
            public int getRowCount(){ return results.keySet().size(); }

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
            }//getValueAt

            @Override
            public boolean isCellEditable(int row, int col){
                return false;
            }
        });

        TableColumn column = fancyTable.getColumnModel().getColumn(1);

        column.setCellRenderer(new DefaultTableCellRenderer(){
            @Override
            public void setValue(Object value){
                if(value instanceof Image){
                    setIcon(new ImageIcon((Image)value));
                    return;
                }//if

                super.setValue(value);
            }//setValue

            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                      Object value,
                                                      boolean isSelected,
                                                      boolean hasFocus,
                                                      int row,
                                                      int column) {
                if(value != null && value instanceof Image){
                    Image img = (Image)value;

                    JLabel label = new JLabel(new ImageIcon(img));

                    label.setMinimumSize(new Dimension(img.getWidth(null), img.getHeight(null)));

                    JPanel panel = new JPanel();
                    panel.setLayout(new GridBagLayout());
                    panel.add(label);
                    if(img == winnerImg) panel.setBackground(new Color(120,200,120)); else panel.setBackground(Color.white);
                    return panel;
                }else{
                    return null;
                }

            }
        });


        fancyTable.setRowHeight(getTallestImage(raceImgMap));
        column.setWidth(getWidestImage(raceImgMap));
        column.setMinWidth(getWidestImage(raceImgMap));

        column.setHeaderValue("Candidate");
        fancyTable.getColumnModel().getColumn(0).setHeaderValue("Candidate ID");
        fancyTable.getColumnModel().getColumn(0).setWidth(100);
        fancyTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer(){
            {setHorizontalAlignment(JLabel.CENTER);}
        });
        fancyTable.getColumnModel().getColumn(2).setHeaderValue("Votes Received");
        fancyTable.getColumnModel().getColumn(2).setWidth(175);
        fancyTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer(){
            {setHorizontalAlignment(JLabel.CENTER);}
        });
        fancyTable.getColumnModel().getColumn(3).setHeaderValue("Percentage");
        fancyTable.getColumnModel().getColumn(3).setWidth(100);
        fancyTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer(){
            {setHorizontalAlignment(JLabel.CENTER);}
        });

        fancyTable.setPreferredSize(new Dimension(fancyTable.getColumnModel().getColumn(1).getWidth() + 375,
                                                  fancyTable.getRowHeight()*fancyTable.getRowCount() + 20));
        fancyTable.setFont(new Font("Courier New", Font.BOLD, 20));

        return fancyTable;
    }

    //decided against this code because it clutters the UI. This converts the children of the root node of the tree into
    // single row columns with the race title, winner of the race, and margin of victory
    /*private JTable createTitleTable(Map<String, BigInteger> results, java.util.List<String> races, Map<String, Image> raceImgMap, final Image titleImg){
        JTable titleTable = new JTable();

        String max = races.get(0);
        String second = races.get(1);
        int totalVotes = 0;
        for(String race: races){
            if(results.get(race).intValue() > results.get(max).intValue()){
                second = max;
                max = race;
            } else if(results.get(race).intValue() > results.get(second).intValue()){
                second = race;
            }
            totalVotes += results.get(race).intValue();
        }

        final Image winnerImg = raceImgMap.get(max);

        final double marginOfVictory = (results.get(max).intValue() - results.get(second).intValue())/(double)totalVotes;

        titleTable.setModel( new DefaultTableModel(){

            public int getColumnCount(){return 3;}
            public int getRowCount(){return 1;}
            public boolean isCellEditable(int row, int col){return false;}
            public Object getValueAt(int row, int col){
                if(row != 0) throw new IndexOutOfBoundsException("This header table only has 1 row");
                switch(col){
                    case 0: return titleImg;
                    case 1: return winnerImg;
                    case 2: return "Margin of Victory: " + marginOfVictory;
                    default: throw new IndexOutOfBoundsException("Column index <col> out of bounds");
                }
            }
        });

        TableCellRenderer cellRenderer = new DefaultTableCellRenderer(){
            @Override
            public void setValue(Object value) {
                if (value instanceof Image) {
                    setIcon(new ImageIcon((Image) value));
                    return;
                }//if

                super.setValue(value);
            }//setValue

            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus,
                                                           int row,
                                                           int column){
                if (value != null && value instanceof Image) {
                    Image img = (Image) value;

                    JLabel label = new JLabel(new ImageIcon(img));

                    label.setMinimumSize(new Dimension(img.getWidth(null), img.getHeight(null)));

                    JPanel panel = new JPanel();
                    panel.setLayout(new GridBagLayout());
                    panel.add(label);
                    if (column == 1) panel.setBackground(new Color(150, 250, 150));
                    else panel.setBackground(Color.lightGray);
                    return panel;
                } else {
                    return null;
                }

            }
        };

        titleTable.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
        titleTable.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);

        titleTable.setRowHeight(titleImg.getHeight(null));

        titleTable.getColumnModel().getColumn(0).setHeaderValue("Race Title");
        titleTable.getColumnModel().getColumn(0).setWidth(titleImg.getWidth(null));

        titleTable.getColumnModel().getColumn(1).setHeaderValue("Race Winner");
        titleTable.getColumnModel().getColumn(1).setWidth(winnerImg.getWidth(null));

        titleTable.setPreferredSize(new Dimension(titleTable.getColumnModel().getColumn(0).getWidth() +
                                                  titleTable.getColumnModel().getColumn(1).getWidth() +
                                                  300,
                                                  titleTable.getRowHeight() + 20));

        titleTable.setFont(new Font("Courier New", Font.PLAIN, 14));

        return titleTable;
    }*/

    /**
     * @param images - a Map of images
     * @return the width of the widest image in images.
     */
    private int getWidestImage(Map<String, Image> images){
        int widest = -1;

        for(Image img : images.values()){
            if(img.getWidth(null) > widest)
                widest = img.getWidth(null);
        }//for

        return widest;
    }//getTallestImage

    /**
     * @param images - a Map of images
     * @return the height of the tallest image in images.
     */
    private int getTallestImage(Map<String, Image> images){
        int tallest = -1;

        for(Image img : images.values()){
            if(img.getHeight(null) > tallest)
                tallest = img.getHeight(null);
        }//for

        return tallest;
    }//getTallestImage

    /**
     * Taking in a ballot location, tries to load all relevant images into a map of race-ids to Images.
     *
     * @param ballot - The ballot file to read
     * @param languages - The list of languages on the ballot
     * @return a map of race-ids to images, or null if an error was encountered.
     */
    private Map<String, Image> loadBallotRaces(String ballot, java.util.List<String> languages) {
        try {
            Map<String, Image> racesToImageMap = new HashMap<String, Image>();

            ZipFile file = new ZipFile(ballot);

            Enumeration<? extends ZipEntry> entries = file.entries();

            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                if(isRaceImage(entry.getName(), languages)){
                    racesToImageMap.put(getRace(entry.getName()), ImageIO.read(file.getInputStream(entry)));
                }//if
            }//while

            return racesToImageMap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param entryName - the Zip entry to consider
     * @param langs - the list of languages to pull the results from
     * @return true if entryName is in the form "media_B*_selected_*.png", ie if it is a "race image"
     */
    private boolean isRaceImage(String entryName, java.util.List<String> langs){
        if(!entryName.startsWith("media/vvpat/B"))
            return false;

        if(!entryName.endsWith(".png"))
            return false;

        if(entryName.indexOf("_selected_") == -1)
            return false;
        if (langs != null)
            if(entryName.indexOf(langs.get(0)) == -1) //grab the first listed language for now
                return false;

        return true;
    }//isRaceImage

    /**
     * Extracts a race-id from a zip entry of a race image.
     *
     * @param name - the entry of the race image.
     * @return A string in the form B*, that is a valid race id
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
     * @param results - A map of race ids to vote totals.
     * @return A basic JTable to display
     */
    private JTable createBasicTable(final Map<String, BigInteger> results) {
        TableModel model = new DefaultTableModel(){
            public int getColumnCount(){ return 2; }
            public int getRowCount(){ return results.keySet().size(); }
            public String getColumnName(int column){
                switch(column){
                    case 0: return "Candidate";
                    case 1: return "Votes";
                    default: throw new RuntimeException(column +" >= 2 column name requested.");
                }
            }//getColumnName

            public Object getValueAt(int row, int col){
                Map.Entry entry = results.entrySet().toArray(new Map.Entry[0])[row];

                switch(col){
                    case 0: return entry.getKey();
                    case 1: return entry.getValue();
                    default: throw new RuntimeException(col + " >= 2 column value requested.");
                }
            }//getValueAt
        };

        return new JTable(model);
    }

    /**
     * Determines the raceID with the minimum value number appended to "B"
     * example min(["B1", "B2", "B3"]) = 1
     *
     * @param s List of RaceIDs of form "B" + some int
     * @return integer value of least appended integer
     */

    private int minRaceId(ArrayList<String> s){
        try{
            int min = Integer.parseInt(s.get(0).substring(1));
            for(int i=1; i<s.size(); i++){
                if(Integer.parseInt(s.get(i).substring(1)) < min){
                    min = Integer.parseInt(s.get(i).substring(1));
                }
            }
            return min;
        }catch(NumberFormatException ex){
            System.out.println("Cannot Order Races");
            return 0;
        }
    }

    /**
     * Use for testing interface as stand alone.
     *
     * @param args command line args
     */

    public static void main(String[] args){
        Map<String, BigInteger> resMap = new TreeMap<String, BigInteger>();
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
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
