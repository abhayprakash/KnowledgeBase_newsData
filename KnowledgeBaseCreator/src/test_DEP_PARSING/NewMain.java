/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test_DEP_PARSING;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
/**
 *
 * @author user
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
   
    private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible=true");
    private final static LexicalizedParser parser = LexicalizedParser.loadModel(PCG_MODEL);

    public Tree parse(String str) {
        List<CoreLabel> tokens = tokenize(str);
        Tree tree = parser.apply(tokens);
        return tree;
    }

    private List<CoreLabel> tokenize(String str) {
        Tokenizer<CoreLabel> tokenizer =
            tokenizerFactory.getTokenizer(
                new StringReader(str));
        return tokenizer.tokenize();
    }

    public static void main(String[] args) {
        // TODO code application logic here

        String input="I watched the movie yesterday with my friends.";

        System.out.println(input);
        Tree tree = new NewMain().parse(input);

        // Get dependency tree
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
        Collection<TypedDependency> td = gs.typedDependenciesCollapsed();
        System.out.println(td);

        Object[] list = td.toArray();
        System.out.println(list.length);
        TypedDependency typedDependency;
        for (Object object : list) {
            typedDependency = (TypedDependency) object;
            
            System.out.println(typedDependency.toString());
        }
    }

}
