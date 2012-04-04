
package heatdiffusion;

import java.io.IOException;

import it.unipi.di.textdb.BucketedHuffword;
import it.unipi.di.textdb.FrontCoding;
import it.unipi.di.textdb.TextDB;
import it.unipi.di.tokenizer.TermTokenizer;
import it.unipi.di.graph.GraphLabelerConfig;
import java.io.*;

public class GraphlabelerTDB {
    
    protected BufferedWriter config;
    
    public void convertTDB(String txtFile, String tdbFile) throws IOException {
        
        TextDB tdb = new BucketedHuffword(txtFile).build(tdbFile);
  
    }
    
    
    public void createConfig(String queryFile, String urlFile, String clickFile) throws IOException
    {
        TextDB query = TextDB.fromTDBFile(queryFile);
        TextDB url = TextDB.fromTDBFile(urlFile);
        TextDB click = TextDB.fromTDBFile(clickFile);

        GraphLabelerConfig conf = new GraphLabelerConfig();
        conf.setVertexDB(0, 2002205, query);
        conf.setVertexDB(2002206, 3166403, url);
        conf.setEdgeDB("click", click);
        
        config = new BufferedWriter(new FileWriter("intermediate/graphlabeler/graphlabeler.conf"));
        config.write("[0, 2002205]: query.tdb");
        config.newLine();
        config.write("[2002206, 3166403]: url.tdb");
        config.newLine();
        config.write("click: weight.tdb");
        config.close();
    }
    
    public void createConfigAfterDG()throws IOException
    {
        config = new BufferedWriter(new FileWriter("intermediate/graphlabeler/graphlabeler.conf"));
        config.write("[0, 2002205]: query.tdb");
        config.newLine();
        config.write("[2002206, 3166403]: url.tdb");
        config.newLine();
        config.write("click: click.tdb");
        config.close();
    }
    
    public void createConfigSubgraph(int queryNode)throws IOException
    {
        config = new BufferedWriter(new FileWriter("query/subgraph.conf"));
        config.write("[0, "+ queryNode+"]: query.tdb");
        config.newLine();
        config.write("["+(queryNode+1)+", 1999]: url.tdb");
        config.newLine();
        config.write("click: click.tdb");
        config.close();
    }
    
    public void createConfigSubgraph1()throws IOException
    {
        config = new BufferedWriter(new FileWriter("query/subgraph.conf"));
        config.write("[0, 4709]: query.tdb");
        config.newLine();
        config.write("[4710, 4999]: url.tdb");
        config.newLine();
        config.write("click: click.tdb");
        config.close();
    }
    
        public void createConfigSubgraph2()throws IOException
    {
        config = new BufferedWriter(new FileWriter("query/subgraph.conf"));
        config.write("[0, 1736]: query.tdb");
        config.newLine();
        config.write("[1737, 1999]: url.tdb");
        config.newLine();
        config.write("click: click.tdb");
        config.close();
    }
        
                public void createConfigSubgraph3()throws IOException
    {
        config = new BufferedWriter(new FileWriter("query/subgraph.conf"));
        config.write("[0, 1969]: query.tdb");
        config.newLine();
        config.write("[1970, 1999]: url.tdb");
        config.newLine();
        config.write("click: click.tdb");
        config.close();
    }
    
    
}
