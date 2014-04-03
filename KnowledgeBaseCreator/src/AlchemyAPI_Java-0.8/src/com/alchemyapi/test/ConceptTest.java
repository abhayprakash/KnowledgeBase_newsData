package com.alchemyapi.test;

import com.alchemyapi.api.AlchemyAPI;

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

class ConceptTest {
    public static void main(String[] args) throws IOException, SAXException,
            ParserConfigurationException, XPathExpressionException {
        // Create an AlchemyAPI object.
        AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile("E:\\Projects\\NewsData\\KnowledgeBase\\KnowledgeBaseCreator\\src\\AlchemyAPI_Java-0.8\\testdir\\api_key.txt");
/*
        // Extract concept tags for a web URL.
        Document doc = alchemyObj.URLGetRankedConcepts("http://www.techcrunch.com/");
        System.out.println(getStringFromDocument(doc));
*/
        // Extract concept tags for a text string.
        Document doc = alchemyObj.TextGetRankedConcepts(
            "Rahul says Modi s Gujarat govt was involved in 2002 riots");
        System.out.println(getStringFromDocument(doc)); 
        doc.getDocumentElement().normalize();
        NodeList ll = doc.getElementsByTagName("concept");
        for(int i = 0; i < ll.getLength(); i++)
        {
            Element el = (Element) ll.item(i);
            
            System.out.println(el.getElementsByTagName("text").item(0).getTextContent());
        }
/*
        // Load a HTML document to analyze.
        String htmlDoc = getFileContents("data/example.html");

        // Extract concept tags for a HTML document.
        doc = alchemyObj.HTMLGetRankedConcepts(htmlDoc, "http://www.test.com/");
        System.out.println(getStringFromDocument(doc));
*/    }

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
