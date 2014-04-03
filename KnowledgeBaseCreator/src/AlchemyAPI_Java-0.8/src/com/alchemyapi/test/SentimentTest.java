package com.alchemyapi.test;

import com.alchemyapi.api.AlchemyAPI;
import com.alchemyapi.api.*;

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

class SentimentTest {
    public static void main(String[] args) throws IOException, SAXException,
            ParserConfigurationException, XPathExpressionException {
        // Create an AlchemyAPI object.
        AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile("E:\\Projects\\NewsData\\KnowledgeBase\\KnowledgeBaseCreator\\src\\AlchemyAPI_Java-0.8\\testdir\\api_key.txt");
/*
        // Extract sentiment for a web URL.
        Document doc = alchemyObj.URLGetTextSentiment("http://www.techcrunch.com/");
        System.out.println(getStringFromDocument(doc));
*/
        // Extract sentiment for a text string.
        Document doc = alchemyObj.TextGetTextSentiment(
            "Modi says Rahul is awesome");
        
        NodeList nl = doc.getElementsByTagName("docSentiment");
        Element eElement = (Element) nl.item(0);
        
        System.out.println(eElement.getElementsByTagName("type").item(0).getTextContent());
        
        System.out.println("Document sentiment");
        System.out.println(getStringFromDocument(doc));
/*
        // Load a HTML document to analyze.
        String htmlDoc = getFileContents("/home/bcard/data/example.html");

        // Extract sentiment for a HTML document.
        doc = alchemyObj.HTMLGetTextSentiment(htmlDoc, "http://www.test.com/");
	System.out.println(getStringFromDocument(doc));
*/	
	// Extract entity-targeted sentiment from a HTML document.
        System.out.println("entity targetted sentiment");
	AlchemyAPI_NamedEntityParams entityParams = new AlchemyAPI_NamedEntityParams();
	entityParams.setSentiment(true);
	doc = alchemyObj.TextGetRankedNamedEntities("Satya Nadella is new Microsoft CEO", entityParams);
	
        NodeList nameList = doc.getElementsByTagName("entity");
        
        for(int i = 0; i < nameList.getLength(); i++)
        {
            Element e_entity = (Element) nameList.item(i);
            String entityType = e_entity.getElementsByTagName("type").item(0).getTextContent();
            String entityName = e_entity.getElementsByTagName("text").item(0).getTextContent();
            NodeList sentiInfo = e_entity.getElementsByTagName("sentiment");
            Element e_senti = (Element) sentiInfo.item(0);
            String sentimentName = e_senti.getElementsByTagName("type").item(0).getTextContent();
            
            System.out.println("et : " + entityType);
            System.out.println("en : " + entityName);
            System.out.println("sn : " + sentimentName);
        }
        
        //System.out.println(getStringFromDocument(doc));
/*	
	// Extract keyword-targeted sentiment from a HTML document.
        System.out.println("keyword targetted sentiment");
	AlchemyAPI_KeywordParams keywordParams = new AlchemyAPI_KeywordParams();
	keywordParams.setSentiment(true);
	doc = alchemyObj.TextGetRankedKeywords("That Mike Tyson is such a sweetheart.", keywordParams);
	System.out.println(getStringFromDocument(doc));
/*        
	//Extract Targeted Sentiment from text
        System.out.println("targetted sentiment");
	AlchemyAPI_TargetedSentimentParams sentimentParams = new AlchemyAPI_TargetedSentimentParams();
	sentimentParams.setShowSourceText(true);
	doc = alchemyObj.TextGetTargetedSentiment("This car is terrible.", "car", sentimentParams);
	System.out.print(getStringFromDocument(doc));

	//Extract Targeted Sentiment from url
	doc = alchemyObj.URLGetTargetedSentiment("http://techcrunch.com/2012/03/01/keen-on-anand-rajaraman-how-walmart-wants-to-leapfrog-over-amazon-tctv/", "Walmart",sentimentParams);
	System.out.print(getStringFromDocument(doc));

	//Extract Targeted Sentiment from html
	doc = alchemyObj.HTMLGetTargetedSentiment(htmlDoc, "http://www.test.com/", "WujWuj", sentimentParams);
	System.out.print(getStringFromDocument(doc));
*/
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
