/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test_WORDNET;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

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

        System.setProperty("wordnet.database.dir", "E:\\Projects\\NewsData\\KnowledgeBase\\KnowledgeBaseCreator\\src\\dict");

        // ==============================INPUT ================================
        String wordForm = "eat";

        //=====================================================================

        //  Get the synsets containing the wrod form
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets(wordForm);

        //  Display the word forms and definitions for synsets retrieved
        if (synsets.length > 0) {
            System.out.println("The following synsets contain '"
                    + wordForm + "' or a possible base form "
                    + "of that text:");
            
            for (int i = 0; i < synsets.length; i++) {
                System.out.println("");
                String[] wordForms = synsets[i].getWordForms();
                for (int j = 0; j < wordForms.length; j++) {
                    System.out.print((j > 0 ? ", " : "")
                            + wordForms[j]);
                }

                System.out.println();
                System.out.println("Definition : " + synsets[i].getDefinition());

                System.out.println("EXAMPLES:");
                for(String s:synsets[i].getUsageExamples()){
                    System.out.println(s);
                }
                
                System.out.print("TYPE : ");
                SynsetType type = synsets[i].getType();

                if (type.equals(SynsetType.NOUN)) {
                    System.out.println("NOUN");
                    // Code for nouns
                } else if (type.equals(SynsetType.VERB)) {
                    System.out.println("Verb");
                    // Code for verbs
                } else {
                    System.out.println("NONE");
                    // Code for non-verb/nouns.
                }
            }
        } else {
            System.err.println("No synsets exist that contain the word form '" + wordForm + "'");
        }
    }

}
