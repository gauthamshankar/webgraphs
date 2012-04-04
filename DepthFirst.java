
package heatdiffusion;

import java.io.*;

import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.ImmutableSubgraph;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unipi.di.graph.GraphLabeler;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.GraphClassParser;
import it.unimi.dsi.fastutil.ints.IntArrays;
import java.util.Stack;
import java.util.Arrays;
import java.util.ArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;


public class DepthFirst {
    
    protected String graphFile = "intermediate/webgraph/aol";
    protected String labelerFile = "intermediate/graphlabeler/graphlabeler.conf";
    
    protected ImmutableGraph graph;
    protected ImmutableSubgraph g;
    protected GraphLabeler labeler;
    
    protected ArrayList subgraphNodes = new ArrayList() ;
//    protected ArrayList fromVertexhNodes = new ArrayList() ;
    protected int sgArrayCount = 0;
    protected int sgNodeCounter = 0;
    protected int sgQueryNodeCounter = -1;
    
//    protected int[] vertex1 = new int[2000];
//    protected int[] vertex2 = new int[2000];
    
    protected String query;

    protected int start;
    protected int maxDist ;
    protected int n ;
    protected int[] dist;
    protected int lo;
    protected int hi;
    protected boolean print = false;
    //protected Stack stack;
    protected final IntArrayFIFOQueue queue = new IntArrayFIFOQueue();
//    protected final IntArrayFIFOQueue vertexQueue = new IntArrayFIFOQueue();
   
    
    public DepthFirst(String q) throws Exception
    {
        /* load graph files */
        graph = ImmutableGraph.load(graphFile);
        labeler = GraphLabeler.load(labelerFile);
 
        /* Set depth first parameters */
        
        //start = 13560; // start node
        query = q;
        maxDist = 3000000; // distance to traverse
        n = graph.numNodes(); // total number of nodes
        
	dist = new int[ n ]; // distance array
        IntArrays.fill( dist, Integer.MAX_VALUE ); // Initially, all distances are infinity.
        

   
        
       // stack = new Stack();        
    }
    
    
    public void findNode() throws Exception
    {
        int i;
        String[] values = labeler.getVertexLabels(0, n-1);
        int index = Arrays.binarySearch(values, query);
        if(index >= 0)
            System.out.println("Query found in index : "+index);
        else
            System.out.println("Query not found");

        start = index;
        lo = start == -1 ? 0 : start;
	hi = start == -1 ? n : start + 1;
    }
    
    public int depthFirstSearch() throws Exception
    {
        int curr = lo, succ, ecc = 0, reachable = 0;
        
        for( int i = lo; i < hi; i++ )
        {
            if ( dist[ i ] == Integer.MAX_VALUE )
            {   
                /* for the start node */
                queue.enqueue(i);
//                vertexQueue.enqueue(i);
                //stack.push(new Integer(i));
                if ( print ) System.out.println( i );
                dist[ i ] = 0;

                while( ! queue.isEmpty() )
                {
                    curr = queue.dequeueInt();
//                    int ver = vertexQueue.dequeueInt();
//                    System.out.println(ver);
//                    vertex1[sgNodeCounter] = ver;
//                    vertex2[sgNodeCounter] = curr;

                    
                    //curr = (Integer) stack.pop();
                    subgraphNodes.add(new Integer(curr));
                    
                    /* count the number of query nodes */
                    if(curr < 2002206)
                    {
                        ++sgQueryNodeCounter;

                    }
                    ++sgNodeCounter;
                    int[] successors = graph.successorArray(curr);
                    int d = graph.outdegree( curr );

                    while( d-- != 0 )
                    {
                        succ = successors[d];

                        if ( dist[ succ ] == Integer.MAX_VALUE && dist[ curr ] + 1 <= maxDist )
                        {
                            reachable++;
                            dist[ succ ] = dist[ curr ] + 1;
                            ecc = Math.max( ecc, dist[ succ ] );
                            queue.enqueue(succ);
//                            vertexQueue.enqueue(curr);
                            //stack.push(new Integer(succ));
                            if ( print ) System.out.println( succ );
                        }
                        
                    } // end of while( d-- != 0)

                    /* checks if there are 5000 query nodes */
                    if(sgNodeCounter == 2000)
                    {
                        queue.clear();
                        //stack.clear();
                    }
                    
                } // end of while(!stack.empty())

            } // end of if

            if(sgNodeCounter == 2000)
            {
                break;
            }
   
	} // end of for
        
        /* for a complete depth first traverse
         * check if all the nodes have been traversed
         */
        int check =0;
        for(int j =0; j < n ; j++)
        {
            if(dist[ j ] != Integer.MAX_VALUE)
                ++check;
        }
        
        System.out.println("No of nodes processed : "+check);

        if ( !print )
                if ( start == -1 ) System.out.println( "The maximum depth of a tree in the depth-first spanning forest is " + ecc );
                else {
                        System.out.println( "The eccentricity of node " + start + " is " + ecc + " (" + reachable + " reachable nodes)" );
                }
        int[] sg = new int[subgraphNodes.size()];

        for (int i = 0; i < sg.length; i++)
        {
           sg[i] = (Integer) subgraphNodes.get(i);
        }

        Arrays.sort(sg);

        subgraph(sg);
        return sgQueryNodeCounter;
    }
    
