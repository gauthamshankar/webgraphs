
package heatdiffusion;

import java.io.*;
import it.unipi.di.graph.GraphLabeler;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;

public class DirectedGraph {
    
    protected GraphLabeler labeler;
    protected ImmutableGraph graph;
    
    protected String labelerFile = "intermediate/graphlabeler/graphlabeler.conf";
    protected String graphFile = "intermediate/webgraph/aol";
    
    protected BufferedWriter click;
    
    protected int numNodes;
    
    public DirectedGraph() throws Exception
    {
        graph = ImmutableGraph.load(graphFile);
        labeler = GraphLabeler.load(labelerFile);
        click = new BufferedWriter(new FileWriter("intermediate/click.txt"));
    }
    
    public void convertToDirectedGraph() throws Exception
    {
        numNodes = graph.numNodes();
        int node = 0;
        int sum = 0;
        double perc = 0;

        
        while(node < numNodes)
        {
            perc = printPerc(node,numNodes-1,perc);
            String[] edgeLabel = labeler.getOutgoingAttribute(node, "click");
            
            for(int i=0; i < edgeLabel.length; i++)
            {
                sum = sum + Integer.parseInt(edgeLabel[i]);
            }
//            System.out.println(sum);
            
            for(int i=0; i < edgeLabel.length; i++)
            {
                if(i != 0)
                {
                    click.write("\t");
                }
                float newEdgeLabel = (float)Integer.parseInt(edgeLabel[i])/sum;
//                System.out.println(newEdgeLabel);
                click.write(String.valueOf(newEdgeLabel));
            }
            if(node+1 != numNodes);
            click.newLine();
            sum = 0;
            ++node;
        }
        
        click.close();
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
    
}
