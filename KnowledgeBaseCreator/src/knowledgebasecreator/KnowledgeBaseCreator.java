/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package knowledgebasecreator;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;

/**
 *
 * @author Abhay Prakash
 */
public class KnowledgeBaseCreator {
    
    /**
     * @param args the command line arguments
     */
    static Connection conn = null;
    static Statement stmt = null;
    
    static PrintWriter writerDeb;// = new PrintWriter(MachineDep.logFilePath, "UTF-8");
    
    private static GraphDatabaseService graphDb;
    
    private static enum RelTypes implements RelationshipType
    {
        OCCURED_TOGETHER
    }
    
    static void DebMsg(String id, String msg)
    {
        writerDeb.println(id + ": " + msg);
    }
    
    public static void main(String[] args) throws IOException {
        writerDeb = new PrintWriter(MachineDep.logFilePath, "UTF-8");
        
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( MachineDep.DB_PATH );
        registerShutdownHook( graphDb );
        
        try{
            Class.forName(MachineDep.JDBC_DRIVER);
            
            System.out.println("Connecting to database...");
            conn = (Connection) DriverManager.getConnection(MachineDep.DB_URL,MachineDep.USER,MachineDep.PASS);
            stmt = (Statement) conn.createStatement();
            
            insertEntitiesInGraphDb(graphDb);
            
            stmt.close();
            conn.close();
        }catch(Exception e){
            System.out.println("Error: " + e);
        }
        
        graphDb.shutdown();
    }
    
    static void insertEntitiesInGraphDb(GraphDatabaseService graphDb) throws SQLException, FileNotFoundException, UnsupportedEncodingException{
        String query;
        query = "SELECT newsHeadline FROM global_information_repository";
        ResultSet rs = stmt.executeQuery(query);
        
        while(rs.next()){
            String headline  = rs.getString("newsHeadline");
            System.out.println(headline);
            Vector<String> entities = getEntities(headline);
            for(String st : entities)
            {
                //insert in neo4j
                try(Transaction tx = graphDb.beginTx())
                {
                    Node entityNode = graphDb.createNode();
                    entityNode.setProperty("Entity", st);
                    // Database operations go here
                    tx.success();
                }
            }
        }
        writerDeb.close();
        rs.close();
    }
    
    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }
    
    static Vector<String> getEntities(String s)
    {
        Vector<String> toret = new Vector<>();
        Properties props = new Properties();
        props.put("annotators","tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
        Annotation document = new Annotation(s);
        pipeline.annotate(document);
        
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences)
        {
            List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
            int tokens_ListSize = tokens.size();
            
            CoreLabel token;
            for(int i = 0; i <  tokens_ListSize ; i++ )
            {
                String possibleEntity = "";
                token = tokens.get(i);
                String pos = token.get(PartOfSpeechAnnotation.class);
                String ner = token.get(NamedEntityTagAnnotation.class);
                while(pos.equals("NNP") || (pos.equals("NN") && !ner.equals("O"))){
                    possibleEntity += " " + token.get(TextAnnotation.class);
                    i++;
                    if(i == tokens_ListSize)
                        break;
                    token = tokens.get(i);
                    pos = token.get(PartOfSpeechAnnotation.class);
                    ner = token.get(NamedEntityTagAnnotation.class);
                }
                
                if(!possibleEntity.equals(""))
                {
                    toret.add(possibleEntity);
                }
            }
        }
        return toret;
    }
}