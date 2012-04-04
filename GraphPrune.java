package heatdiffusion;

import java.io.*;
import java.util.regex.*;
import it.unimi.dsi.fastutil.ints.IntArrays;
import java.util.Arrays;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.ImmutableSubgraph;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unipi.di.graph.GraphLabeler;

class GraphPrune {
    
    /* Data files */
    protected String graphFile = "original/webgraph/wg-aol";
    protected String labelerFile = "original/graphlabeler/graphlabeler.conf";
    
    /* Webgraph / Graphlabeler */
    protected ImmutableGraph graph;
    protected GraphLabeler labeler;
    
    /* Sql */
    protected Connection con;
    protected Statement stmt;   
    
    /* Qery nodes [0 - 4811649] */
    protected int totalQueryNode = 4811650;
    
    /* Url nodes */
    protected int urlStartNode = 4811650;
    protected int urlEndNode = 6444437;
    protected int totalUrlNode = 1632788;
    
    /* File streams */
    protected BufferedWriter q;
    protected BufferedWriter url;
    protected BufferedWriter click;
    protected DataOutputStream out;
    
    /* Test counters */
    protected int totCounter = 0;
    protected int outCounter = 0;
    protected int headerNumNodes = 3166404;
    
    /* To create a nodes that will
     * form subgraph after pruning 
     */
    protected int[] subgraphNodes = new int[3166404];
    protected int subgraphCounter = 0;

    
    public GraphPrune()throws Exception {
        
        /* Load Webgraph / Graphlabeler files */
        graph = ImmutableGraph.load(graphFile);
        labeler = GraphLabeler.load(labelerFile);
        
        /* Output stream for webgraph file */
        out = new DataOutputStream(
                     new FileOutputStream( "intermediate/prunedAol.txt"  ) );
        
        /* Initial values set as -1 */
        IntArrays.fill( subgraphNodes, -1 );

        /* Write total number of nodes in first line 
         * out.writeInt(2002206);
         * out.writeInt(927214);
         * out.writeInt(3166404);
         */  
        out.writeInt(headerNumNodes);
        
        /* File streams for graphlabeler files
         * 
         * q => query.txt => query.tdb , stores the query labels
         * url => url.txt => url.tdb , stores the url labels
         * click => weight.txt => weight.tdb , stores the click edge attribute
         * It stores undirected values and has not been normalized between 0-1                 
         */
        q = new BufferedWriter(new FileWriter("intermediate/query.txt"));
        url = new BufferedWriter(new FileWriter("intermediate/url.txt"));
        click = new BufferedWriter(new FileWriter("intermediate/weight.txt"));

        /* Create Mysql Connection */
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection ("jdbc:mysql://localhost/aol?"+
                                                    "user=root&password=reason");
            stmt = con.createStatement();
         }

        catch(ClassNotFoundException e)
        {
            System.out.println(e);
        }

