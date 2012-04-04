package heatdiffusion;

import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.ImmutableSubgraph;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unipi.di.graph.GraphLabeler;
import java.util.ArrayList;
import java.io.*;
import java.util.Arrays;
import java.util.AbstractList;
import java.util.*;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;
import it.unipi.di.util.Utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HeatDiffusion {
    
    protected final int P = 10;
    protected final int a = 1;
    
    protected int numNodes;
    protected String q;
    
    protected Connection con;
    protected Statement stmt;

    protected String subgraphFile = "query/java";
    protected String sublabelerFile = "query/subgraph.conf";
    
    protected String graphFile = "intermediate/webgraph/aol";
    protected String labelerFile = "intermediate/graphlabeler/graphlabeler.conf";
    
    protected ImmutableGraph g;
    protected GraphLabeler l;
    
    protected ImmutableGraph graph;
    protected GraphLabeler labeler;
    
    public HeatDiffusion(String u) throws Exception
    {
        g = ImmutableSubgraph.load("query/"+u);
        l = GraphLabeler.load(sublabelerFile);
        
        graph = ImmutableGraph.load(graphFile);
        labeler = GraphLabeler.load(labelerFile);
        numNodes = g.numNodes();
        q = u;   
        
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
    public static void main(String args[]) throws Exception
    {
        HeatDiffusion hd = new HeatDiffusion("java");
//        hd.coltTest();
        hd.mulTest();
    }
    
    public void mulTest() throws Exception
    {
        DenseDoubleMatrix2D f = new DenseDoubleMatrix2D(2000,2000);
        f.assign(9.99999999999999999);
        System.out.println(f.cardinality());
        DenseDoubleMatrix2D g = new DenseDoubleMatrix2D(2000,2000);
        g.assign(9.99999999999999999);
        System.out.println(g.cardinality());
        f.zMult(g, null, a, a, false, false);
    }
    
    public void coltTest() throws Exception
    {
        
        int check =0 ,counter =-1;
        /* f(0) array */
        String[] label = l.getVertexLabels(0, 1999);
        int index = Arrays.binarySearch(label, q);
        DenseDoubleMatrix2D f0 = new DenseDoubleMatrix2D(numNodes,1);
        f0.assign((double)0);
        f0.setQuick(index,0, (double)1);
        
        /* H array */
        DenseDoubleMatrix2D H = new DenseDoubleMatrix2D(numNodes,numNodes);
        H.assign((double)0);
        
        for(int i=0; i < numNodes; i++)
        {
            int iout = g.outdegree(i);
            int[] isucc = g.successorArray(i);
            
//            System.out.println("out edges of "+ i +" :");
//            for(int x=0; x<iout; x++)
//                System.out.println(isucc[x]);
            
            for(int j=0; j < numNodes; j++)
            {
                
                if(i == j)
                {
                    continue;
                }
                else
                {
                    int flag =0;
                    for(int k=0; k<iout; k++) {
                        if(isucc[k] == j)
                        {
//                            System.out.println(isucc[k]);
                            flag = 1;
                            break;
                        }
                    }
                    if(flag != 1)
                    {
                        continue;
                    }
                    flag=0;
                    ++check;
                    ++counter;

                    int jsucc[] = g.successorArray(j);
                    int jout = g.outdegree(j);
//                    System.out.println(l.getVertexLabel(j)+" "+jout);
                    
//                    System.out.println("out edges of "+ j +" :");
//                    for(int x=0; x<jout; x++)
//                        System.out.println(jsucc[x]);

                    
                    String[] click = l.getOutgoingAttribute(j, "click");
//                    String test = l.getVertexLabel(j);
//                    System.out.println(click.length);
                    /*test*/
//                    String[] testLabel = labeler.getVertexLabels(0, graph.numNodes()-1);
//                    int fuck = Arrays.binarySearch(testLabel,test);
//                    System.out.println(fuck);
//                    System.out.println(labeler.getVertexLabel(3144725));
//                    System.out.println(graph.outdegree(3144725));
                    
                    
                    
                    flag = -1;
                    for(int k=0; k<jout; k++) {
                        if(jsucc[k] == i)
                        {
//                            System.out.println(jsucc[k]);
                            flag = k;
                            break;
                        }
                    }
                    
                    if(flag == -1)
                        System.out.println("Flag Error");
                    
                    double Wji = Double.parseDouble(click[flag]);
                    
                    double Wjk = (double)0;
                    for(int k=0; k < jout; k++)
                        Wjk = Wjk + Double.parseDouble(click[k]);
 
                    double val1 = (double) (Wji / Wjk);
                    H.setQuick(i,j,val1);
                }
            }
            
            if(check != iout)
                System.out.println("Error : "+check+" "+iout);
            check = 0;
        }
        System.out.println(counter);
        System.out.println(H.cardinality());
        
        /* Take transpose of H matrix */
        Algebra s =  new Algebra();
     DoubleMatrix2D y = s.transpose(H);
        H.assign(y);
        
        /* Create D, H = H - D */
        System.out.println("Create D, H = H - D");
//        DenseDoubleMatrix2D D = new DenseDoubleMatrix2D(numNodes,numNodes);
        for(int i=0; i<g.numNodes(); i++)
        {
            int out = g.outdegree(i);
            
            if(out > 0)
            {
//                String[] outEdges = l.getOutgoingAttribute(i, "click");
//                double sum =0;
//                for(int t=0; t<out;t++)
//                    sum = (double)(sum + Double.parseDouble(outEdges[t]));
//                if(sum > 0)
//                {
                    double val = H.getQuick(i, i);
                    val = (double)(val - 1);
                    H.setQuick(i, i, val);
//                }
            }
            
        }
        System.out.println(H.cardinality());

//        
        /* (a/P * R) + I */
        System.out.println("(a/P * R) + I");
        for(int i=0; i<g.numNodes(); i++)
        {
            for(int j=0; j<g.numNodes(); j++)
            {
                double val = H.getQuick(i,j);
                val = (double)((val/10));
                if(i == j)
                    val = (double) (val + 1);
                H.setQuick(i, j, val);
            }
        }
        System.out.println(H.cardinality());

        
//
        System.out.println("mul");
        DenseDoubleMatrix2D C = new DenseDoubleMatrix2D(numNodes,numNodes);
        C.assign(0);
        
        long start = System.currentTimeMillis();
        
        //1
        System.out.println("1");
        H.zMult(H,C, a, a, false, false);

        
        System.out.println("Assigning H and trimming");
        
        H.assign(C);
//        H.trimToSize();
        System.out.println(H.cardinality());
        System.out.println("\ndone in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
        start = System.currentTimeMillis();
        
//        DoubleMatrix1D ans = H.viewRow(0);
//        System.out.println(ans.cardinality());
//        
        
        
        //2
//        System.out.println("2");
//        H.zMult(H,C, a, a, false, false);
//
//        
//        System.out.println("Assigning H and trimming");
//        
//        H.assign(C);
////        H.trimToSize();
//        System.out.println(H.cardinality());
//        System.out.println("\ndone in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
//        start = System.currentTimeMillis();
//        
//        
//        //3
//        System.out.println("3");
//        H.zMult(H,C, a, a, false, false);
//
//        
//        System.out.println("Assigning H and trimming");
//        
//        H.assign(C);
////        H.trimToSize();
//        System.out.println(H.cardinality());
//        System.out.println("\ndone in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
//        start = System.currentTimeMillis();
//        
//        //4
//        System.out.println("4");
//        H.zMult(H,C, a, a, false, false);
//        
//        
//        System.out.println("Assigning H and trimming");
//        
//        H.assign(C);
////        H.trimToSize();
//        System.out.println(H.cardinality());
//        System.out.println("\ndone in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
//        start = System.currentTimeMillis();
//        
//        //5
//        System.out.println("5");
//        H.zMult(H,C, a, a, false, false);
//
//        
//        System.out.println("Assigning H and trimming");
//        
//        H.assign(C);
////        H.trimToSize();
//        System.out.println(H.cardinality());
//        System.out.println("\ndone in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
//        start = System.currentTimeMillis();
//        
//        //6
//        System.out.println("6");
//       H.zMult(H,C, a, a, false, false);
//
//        
//        System.out.println("Assigning H and trimming");
//        
//        H.assign(C);
////        H.trimToSize();
//        System.out.println(H.cardinality());
//        System.out.println("\ndone in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
//        start = System.currentTimeMillis();
//        
//        //7
//        System.out.println("7");
//        H.zMult(H,C, a, a, false, false);
//
//        
//        System.out.println("Assigning H and trimming");
//        
//        H.assign(C);
////        H.trimToSize();
//        System.out.println(H.cardinality());
//        System.out.println("\ndone in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
//        start = System.currentTimeMillis();  
//        
//        //8
//        System.out.println("8");
//        H.zMult(H,C, a, a, false, false);
//
//        
//        System.out.println("Assigning H and trimming");
//        
//        H.assign(C);
////        H.trimToSize();
//        System.out.println(H.cardinality());
//        System.out.println("\ndone in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
//        start = System.currentTimeMillis();    
        /*
        //9 
        System.out.println("9");
        H.zMult(H,C, a, a, false, false);

        
        System.out.println("Assigning H and trimming");
        
        H.assign(C);
//        H.trimToSize();
        System.out.println(H.cardinality());
        System.out.println("\ndone in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
        start = System.currentTimeMillis();
        

        System.out.println(f0.cardinality());
        */
        /* Entire thing into f(0) */
        System.out.println("Entire thing into f0");
        DenseDoubleMatrix2D f1 = new DenseDoubleMatrix2D(numNodes,1);
        f1.assign(0);
        H.zMult(f0, f1, a, a, false, false);
         
        System.out.println(f1.cardinality());
        double[][] res = f1.toArray();
        
        for(int j=0; j<numNodes;j++)
        {
            String queryLabel = l.getVertexLabel(j);
//            System.out.println(queryLabel);
            String query = "INSERT INTO heat10 VALUES ("+ res[j][0] +"," + j +"," + " '"+queryLabel +"' "+ ")";
            try {
                stmt.execute(query);
            }
            catch(SQLException e) {
               e.printStackTrace();
            }
        }
//        double[] r = new double[numNodes];
//        double[] r1 = new double[numNodes];
//        for(int j=0; j<numNodes; j++)
//        {
//            r1[j] = res[0][j];
//            r[j] = res[0][j];
//        }
//        Arrays.sort(r1);
//        for(int j=0; j<5;j++)
//        {
//            System.out.println(r1[j]);
//            int in = Arrays.binarySearch(r, r1[j]);
//            System.out.println(in);
//        }
        
        
        
//        System.out.println("Assigning D and trimming");
//        
//        D.assign(C);
//        D.trimToSize();
//        
//        System.out.println("Assigning C to 0 and trimming");
//        C.assign(0);
//        C.trimToSize();
        
//        wait(10000);
//        System.out.print("sleepin");
//        Thread.sleep(40000);
//        System.out.println("Again");
//        C = H.zMult(D, null, a, a, true, true);
        
    }
    
//    public DoubleDoubleFunction sub()
//    {
//        DoubleDoubleFunction k ;
//        k.apply(P, P);
//    }
//   
    public void dataSetup() throws Exception
    {
        ArrayList AN = new ArrayList();
        ArrayList AJ = new ArrayList();
        ArrayList AI = new ArrayList();
        
        int check =0 ,counter =-1,f =0;
        long start;
        
        /* f(0) array */
        
        System.out.println("\nCreating F(0)");
        start = System.currentTimeMillis();
        
        float [][] initialHeatdata = new float[1][numNodes];
        String[] label = l.getVertexLabels(0, 4999);
        int index = Arrays.binarySearch(label, q);
        initialHeatdata[0][index] = 10;
        
        System.out.println("done in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
        
        //SparseDoubleMatrix2D H = new SparseDoubleMatrix2D(numNodes,numNodes);
        
        /* H array */
        
        System.out.println("\nCreating H");
        start = System.currentTimeMillis();
        
        
        for(int i=0; i < numNodes; i++)
        {
            f =0;
            int iout = g.outdegree(i);
            int[] isucc = g.successorArray(i);
            
            for(int j=0; j < numNodes; j++)
            {
                if(i == j)
                {
                    continue;
                }
                else
                {
                    int flag =0;
                    for(int k=0; k<iout; k++) {
                        if(isucc[k] == j)
                        {
                            flag = 1;
                            break;
                        }
                    }
                    if(flag != 1)
                    {
                        continue;
                    }
                    flag=0;
                    ++check;
                    ++counter;
                    
                    int jsucc[] = g.successorArray(j);
                    int jout = g.outdegree(j);
                    String[] click = l.getOutgoingAttribute(j, "click");

                    flag = -1;
                    for(int k=0; k<jout; k++) {
                        if(jsucc[k] == i)
                        {
                            flag = k;
                            break;
                        }
                    }
                    
                    if(flag == -1)
                        System.out.println("Flag Error");
                    
                    float Wji = Float.parseFloat(click[flag]);
                    
                    float Wjk = 0;
                    for(int k=0; k < jout; k++)
                        Wjk = Wjk + Float.parseFloat(click[k]);
                    
                    float val = (float) (Wji / Wjk);
                    double val1 = (double) (Wji / Wjk);
                   // H.setQuick(i,j,val1);
                    AN.add(new Float(val));
                    AJ.add(new Integer(j));
                    if(f == 0) {
                        AI.add(new Integer(counter));
                        f = 1;
                    }
                }
            }
            
            if(check != iout)
                System.out.println("Error : "+check+" "+iout);
            check = 0;
        }
        
         AI.add(new Integer(counter+1));
         
         /* Create SparseMatrix object */
         SparseMatrix H = new SparseMatrix(AN,AJ,AI,AI.size()-1,AI.size()-1);
         //dataConsistencyTest(H);
         
         System.out.println("done in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
         
        /* Create D */
         
        System.out.println("\nCreating D");
        start = System.currentTimeMillis();
        
        ArrayList DAN = new ArrayList();
        ArrayList DAJ = new ArrayList();
        ArrayList DAI = new ArrayList();
        counter = -1;
        
        for(int i=0; i<g.numNodes(); i++)
        {
            int out = g.outdegree(i);
            if(out > 0)
            {
                ++counter;
                DAN.add(new Float(1.0));
                DAJ.add(new Integer(i));
                DAI.add(counter);
            }
            
        }
        DAI.add(counter+1);
        SparseMatrix D = new SparseMatrix(AN,AJ,AI,AI.size()-1,AI.size()-1);
        
        System.out.println("done in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
         /* compute */
         compute(H,D);
        
    }
    
    public void compute(SparseMatrix H,SparseMatrix D) throws Exception
    {
        long start;
        
        /* H = H - D */
        
        System.out.println("\n H = H - D");
        start = System.currentTimeMillis();
        
        for(int i=0; i<D.getRow(); i++)
        {
            boolean b = H.contains(i,i);
            boolean c = D.contains(i,i);
            float val1,val2,sub;
            
            if(b == true)
                val1 = H.get(i, i);
            else
                val1 = 0;
            
            if(c == true)
                val2 = H.get(i, i);
            else
                val2 = 0;
                
            sub = val1 - val2;
            if(sub == 0)
                continue;
            else
                H.update(i, i, sub);
        }
        
        System.out.println("done in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
        
        /* X = (I + (a/P)H) */
        
        System.out.println("\n X = (I + (a/P)H)");
        start = System.currentTimeMillis();
        
        for (int i = 0; i < D.getRow(); i++)
        for (int j = 0; j < D.getColumn(); j++)
        {
            float val;
            boolean b = H.contains(i,j);

            if(b == true && i==j)
            {
                val = H.get(i,j);
                val = val * (1/10);
                val = val+1;
                H.update(i, j, val);
            }
            else
            if(b == true)
            {
                val = H.get(i,j);
                val = val * (1/10);
                H.update(i, j, val);
            }
            else
            if(i==j)
            {
                H.update(i, j, 1);
            }
        }
        
        System.out.println("done in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
        
        /* X^P */
        
        System.out.println("\n X ^ P");
        start = System.currentTimeMillis();
        
        H = H.mul(H);
        
        System.out.println("done in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
        
    }
    
    public void dataConsistencyTest(SparseMatrix H) throws Exception
    {
        System.out.println("Starting testing");
        boolean c;
        boolean n;
        for(int i=0; i<g.numNodes(); i++)
        {
            int out = g.outdegree(i);
            int[] succ = g.successorArray(i);
            c = false;
            n = false;
            for(int j=0 ; j<g.numNodes(); j++)
            {
                for(int k=0; k<out; k++)
                {
                    if(succ[k] == j)
                    {
                        c = true;
                        break;
                    }
                }
                n = H.contains(i, j);
                if(n != c)
                {
                    System.out.println("ERROR !!!!!!!!");
                    return;
                }
                n = false;
                c = false;
            }
        }
        System.out.println("Test Successful");
        
    }
   
}
