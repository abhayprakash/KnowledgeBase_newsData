/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test_COREF_RESOL;

import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import de.jollyday.HolidayManager;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.ling.CoreLabel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.joda.time.ReadablePeriod;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 *
 * @author user
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        //======================== INPUT ==========================================

        String input="My steak was SOFT but cooked exactly how I like it";

        System.out.println(input+"\n");
        // ========================================================================

        Properties props = new Properties();
        props.put("annotators", "tokenize,ssplit,pos,lemma,ner,parse,dcoref");
        StanfordCoreNLP pi = new StanfordCoreNLP(props);

        Annotation doc = new Annotation(input);
        pi.annotate(doc);
        Map<Integer, CorefChain> graph = doc.get(CorefChainAnnotation.class);


        for (Map.Entry<Integer, CorefChain> entry : graph.entrySet()) {
            CorefChain c = entry.getValue();

            if (c.getMentionsInTextualOrder().size() <= 1) {
                continue;
            }

            CorefMention cm = c.getRepresentativeMention();
            String clust = "";
            List<CoreLabel> tks = doc.get(SentencesAnnotation.class).get(cm.sentNum - 1).get(TokensAnnotation.class);
            for (int i = cm.startIndex - 1; i < cm.endIndex - 1; i++) {
                clust += tks.get(i).get(TextAnnotation.class) + " ";
            }
            clust = clust.trim();
            System.out.print("representative mention: \"" + clust + "\" is mentioned by:");

            //System.out.println(clust.toString());

            for (CorefMention m : c.getMentionsInTextualOrder()) {
                String clust2 = "";
                tks = doc.get(SentencesAnnotation.class).get(m.sentNum - 1).get(TokensAnnotation.class);
                for (int i = m.startIndex - 1; i < m.endIndex - 1; i++) {
                    clust2 += tks.get(i).get(TextAnnotation.class) + " ";
                }
                clust2 = clust2.trim();
                //don't need the self mention
                if (clust.equals(clust2)) {
                    continue;
                }

                System.out.println("\t" + clust2);
                //System.out.println(clust2.toString());
            }
        }
    }
}
