/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package knowledgebasecreator;

import com.alchemyapi.api.AlchemyAPI;
import com.alchemyapi.api.AlchemyAPI_NamedEntityParams;
import com.alchemyapi.api.AlchemyAPI_RelationParams;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.w3c.dom.Element;
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
        OCCURED_TOGETHER,
        CONCEPT_FALLS_IN,
        APPEARED_IN,
        PUBLISHED_BY,
        ASSOCIATED_WITH
    }
    
    static AlchemyAPI alchemyObj;// = AlchemyAPI.GetInstanceFromFile("E:\\Projects\\NewsData\\KnowledgeBase\\KnowledgeBaseCreator\\src\\AlchemyAPI_Java-0.8\\testdir\\api_key.txt");
    
    static HashMap<String, Node> nodeIndex = new HashMap <>(); // this will be used for any type of node
    
    //TODO
    //static HashMap<Node, HashMap<Node, Relationship>> relTable = new HashMap<>();
    
    static void DebMsg(String id, String msg)
    {
        writerDeb.println(id + ": " + msg);
    }
    
    public static void main(String[] args) throws IOException {
        alchemyObj = AlchemyAPI.GetInstanceFromFile("E:\\Projects\\NewsData\\KnowledgeBase\\KnowledgeBaseCreator\\src\\AlchemyAPI_Java-0.8\\testdir\\api_key.txt");
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
    
    static void insertEntitiesInGraphDb(GraphDatabaseService graphDb) throws SQLException, FileNotFoundException, UnsupportedEncodingException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, ParseException{
        String query;
        query = "SELECT newsHeadline, start_time_stamp, source_id FROM global_information_repository";
        ResultSet rs = stmt.executeQuery(query);
        
        while(rs.next()){
            String headline  = rs.getString("newsHeadline");
            String timeOfNews = rs.getString("start_time_stamp");
            String publisherId = rs.getString("source_id");
            
            System.out.println(headline);
            
            // date of publication
            String[] dateAndTime = timeOfNews.split(" ");
            SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
            String dateString = dateAndTime[0];
            try(Transaction tx = graphDb.beginTx())
            {
                // Common Variables
                Node entityNode, prevNode, newsNode, conceptNode, topicNode = null, publisherNode;
                Relationship relationship;
                Label label;
                
                // creating or getting Topic Node(s)
                Document topicInfo = alchemyObj.TextGetCategory(headline);
                NodeList categoryList = topicInfo.getElementsByTagName("category");
                String likelyTopic = categoryList.item(0).getTextContent();
                if(!likelyTopic.equals("unknown") && !likelyTopic.equals(""))
                {
                    if(nodeIndex.containsKey(likelyTopic))
                    {
                        topicNode = nodeIndex.get(likelyTopic);
                    }
                    else
                    {
                        label = DynamicLabel.label("Topic");
                        topicNode = graphDb.createNode(label);
                        topicNode.setProperty("name", likelyTopic);
                        nodeIndex.put(likelyTopic, topicNode);
                    }
                }
                
                // creating or getting Concept Node(s)
                label = DynamicLabel.label("Concept");
                Document conceptInfo = alchemyObj.TextGetRankedConcepts(headline);
                NodeList concepts = conceptInfo.getElementsByTagName("concept");
                List<Node> conceptNodeList = new ArrayList<>();
                for(int i = 0; i < concepts.getLength(); i++)
                {
                    Element it_concept = (Element) concepts.item(i);
                    String conceptName = it_concept.getElementsByTagName("text").item(0).getTextContent();
                    if(nodeIndex.containsKey(conceptName))
                        conceptNode = nodeIndex.get(conceptName);
                    else
                    {
                        conceptNode = graphDb.createNode(label);
                        conceptNode.setProperty("name", conceptName);
                        nodeIndex.put(conceptName, conceptNode);
                    }
                    
                    // creating relationship CONCEPT_FALLS_IN
                    if(!likelyTopic.equals("unknown") && !likelyTopic.equals(""))
                    {
                        relationship = conceptNode.createRelationshipTo( topicNode, RelTypes.CONCEPT_FALLS_IN );
                    }
                    conceptNodeList.add(conceptNode);
                }
                
                // creating or getting Publisher Node
                if(!nodeIndex.containsKey(publisherId))
                {
                    label = DynamicLabel.label("Publisher");
                    publisherNode = graphDb.createNode(label);
                    publisherNode.setProperty("id", publisherId);
                    nodeIndex.put(publisherId, publisherNode);
                }
                else
                {
                    publisherNode = nodeIndex.get(publisherId);
                }
                
                // getting subject, object, relation among them and sentiment
                String Subject = "", Object = "", sentimentFromSubject = "neutral", action = "";
                AlchemyAPI_RelationParams relationParams = new AlchemyAPI_RelationParams();
                relationParams.setSentiment(true);
                relationParams.setRequireEntities(true);
                relationParams.setSentimentExcludeEntities(true);
                Document relationInfo = alchemyObj.TextGetRelations(headline, relationParams);
                
                Element relation1 = (Element)relationInfo.getElementsByTagName("relations").item(0);
                Element ele_nl;
//taking only one relation
                try{
                    ele_nl = (Element)relation1.getElementsByTagName("subject").item(0);
                    Subject = ele_nl.getElementsByTagName("text").item(0).getTextContent();
                }catch(Exception e)
                {
                    
                }
                try{
                    ele_nl = (Element)relation1.getElementsByTagName("object").item(0);
                    Object = ele_nl.getElementsByTagName("text").item(0).getTextContent();
                    try{
                        ele_nl = (Element)ele_nl.getElementsByTagName("sentimentFromSubject").item(0);
                        sentimentFromSubject = ele_nl.getElementsByTagName("type").item(0).getTextContent();
                    }catch(Exception e)
                    {
                        
                    }
                }catch(Exception e)
                {
                    
                }
                try{
                    ele_nl = (Element)relation1.getElementsByTagName("action").item(0);
                    action = ele_nl.getElementsByTagName("lemmatized").item(0).getTextContent();
                }catch(Exception e)
                {
                    
                }
                
                Document newsSentimentInfo = alchemyObj.TextGetTextSentiment(headline);
                NodeList nl = newsSentimentInfo.getElementsByTagName("docSentiment");
                Element eElement = (Element) nl.item(0);
                String newsSentiment = eElement.getElementsByTagName("type").item(0).getTextContent();
                
                // creating News Node
                label = DynamicLabel.label("newsNode");
                newsNode = graphDb.createNode(label);   // assuming news Headline will not repeat
                newsNode.setProperty("headline", headline);
                newsNode.setProperty("date", dateString);
                newsNode.setProperty("sentiment", newsSentiment);
                
                
                // creating entity nodes
                List<List<String>> entities = getEntities_AndTypes_AndSentiment(headline);
                for(int i = 0; i < entities.get(0).size(); i++)
                {
                    String entityName = entities.get(0).get(i);
                    String entityType = entities.get(1).get(i);
                    String entitySentiment = entities.get(2).get(i);
                    
                    //insert in neo4j
                    if(!nodeIndex.containsKey(entityName))
                    {
                        label = DynamicLabel.label("Entity");
                        entityNode = graphDb.createNode(label);
                        entityNode.setProperty("type", entityType);
                        entityNode.setProperty("name", entityName);
                        
                        entityNode.setProperty("generalSentimentOfSociety", entitySentiment);
                        nodeIndex.put(entityName, entityNode);
                        
                        entityNode.setProperty("noOfTimesAppeared", 1);
                        entityNode.setProperty("daysInLongestStreak", 1);
                        entityNode.setProperty("currentStreak", 1);
                        entityNode.setProperty("endDayOfLongestStreak", dateString); // our purpose is basically to capture new appearance of entity so taking just start
                    }
                    else
                    {
                        entityNode = nodeIndex.get(entityName);
                        // for the case if this was created due to being concept
                        try{
                            entityNode.getProperty("noOfTimesAppeared");
                        }
                        catch(Exception ex)
                        {
                            entityNode.setProperty("type", entityType);
                            entityNode.setProperty("generalSentimentOfSociety", entitySentiment);
                            entityNode.setProperty("noOfTimesAppeared", 0);
                            entityNode.setProperty("daysInLongestStreak", 1);
                            entityNode.setProperty("currentStreak", 1);
                            entityNode.setProperty("endDayOfLongestStreak", dateString); // our purpose is basically to capture new appearance of entity so taking just start
                        }
                        
                        Integer newCount = Integer.parseInt(entityNode.getProperty("noOfTimesAppeared").toString()) + 1;
                        entityNode.setProperty("noOfTimesAppeared", newCount.toString());
                        
                        String lastDate = entityNode.getProperty("endDayOfLongestStreak").toString();
                        
                        entityNode.setProperty("endDayOfLongestStreak", dateString);
                        
                        if(ft.parse(dateString).getTime() - ft.parse(lastDate).getTime() == (24 * 60 * 60 * 1000))
                        {
                            Integer currentStreak = Integer.parseInt((String)entityNode.getProperty("currentStreak")) + 1;
                            entityNode.setProperty("currentStreak", currentStreak.toString());
                            Integer longestStreak = Integer.parseInt((String)entityNode.getProperty("daysInLongestStreak"));
                            if(currentStreak > longestStreak)
                            {
                                entityNode.setProperty("daysInLongestStreak", currentStreak.toString());
                            }
                        }
                        else
                        {
                            entityNode.setProperty("currentStreak", 1);
                        }
                    }
                    
                    // creating relationship between entity and newsNode
                    relationship = entityNode.createRelationshipTo(newsNode, RelTypes.APPEARED_IN);
                    relationship.setProperty("date", dateString);
                    relationship.setProperty("sentiment",newsSentiment);
                    
                    // creating relationship entity to publisher node
                    relationship = entityNode.createRelationshipTo(publisherNode, RelTypes.PUBLISHED_BY);
                    /**TODO : maintain count - right now keeping just last one**/
                    relationship.setProperty("sentiment", newsSentiment);
                    
                    // creating relationship entity to concept node
                    /**TODO : maintain count - right now just the last one**/
                    Node it_conceptNode;
                    for(int conceptList_i = 0; conceptList_i < conceptNodeList.size(); conceptList_i++)
                    {
                        it_conceptNode = conceptNodeList.get(conceptList_i);
                        relationship = entityNode.createRelationshipTo(it_conceptNode, RelTypes.ASSOCIATED_WITH);
                    }
                    
                    // creating Entity-->Entity OCCURED_TOGETHER relation
                    for(int j = 0; j < i; j++)
                    {
                        String prevEntityName = entities.get(0).get(j);
                        prevNode = nodeIndex.get(prevEntityName);
                        relationship = prevNode.createRelationshipTo( entityNode, RelTypes.OCCURED_TOGETHER );
                        relationship.setProperty("date",dateString);
                        relationship.setProperty("sentiment",newsSentiment); // general sentiment of news
                        if(Subject.contains(prevEntityName) && Object.contains(entityName))
                        {
                            relationship.setProperty("action",action);
                            relationship.setProperty("sentiment",sentimentFromSubject);
                        }
                    }
                }
                // Database operations go here
                tx.success();
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
    
    static List<List<String>> getEntities_AndTypes_AndSentiment(String s) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException
    {
        List<List<String>> toret = new Vector<>();
        
        toret.add(new ArrayList<String>());
        toret.add(new ArrayList<String>());
        toret.add(new ArrayList<String>());
        
        AlchemyAPI_NamedEntityParams entityParams = new AlchemyAPI_NamedEntityParams();
        entityParams.setSentiment(true);
        Document doc = alchemyObj.TextGetRankedNamedEntities(s, entityParams);
        
        NodeList nameList = doc.getElementsByTagName("entity");
        
        for(int i = 0; i < nameList.getLength(); i++)
        {
            Element e_entity = (Element) nameList.item(i);
            toret.get(0).add(e_entity.getElementsByTagName("text").item(0).getTextContent());
            toret.get(1).add(e_entity.getElementsByTagName("type").item(0).getTextContent());
            NodeList sentiInfo = e_entity.getElementsByTagName("sentiment");
            Element e_senti = (Element) sentiInfo.item(0);
            toret.get(2).add(e_senti.getElementsByTagName("type").item(0).getTextContent());
        }
        
        return toret;
    }
}