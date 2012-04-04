package heatdiffusion;

//import it.unipi.di.util.Utils;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Calendar;
import java.io.*;
import heatdiffusion.HeatDiffusion;
import it.unipi.di.util.Utils;

public class RecommendationEngine {
    
    public static void main(String args[]) throws Exception {
       
//        GraphPrune graph = new GraphPrune();
//        WebGraphBin bin = new WebGraphBin();
        GraphlabelerTDB tdb = new GraphlabelerTDB();
//        long start;
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//          
//        start = System.currentTimeMillis();
//        
//        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        Calendar cal = Calendar.getInstance();
//        System.out.println("\nProgram started at : "+dateFormat.format(cal.getTime()));
//        
//        System.out.println("\n\n<----------Pruning Graph----------->\n\n");
//        graph.graphQueryTraverse();
//        
//        System.out.println("\ndone in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
//        start = System.currentTimeMillis();
//        
//        graph.writeQueryNode();
//        
//        System.out.println("\ndone in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
//        start = System.currentTimeMillis();
//        
//        graph.graphUrlTraverse();
//        
//        System.out.println("\ndone in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
//        start = System.currentTimeMillis();
//        
//        System.out.println("\n\n<----------Building WebGraph----------->");
//        
//        bin.createWebGraphFromBinaryFile("intermediate/prunedAol.txt", "intermediate/webgraph/aol");
//        
//        System.out.println("done in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
//        graph.comparegraph();
//        start = System.currentTimeMillis();
//        
//        System.out.println("\n\n<----------Building Graphlabeler-------->");
//        
//        tdb.convertTDB("intermediate/weight.txt","intermediate/graphlabeler/weight.tdb");
//        tdb.convertTDB("intermediate/url.txt","intermediate/graphlabeler/url.tdb");
//        tdb.convertTDB("intermediate/query.txt","intermediate/graphlabeler/query.tdb");
//        tdb.createConfig("intermediate/graphlabeler/query.tdb","intermediate/graphlabeler/url.tdb",
//                "intermediate/graphlabeler/weight.tdb");
//        
//        System.out.println("done in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
//        start = System.currentTimeMillis();
//        
//        System.out.println("\n\n<---------Converting to Directed graph-------->");
//        DirectedGraph dg = new DirectedGraph();
//        dg.convertToDirectedGraph();
//        tdb.convertTDB("intermediate/click.txt","intermediate/graphlabeler/click.tdb");
//        tdb.createConfigAfterDG();
//        
//        System.out.println("\ndone in: " + Utils.elapsedTime(start, System.currentTimeMillis()));
//        
//        System.out.println("Program ended at : "+dateFormat.format(cal.getTime()));
//        
        String query;
        System.out.println("Enter the Query : ");
        query = br.readLine();
        DepthFirst df = new DepthFirst(query);
        df.findNode();
        int queryNode = df.depthFirstSearch();

        tdb.convertTDB("query/url.txt", "query/url.tdb");
        tdb.convertTDB("query/query.txt", "query/query.tdb");
        tdb.convertTDB("query/click.txt", "query/click.tdb");
        tdb.createConfigSubgraph(queryNode);
////        
        HeatDiffusion hd = new HeatDiffusion(query);
        hd.coltTest();
        
//        GraphPrune graph = new GraphPrune();
//        graph.testClickData();
        
          //      hd.dataSetup();
//        hd.sparseMatrixTest();
//        hd.finale();

        
        
    }
}