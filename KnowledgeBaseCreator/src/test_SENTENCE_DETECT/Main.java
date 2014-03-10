/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test_SENTENCE_DETECT;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

/**
 *
 * @author user
 */
public class Main {

    /**
     * @param args the command line arguments
     */


     public static String sentence_detect_model="E:\\Projects\\NewsData\\KnowledgeBase\\KnowledgeBaseCreator\\src\\models\\en-sent.zip";

    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here

        //======================== INPUT ==========================================

        String input="Mr. willson is chairman of Elsevier, the Dutch publishing group. He likes to eat chicken at night. After Having dinner, he reads newspaper.";

        System.out.println(input+"\n");
        // ========================================================================
        
        InputStream is = new FileInputStream(sentence_detect_model);
        SentenceModel model = new SentenceModel(is);
        SentenceDetectorME sdetector = new SentenceDetectorME(model);

        String sentences[] = sdetector.sentDetect(input);

        System.out.println();
        for(int i=0;i<sentences.length;i++){
            System.out.println("SENTENCE "+(i+1)+": "+sentences[i]);
        }

    }

}