    public void subgraph(int[] subgraph) throws Exception
    {
        
        ImmutableSubgraph g = new ImmutableSubgraph(graph,subgraph);
        g.save("query/"+query);
        System.out.println(subgraph.length);
        
        BufferedWriter q = new BufferedWriter(new FileWriter("query/query.txt"));
        BufferedWriter url = new BufferedWriter(new FileWriter("query/url.txt"));
        BufferedWriter click = new BufferedWriter(new FileWriter("query/click.txt"));
        
        int totalSubgraphNode = subgraphNodes.size();
        int counter = 0;
        NodeIterator iter = g.nodeIterator();
        while(iter.hasNext() && counter >= 0 && counter < totalSubgraphNode) 
        {
//            System.out.println("In WHILE");
//            System.out.println(counter);
            int node = iter.next();
            //System.out.println(node);
            
            int[] succ = g.successorArray(node);
            int s = g.outdegree(node);
            for(int i=0; i< s; i++)
            {
                succ[i] = g.toRootNode(succ[i]);
              //  System.out.print(succ[i]+" ");
            }
            //System.out.println("\n");
 
            int rnode = g.toRootNode(node);
            //System.out.println(rnode +" "+labeler.getVertexLabel(rnode));
            if(rnode <= 2002205)
            {
                q.write(labeler.getVertexLabel(rnode));
                q.write("\n");
            }
            else
            {
                url.write(labeler.getVertexLabel(rnode));
                url.write("\n");
            }
            
            String[] clickval = labeler.getOutgoingAttribute(rnode, "click");
            int[] outval = graph.successorArray(rnode);
            int c = graph.outdegree(rnode);
            
//            int[] vertex2val = new int[2000];
//            int p = -1;
//            System.out.println("IN2");
//            
//            for(int i=0; i < 2000; i++)
//            {
//                if(vertex1[i] == rnode)
//                {
//                    System.out.println("IN");
//                    vertex2val[++p] = vertex2[i];
//                    System.out.println(vertex2val[p]);
//                }
//            }
            
//            for(int i =0; i<c; i++)
//                System.out.print(outval[i]+" ");
//            System.out.println("\n\n");
            
            if(clickval.length != c)
            {
                System.out.println("Error clickval and outval :"+clickval.length+" "+c);
                System.out.println("Node num : "+rnode);         
            }
            
            int check = 0;
            int k=0,flag=0;
            for(int i=0; i < s; i++)
            {
                for(k=0;k<c;k++)
                {
                    if(succ[i] == outval[k])
                    {
//                        int h = Arrays.binarySearch(vertex2val, outval[k]);
                       // System.out.println(h);
//                        if(h >= 0)
//                        {
//                            flag = 1;
//                            break;   
//                        }
//                        else
//                        {
                            flag = 1;
                            break;
//                        }
                    }
                }
                
                if(flag == 1)
                {
                    //System.out.println(k);
                    flag = 0;
                    ++check;
                    if(check != 1)
                        click.write("\t");
                    
                    click.write(clickval[k]);
                }
                
//                if(flag == 2)
//                {
//                    //System.out.println(k);
//                    flag = 0;
//                    ++check;
//                    if(check != 1)
//                        click.write("\t");
//                    
//                    click.write("0");
//                }
            }
            ++counter;
            click.write("\n");
            if(check != s)
                System.out.println("Error check and succ :"+check+" "+s);
            
            check=0;
            
        }
        
        if(counter != g.numNodes())
            System.out.println("Error in counter and g"+counter+" "+g.numNodes());
        
        q.close();
        url.close();
        click.close();
    }

}