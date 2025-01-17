package Classification.Model.DecisionTree;

import Classification.Instance.CompositeInstance;
import Classification.Instance.Instance;
import Classification.InstanceList.InstanceList;
import Classification.Model.ValidatedModel;
import Classification.Performance.ClassificationPerformance;
import Math.Vector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class DecisionTree extends ValidatedModel implements Serializable {

    private DecisionNode root;

    /**
     * Constructor that sets root node of the decision tree.
     *
     * @param root DecisionNode type input.
     */
    public DecisionTree(DecisionNode root) {
        this.root = root;
    }

    public DecisionTree(String fileName){
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));
            root = new DecisionNode(input);
            input.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The predict method  performs prediction on the root node of given instance, and if it is null, it returns the possible class labels.
     * Otherwise it returns the returned class labels.
     *
     * @param instance Instance make prediction.
     * @return Possible class labels.
     */
    public String predict(Instance instance) {
        String predictedClass = root.predict(instance);
        if ((predictedClass == null) && ((instance instanceof CompositeInstance))) {
            predictedClass = ((CompositeInstance) instance).getPossibleClassLabels().get(0);
        }
        return predictedClass;
    }

    @Override
    public HashMap<String, Double> predictProbability(Instance instance) {
        return root.predictProbabilityDistribution(instance);
    }

    @Override
    public void saveTxt(String fileName) {
        try {
            PrintWriter output = new PrintWriter(fileName, "UTF-8");
            root.saveTxt(output);
            output.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public DecisionNode getRoot(){
        return root;
    }

    /**
     * The prune method takes a {@link DecisionNode} and an {@link InstanceList} as inputs. It checks the classification performance
     * of given InstanceList before pruning, i.e making a node leaf, and after pruning. If the after performance is better than the
     * before performance it prune the given InstanceList from the tree.
     *
     * @param node     DecisionNode that will be pruned if conditions hold.
     * @param pruneSet Small subset of tree that will be removed from tree.
     */
    public void pruneNode(DecisionNode node, InstanceList pruneSet) {
        ClassificationPerformance before, after;
        if (node.leaf){
            return;
        }
        before = testClassifier(pruneSet);
        node.leaf = true;
        after = testClassifier(pruneSet);
        if (after.getAccuracy() < before.getAccuracy()) {
            node.leaf = false;
            for (DecisionNode child : node.children) {
                pruneNode(child, pruneSet);
            }
        }
    }

    public void generateTestCode(String codeFileName, String methodName){
        try {
            PrintWriter output = new PrintWriter(codeFileName);
            output.println("public static String " + methodName + "(String[] testData){");
            root.generateTestCode(output, 1);
            output.println("\treturn \"\";");
            output.println("}");
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * The prune method takes an {@link InstanceList} and  performs pruning to the root node.
     *
     * @param pruneSet {@link InstanceList} to perform pruning.
     */
    public void prune(InstanceList pruneSet) {
        pruneNode(root, pruneSet);
    }
}
