
package practice7;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.jom.OptimizationProblem;
import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.Triple;

/** This is a template to be used in the lab work, a starting point for the students to develop their programs
 *
 */
public class DestinationLinkModularCapacities implements IAlgorithm
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
        /* Typically, you start reading the input parameters */
        double capacityModule = Double.parseDouble(algorithmParameters.get("capacityModule"));
        /* Now you make the things here that modify the NetPlan object: the state of the NetPlan object at the end of this method, is the new network design */

        netPlan.removeAllUnicastRoutingInformation();
        DoubleMatrix2D trafficMatrix = netPlan.getMatrixNode2NodeOfferedTraffic();

        OptimizationProblem op = new OptimizationProblem();

        int N = netPlan.getNumberOfNodes();
        int E = netPlan.getNumberOfLinks();

        op.addDecisionVariable("x_te",false, new int[]{N,E}, 0, Double.MAX_VALUE);
        op.addDecisionVariable("a_e", true, new int [] {E}, 0, Integer.MAX_VALUE );
        op.setObjectiveFunction("minimize", "sum(a_e)");

        for(Node node_n : netPlan.getNodes()){

            op.setInputParameter("deltaPlus", NetPlan.getIndexes(node_n.getOutgoingLinks()), "row");
            op.setInputParameter("deltaMinus", NetPlan.getIndexes(node_n.getIncomingLinks()), "row");
            for(Node node_t: netPlan.getNodes()){

                op.setInputParameter("h_nt", trafficMatrix.get(node_n.getIndex(),node_t.getIndex()));
                op.setInputParameter("h_t", netPlan.getVectorNodeEgressUnicastTraffic().get(node_t.getIndex()));



            }
        }

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
        param.add(Triple.of("capacityModule", "10","Size of the capacity module measured in the same units as the traffic"));
        return param;
    }
}