        catch(SQLException e) 
        {
            System.out.println(e);
        }
        
    }
    
    public void graphQueryTraverse() throws Exception
    {
        
        System.out.println("<!---------- Pruning Query Node ------------!>");
        
        /* Counter for the while loop */
        int counter = 0;
        
        /* Counter for number of dirty nodes */
        int dirtyNodes = 0;
        
        /* Percentage completed */
        double perc = 0;
        
        /* Node iterator to traverse through graph */
        NodeIterator iter = graph.nodeIterator();
        
        while(iter.hasNext() && counter >= 0 && counter < totalQueryNode) {
    
            int node = iter.next();
            String label = labeler.getVertexLabel(counter);
            
            /* Prints percentage completed */
            perc = printPerc(node,totalQueryNode-1,perc);
 
            boolean d = checkQueryLabel(label);
          
            /*dirty */
            if(!d)
            { 
                ++dirtyNodes;
                pruneQueryNode(node, iter); 
            }  
           
           /*not dirty */
            else
            {
                /* check session for node */
                boolean s = checkQuerySession(node);
                
                if(!s)
                {
                    ++dirtyNodes;
                    pruneQueryNode(node, iter);
                }
                
                else
                {
                    ++totCounter;
                    
                    /* write node to subgraph array */
                    subgraphNodes[subgraphCounter++] = node; 
                    
                    /* Write click label file */
                    String[] clickLabel = labeler.getOutgoingAttribute(node, "click");
                    int len = 0;
                    while(len < clickLabel.length)
                    {
                        if(len != 0)
                        {
                            click.write("\t");
                        }
                        click.write(clickLabel[len]);
                        len++;
                    }
                    click.newLine();
                    
                    /* Write query label file */
                    String queryLabel = labeler.getVertexLabel(node);
                    q.write(queryLabel);
                    if(counter != 4811649)
                    q.newLine();
                }
            }       
                
            ++counter;
            
        } // End of while
        
        q.close();
        
        System.out.println("\nTotal Number of query nodes processed : "+ counter);
        System.out.println("\nTotal number of dirty query nodes : "+dirtyNodes);
        System.out.println("\nTotal number of new query nodes : "+(totalQueryNode -dirtyNodes));
        
    }
    
    /* Only query's that contain complete words with space are allowed */
    private boolean checkQueryLabel(String label) throws IOException {

        Pattern p = Pattern.compile("^[a-z ]+$");
        Matcher m = p.matcher(label);
        boolean b = m.matches();
        return b;
    }
    
    /* Checks if a query has been typed a minimum of two times */
    private boolean checkQuerySession(int node) throws Exception
    {
        int sum = 0;
        int index = -1;
        int count = 0;
        
        String[] values = labeler.getOutgoingAttribute(node, "session");
         
        if(values.length >= 2)
            return true;
        
        while(count < values.length)
        {
            do
            {
                ++sum;
                index = values[count].indexOf("::",index+1);
                
            }   while(index != -1);
            
            if(sum >= 2)
                return true;
            
            ++count;
        }
        return false;
    }
    
    public void pruneQueryNode(int node,NodeIterator iter) throws Exception
    {

        int queryOutDegree;
        String query;    
        int count = 0;
        
        /*get the adjacency list of the node */
        int[] Qsucc = iter.successorArray();
        queryOutDegree = iter.outdegree();
         
        while(count < queryOutDegree)
        {
            query = "INSERT INTO prunedata VALUES ("+ Qsucc[count] +"," + node + ")";
            try {
                stmt.execute(query);
            }
            catch(SQLException e) {
               e.printStackTrace();
            }
            
            ++count;
        }

    }
       
    public void writeQueryNode() throws Exception
    {
        System.out.println("\n<!---------- fill subgraph array ------------!>");
        
        int counter = urlStartNode; int dirtyNodes = 0; double perc = 0;
        int urlNodeChange = 0;
        ResultSet rs;
        
        NodeIterator iter = graph.nodeIterator(urlStartNode);
        
        while(iter.hasNext() && counter >= urlStartNode && counter <= urlEndNode) {
            
           int node = iter.next();
           
           perc = printPerc(node-urlStartNode,urlEndNode-urlStartNode,perc);
           
           String query = "Select urlNode,queryNode from prunedata where urlNode = "+ node;
            try {
                rs = stmt.executeQuery(query);
                urlNodeChange = checkUrlNode(rs);
                rs.beforeFirst();
                
                if(urlNodeChange > 0)
                {
                    int urlOutDegree = iter.outdegree();
                    int newUrlOutDegree = urlOutDegree - urlNodeChange;
        
                    if(newUrlOutDegree > 0)
                        subgraphNodes[subgraphCounter++] = node;
                }
                
                else
                {
                     /* write node to subgraph array */
                    subgraphNodes[subgraphCounter++] = node;
                }
            }
            catch(SQLException e) {
               e.printStackTrace();
            }
            
            ++counter;
        }
        
        try {
            if(subgraphCounter != 3166404 )
                throw new GraphIOException("the subgraph array list does not match : "+subgraphCounter+" != "+3166404);
        }
        
        catch(Exception e)
        {
            System.out.println(e);
        }
        
        /* write query node */
        System.out.println("\n<!---------- Write Query Node ------------!>");
        iter = graph.nodeIterator(0);
        counter = 0;
        perc = 0; 
        
        while(iter.hasNext() && counter >= 0 && counter < totalQueryNode) 
        {
            int node = iter.next();
            perc = printPerc(node,totalQueryNode-1,perc);
            
            int index = Arrays.binarySearch(subgraphNodes, node);
            
            if(index >= 0)
            {
                int outdegree = iter.outdegree();
                out.writeInt(outdegree);
             
                int[] succ = iter.successorArray();
                for(int i=0;i<outdegree;i++)
                {
                    int in = Arrays.binarySearch(subgraphNodes,succ[i]);
                    out.writeInt(in);
                }
            }
        
            ++counter;
        }
     
    }
       
    public void graphUrlTraverse() throws Exception
    {
        
        System.out.println("\n<!---------- Prune Url Node ------------!>");
        
        int counter = urlStartNode; int dirtyNodes = 0; double perc = 0;
        int urlNodeChange = 0;
        ResultSet rs;
        
        NodeIterator iter = graph.nodeIterator(urlStartNode);
        
        while(iter.hasNext() && counter >= urlStartNode && counter <= urlEndNode) {
            
           int node = iter.next();
           //System.out.println(node);
           perc = printPerc(node-urlStartNode,urlEndNode-urlStartNode,perc);
           
           String query = "Select urlNode,queryNode from prunedata where urlNode = "+ node;
            try {
                rs = stmt.executeQuery(query);
                urlNodeChange = checkUrlNode(rs);
                rs.beforeFirst();
                
                if(urlNodeChange > 0)
                {
                    int urlOutDegree = iter.outdegree();
                    int newUrlOutDegree = urlOutDegree - urlNodeChange;
        
                    if(newUrlOutDegree > 0)
                        pruneUrlNode(node,iter,newUrlOutDegree,urlOutDegree,rs);
                    
                    else
                        ++dirtyNodes;
                }
                else
                {
                    ++totCounter;
                    int outdegree = iter.outdegree();
                    out.writeInt(outdegree);
                    int[] succ = iter.successorArray();
                    for(int i=0;i<outdegree;i++)
                    {
                       int index = Arrays.binarySearch(subgraphNodes, succ[i]);
                        out.writeInt(index);
                    }
                                        
                    /* Write to url labeler */
                    String urlLabel = labeler.getVertexLabel(node);
                    url.write(urlLabel);
                    if(counter != urlEndNode)
                    url.newLine();
                    
                    /* Write to click label */
                    String[] clickLabel = labeler.getOutgoingAttribute(node, "click");
                    int len = 0;
                    while(len < clickLabel.length)
                    {
                        if(len != 0)
                        {
                            click.write("\t");
                        }                        
                        click.write(clickLabel[len]);
                        len++;
                    }
                    if(counter != urlEndNode)
                    click.newLine();
                }
            }
            catch(SQLException e) {
               e.printStackTrace();
            }
            
            ++counter;

        }
        
       url.close();
       out.close();
       click.close();
       
        try
        {
            if(totCounter != headerNumNodes)
                throw new GraphIOException("\nTotal number of nodes does not match\n"+totCounter+" != "+headerNumNodes); 
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        
        System.out.println("\nTotal Number of url nodes processed : "+((counter-1)-(urlStartNode)+1));
        System.out.println("\nTotal number of dirty url nodes : "+dirtyNodes);
        System.out.println("\nTotal number of new url nodes : "+((urlEndNode-urlStartNode+1)-dirtyNodes));
        
    }
  
    private int checkUrlNode(ResultSet rs) {
        
        int count = 0;
        try {
        while(rs.next())
            {
                ++count;
            }
        }  
        catch(SQLException e) {
               e.printStackTrace();
        }
        return count;
    }
   
    public void pruneUrlNode(int node,NodeIterator iter,int newUrlOutDegree,int urlOutDegree,ResultSet rs) throws Exception
    {
        outCounter = 0 ;
        ++totCounter;
        
        /* write node to subgraph array */
        //subgraphNodes[subgraphCounter++] = node;
        
        int count = 0; 
        int Usucc[] = iter.successorArray();
        out.writeInt(newUrlOutDegree);
        
        String[] clickLabel = labeler.getOutgoingAttribute(node, "click");

        while(count < urlOutDegree)
        {
//            rs.beforeFirst();
            if(!isSame(Usucc[count],rs))
            {
                int index = Arrays.binarySearch(subgraphNodes, Usucc[count]);
                out.writeInt(index);
                //out.writeInt(Usucc[count]);
                if(outCounter != 0)
                {
                    click.write("\t");
                }                
                click.write(clickLabel[count]);
                ++outCounter;
            }
            ++count;
        }
        
        if(totCounter != headerNumNodes )
        click.newLine();
        
        try
        {
            if(outCounter != newUrlOutDegree)
                throw new GraphIOException("New url outdegree does not match\n"+outCounter+" != "+newUrlOutDegree); 
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        
        
        /* Write into url label file */
        String urlLabel = labeler.getVertexLabel(node);
        url.write(urlLabel);
        if(totCounter != headerNumNodes )
        url.newLine();
        
    }
    
    public boolean isSame(int node, ResultSet rs) throws Exception
    {
        
        rs.beforeFirst();
        try {
            while(rs.next())
            {
                if(node == rs.getInt(2))
                {
  //                  rs.beforeFirst();
                    return true;
                }
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
   //     rs.beforeFirst();
        return false;
    }
    
   private static double printPerc(int node, int numNodes, double percLimit) {
	
        int perc = (int)(node / (double)numNodes * 100);
	
        if (perc >= percLimit || node == 0) {
            if (node == 0) System.out.print("\n >> Progress: ");
            System.out.print(perc + "% ");
            percLimit += 5;
	}
        
		
	return percLimit;
    }
   
   public void comparegraph() throws Exception
   {
       ImmutableSubgraph g = new ImmutableSubgraph(graph,subgraphNodes);
       g.save("subgraph");
       ImmutableGraph s = ImmutableGraph.load("intermediate/webgraph/aol");
       System.out.println(g.equals(s));
       
   }
   
   public void testClickData() throws Exception
   {
       ImmutableGraph s = ImmutableGraph.load("intermediate/webgraph/aol");
       GraphLabeler l = GraphLabeler.load("intermediate/graphlabeler/graphlabeler.conf");
       
       NodeIterator iter = s.nodeIterator();
        
        while(iter.hasNext() ) {
    
            int node = iter.next();
            String[] label = l.getOutgoingAttribute(node, "click");
            int outt = s.outdegree(node);
            if(outt != label.length)
                System.out.println("Error");
        }
       
   }
   
}

/* Exception Class */
class GraphIOException extends Exception
{
  String mistake;

// Default constructor - initializes instance variable to unknown

  public GraphIOException()
  {
    super();             // call superclass constructor
    mistake = "unknown";
  }
  
// Constructor receives some kind of message that is saved in an instance variable.

  public GraphIOException(String err)
  {
    super(err);     // call super class constructor
    mistake = err;  // save message
  }
   
// public method, callable by exception catcher. It returns the error message.

  public String getError()
  {
    return mistake;
  }
}
  
