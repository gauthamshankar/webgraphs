package heatdiffusion;

import java.io.*;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.examples.IntegerListImmutableGraph;
import it.unimi.dsi.webgraph.BVGraph;

public class WebGraphBin {
    
    protected static String graphFileName;
    
    protected ImmutableGraph graph;
    
    protected int numNodes;
    
//    public WebGraphBin(String file) throws Exception
//    {
//        graphFileName = file;
//        graph = ImmutableGraph.load(graphFileName);
//        numNodes = graph.numNodes();
//    }
//    
    public void makeBinaryFileFromWebGraph(String graphfile,String binfile) throws Exception
    {
        String binaryFileName = binfile;
        
        graphFileName = graphfile;
        graph = ImmutableGraph.load(graphFileName);
        numNodes = graph.numNodes();
        //int counter = 0;
        
        try
        {
            DataOutputStream out = new DataOutputStream(
                     new FileOutputStream( binaryFileName  ) );
           
            /* First Write the Total Number Of Nodes */
            out.writeInt((numNodes));
            
            int node = 0;
            
            while( node < numNodes)
            {
                NodeIterator iter = graph.nodeIterator(node);
                iter.next();
                
                int outdegree = iter.outdegree();
                
                /* Write the outdegree of node */
                out.writeInt( outdegree);
                
                /* Get the successor list of node and write them into file */
                int[] succ = iter.successorArray();
                
                for(int i=0;i<outdegree;i++)
                {
                    out.writeInt(succ[i]);
                }
                
                ++node;
            }
                
                out.close();
        }
        
        catch(IOException e)
        {
            System.out.println(e + "\n File " + binaryFileName + " could not be created");
        }
 
    }
    
    public void createWebGraphFromBinaryFile(String binFile, String wgFile) throws Exception
    {
        String binaryFileName = binFile;
        String WebGraphFileName = wgFile;
       
        ImmutableGraph WebGraph = IntegerListImmutableGraph.loadOffline(binaryFileName);
     
        try
        {
            BVGraph.store(WebGraph,WebGraphFileName);
        }
        
        catch(Exception e)
        {
            e.printStackTrace();
        }
     
    }

}
