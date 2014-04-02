package knowledgebasecreator;

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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class alchemyTest {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException
    {
        // Create an AlchemyAPI object.
        AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile("E:\\Projects\\NewsData\\KnowledgeBase\\KnowledgeBaseCreator\\src\\AlchemyAPI_Java-0.8\\testdir\\api_key.txt");

        // Extract a ranked list of named entities from a text string.
        Document doc = alchemyObj.TextGetRankedNamedEntities("Mr. Bob is going to India, yes India");
        System.out.println(getStringFromDocument(doc));
        
        System.out.println(doc.getElementsByTagName("type").item(0).getTextContent());
        System.out.println(doc.getElementsByTagName("text").item(0).getTextContent());
        
        System.out.println(doc.getElementsByTagName("type").item(1).getTextContent());
        System.out.println(doc.getElementsByTagName("text").item(1).getTextContent());
        
        //doc.getDocumentElement().normalize();
 /*
        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        if (doc.hasChildNodes()) {
 
		printNote(doc.getChildNodes());
 
	}
         */
    }
    
    private static void printNote(NodeList nodeList) {
 
    for (int count = 0; count < nodeList.getLength(); count++) {
 
	Node tempNode = nodeList.item(count);
 
	// make sure it's element node.
	if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
 
		// get node name and value
		System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
		System.out.println("Node Value =" + tempNode.getTextContent());
 
		if (tempNode.hasAttributes()) {
 
			// get attributes names and values
			NamedNodeMap nodeMap = tempNode.getAttributes();
 
			for (int i = 0; i < nodeMap.getLength(); i++) {
 
				Node node = nodeMap.item(i);
				System.out.println("attr name : " + node.getNodeName());
				System.out.println("attr value : " + node.getNodeValue());
 
			}
 
		}
 
		if (tempNode.hasChildNodes()) {
 
			// loop again if has child nodes
			printNote(tempNode.getChildNodes());
 
		}
 
		System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");
 
	}
 
    }
 
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
