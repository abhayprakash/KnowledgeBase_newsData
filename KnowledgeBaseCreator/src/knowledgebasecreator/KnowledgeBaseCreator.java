/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package knowledgebasecreator;

import com.alchemyapi.api.AlchemyAPI;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
    
    static void insertEntitiesInGraphDb(GraphDatabaseService graphDb) throws SQLException, FileNotFoundException, UnsupportedEncodingException, IOException, SAXException, ParserConfigurationException, XPathExpressionException{
        String query;
        query = "SELECT newsHeadline FROM global_information_repository";
        ResultSet rs = stmt.executeQuery(query);
        /*
        ExecutionEngine engine = new ExecutionEngine( graphDb );
 
        ExecutionResult result;
        */
        
        HashMap<String, Node > nodeIndex = new HashMap <>();
        
        while(rs.next()){
            String headline  = rs.getString("newsHeadline");
            System.out.println(headline);
            List<List<String>> entities = getEntities_AndTypes(headline);
            Node entityNode, prevNode, newsNode;
            
            Label label = DynamicLabel.label("newsNode");
            newsNode = graphDb.createNode(label);
            
            for(int i = 0; i < entities.size(); i++)
            {
                String entityName = entities.get(0).get(i); 
                //insert in neo4j
                try(Transaction tx = graphDb.beginTx())
                {
                    if(!nodeIndex.containsKey(entityName))
                    {    
                        entityNode = graphDb.createNode();
                        entityNode.setProperty("name", entityName);
                        entityNode.setProperty("noOfTimesAppeared", 0);
                        entityNode.setProperty("daysInLongestStreak", 1);
                        
                    }
                    else
                    {
                        entityNode = nodeIndex.get(st);
                        Integer newCount = Integer.parseInt((String) entityNode.getProperty("OccuredCount")) + 1;
                        entityNode.setProperty("OccuredCount", newCount);
                    }
                    entityNode.setProperty("News", headline);
                    
                    for(int j = 0; j < i; j++)
                    {
                        prevNode = nodeIndex.get(entities.get(j));
                        Relationship relationship = entityNode.createRelationshipTo( prevNode, RelTypes.OCCURED_TOGETHER );
                        relationship.setProperty( "HeadLine", headline);
                        relationship = prevNode.createRelationshipTo( entityNode, RelTypes.OCCURED_TOGETHER );
                        relationship.setProperty( "HeadLine", headline);
                    }
                    nodeIndex.put(st, entityNode);
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
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }
    
    static List<List<String>> getEntities_AndTypes(String s) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException
    {
        List<List<String>> toret = new Vector<>();
        
        toret.add(new ArrayList<String>());
        toret.add(new ArrayList<String>());
        
        AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile("E:\\Projects\\NewsData\\KnowledgeBase\\KnowledgeBaseCreator\\src\\AlchemyAPI_Java-0.8\\testdir\\api_key.txt");
        Document doc = alchemyObj.TextGetRankedNamedEntities(s);
        
        NodeList nameList = doc.getElementsByTagName("text");
        NodeList typeList = doc.getElementsByTagName("type");
        
        for(int i = 0; i < nameList.getLength(); i++)
        {
            toret.get(0).add(nameList.item(i).getTextContent());
            toret.get(1).add(typeList.item(i).getTextContent());
        }
        
        return toret;
    }
}