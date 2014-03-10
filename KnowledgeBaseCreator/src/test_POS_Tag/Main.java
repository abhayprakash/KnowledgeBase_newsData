/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test_POS_Tag;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 *
 * @author user
 */
public class Main {

    /**
     * @param args the command line arguments
     */

    public static MaxentTagger tagger = new MaxentTagger("E:\\Projects\\NewsData\\KnowledgeBase\\KnowledgeBaseCreator\\src\\models_2\\wsj-0-18-left3words-distsim.tagger");

    public static void main(String[] args) {
        // TODO code application logic here

        // =================== INPUT ================================

        String s = "Mr. Bob stays in Delhi since 1998. He likes to eat chicken at night.";
        System.out.println(s);

        // ======================================================

        System.out.println();
        String tagged = tagger.tagString(s);
        System.out.println(tagged);
        
    }
}
