package com.alchemyapi.test;

import com.alchemyapi.api.AlchemyAPI;
import com.alchemyapi.api.AlchemyAPI_NamedEntityParams;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import java.io.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class EntityTest {
    public static void main(String[] args)
            throws IOException, SAXException,
            ParserConfigurationException, XPathExpressionException
    {
        // Create an AlchemyAPI object.
        AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile("E:\\Projects\\NewsData\\KnowledgeBase\\KnowledgeBaseCreator\\src\\AlchemyAPI_Java-0.8\\testdir\\api_key.txt");
        
        // Extract a ranked list of named entities for a web URL.
//        Document doc = alchemyObj.URLGetRankedNamedEntities("http://www.techcrunch.com/");
//        System.out.println(getStringFromDocument(doc));
        
        // Extract a ranked list of named entities from a text string.
        AlchemyAPI_NamedEntityParams entityParams = new AlchemyAPI_NamedEntityParams();
	entityParams.setSentiment(true);
        Document doc = alchemyObj.TextGetRankedNamedEntities(
                "Hello there, my name is Bob Jones.  I live in the United States of America.  " +
                        "Where do you live, Fred?");
        NodeList nameList = doc.getElementsByTagName("text");
        NodeList typeList = doc.getElementsByTagName("type");
        NodeList sentiList = doc.getElementsByTagName("sentiment");
        Element ee = (Element) sentiList.item(0);
        //System.out.println(ee.toString());
        //NodeList sentiType = ee.getElementsByTagName("type");
        for(int i = 0; i < nameList.getLength(); i++)
        {
            System.out.println("n " + nameList.item(i).getTextContent());
            System.out.println("t " + typeList.item(i).getTextContent());
            //System.out.println("s " + sentiType.item(0).getTextContent());
        }

//System.out.println(getStringFromDocument(doc));
        //System.out.println(doc.toString());
        
        // Load a HTML document to analyze.
//        String htmlDoc = getFileContents("data/example.html");
        
        // Extract a ranked list of named entities from a HTML document.
//        doc = alchemyObj.HTMLGetRankedNamedEntities(htmlDoc, "http://www.test.com/");
//        System.out.println(getStringFromDocument(doc));
    }
    
    // utility function
    private static String getFileContents(String filename)
            throws IOException, FileNotFoundException
    {
        File file = new File(filename);
        StringBuilder contents = new StringBuilder();
        
        BufferedReader input = new BufferedReader(new FileReader(file));
        
        try {
            String line = null;
            
            while ((line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        } finally {
            input.close();
        }
        
        return contents.toString();
    }
    
    // utility method
    private static String getStringFromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            
            return writer.toString();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
