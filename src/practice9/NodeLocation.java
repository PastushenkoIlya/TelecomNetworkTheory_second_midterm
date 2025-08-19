package practice9;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jom.OptimizationProblem;
import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.Triple;

/** This is a template to be used in the lab work, a starting point for the students to develop their programs
 *
 */
public class NodeLocation implements IAlgorithm
{

    /** The method called by Net2Plan to run the algorithm (when the user presses the "Execute" button)
     * @param netPlan The input network design. The developed algorithm should modify it: it is the way the new design is returned
     * @param algorithmParameters Pair name-value for the current value of the input parameters
     * @param net2planParameters Pair name-value for some general parameters of Net2Plan
     * @return
     */
    @Override
    public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters, Map<String, String> net2planParameters)
    {
        double C = Double.parseDouble(algorithmParameters.get("C"));
        double K = Double.parseDouble(algorithmParameters.get("K"));


        /* Now you make the things here that modify the NetPlan object: the state of the NetPlan object at the end of this method, is the new network design */
        netPlan.removeAllRoutes();

        OptimizationProblem op = new OptimizationProblem();

        int N = netPlan.getNumberOfNodes();
        op.addDecisionVariable("z_j",true, new int [] {1,N},0,1);
        op.addDecisionVariable("e_ij", true, new int [] {N,N},0,1);
        op.setInputParameter("C", C);
        op.setInputParameter("K", K);
        op.setInputParameter("c_ij", netPlan.getMatrixNode2NodeEuclideanDistance());

        op.setObjectiveFunction("minimize"," C * sum(z_j) + sum(c_ij .* e_ij)");

        for(Node node : netPlan.getNodes()){
            op.setInputParameter("i", node.getIndex());
            op.addConstraint("sum(e_ij(i,all)) == 1");
        }
        for(Node node : netPlan.getNodes()){
            op.setInputParameter("i", node.getIndex());
            op.addConstraint("sum(e_ij(all,j)) <= K * z_j(j)");
        }

        op.solve("glpk");
        if(!op.solutionIsOptimal()) throw new Net2PlanException("The optimal solution is not optimal");

        final double[] z_j = op.getPrimalSolution("z_j").to1DArray();
        final double [][] e_ij = op.getPrimalSolution("e_ij").to2DArray();


        return "Ok!"; // this is the message that will be shown in the screen at the end of the algorithm
    }

    /** Returns a description message that will be shown in the graphical user interface
     */
    @Override
    public String getDescription()
    {
        return "Here you should return the algorithm description to be printed by Net2Plan graphical user interface";
    }


    /** Returns the list of input parameters of the algorithm. For each parameter, you shoudl return a Triple with its name, default value and a description
     * @return
     */
    @Override
    public List<Triple<String, String, String>> getParameters()
    {
        final List<Triple<String, String, String>> param = new LinkedList<Triple<String, String, String>> ();
        param.add (Triple.of ("C" , "10" , "Coste de un nodo troncal"));
        param.add (Triple.of ("K" , "5" , "Número máximo de nodos de acceso que se pueden conectar a un mismo nodo troncal."));
        return param;
    }
}
