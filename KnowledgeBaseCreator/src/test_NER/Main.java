/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test_NER;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
/**
 *
 * @author SAHISNU
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // =================== INPUT ================================

        String s = "Mr. Bob stays in Delhi since 1998. He likes to eat chicken at night.";
        System.out.println(s);

        // ==========================================================

        Annotation document = new Annotation(s);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        System.out.println();
        int count=1;
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                String ne = token.get(NamedEntityTagAnnotation.class);
                System.out.println(ne);
                if(!ne.toString().equals("O"))
                   System.out.println(ne.toString()+" ----> "+count);
                count++;
            }
        }

        System.out.println();
    }
}
