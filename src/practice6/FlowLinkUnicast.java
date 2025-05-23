
package practice6;
import java.util.*;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.jom.DoubleMatrixND;
import com.jom.OptimizationProblem;
import com.net2plan.interfaces.networkDesign.*;
import com.net2plan.utils.Triple;

/** This is a template to be used in the lab work, a starting point for the students to develop their programs
 *
 */
public class FlowLinkUnicast implements IAlgorithm
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
        netPlan.removeAllUnicastRoutingInformation();
        OptimizationProblem op = new OptimizationProblem();

        op.addDecisionVariable(
                "x_de",
                false,
                new int [] {netPlan.getNumberOfDemands(), netPlan.getNumberOfLinks()},
                0,
                Double.MAX_VALUE
        );

        op.setObjectiveFunction("minimize", "sum(x_de)");

        for(Node node : netPlan.getNodes()){
            op.setInputParameter("deltaPlus", NetPlan.getIndexes(node.getOutgoingLinks()),"row");
            op.setInputParameter("deltaMinus", NetPlan.getIndexes(node.getIncomingLinks()),"row");
            for (Demand demand : netPlan.getDemands()){


                op.setInputParameter("h_d", demand.getOfferedTraffic());
                op.setInputParameter("d", demand.getIndex());

                if(node == demand.getIngressNode())
                    op.addConstraint("sum(x_de(d,deltaPlus)) - sum(x_de(d,deltaMinus)) == h_d");
                else if(node == demand.getEgressNode())
                    op.addConstraint("sum(x_de(d,deltaPlus)) - sum(x_de(d,deltaMinus)) == - h_d");
                else
                    op.addConstraint("sum(x_de(d,deltaPlus)) - sum(x_de(d,deltaMinus)) == 0");
            }
        }


        for(Link link : netPlan.getLinks()){
            op.setInputParameter("u_e", link.getCapacity());
            op.setInputParameter("e", link.getIndex());
            op.addConstraint("sum(x_de(all,e)) <= u_e");
        }

        op.solve("glpk");

        if (!op.solutionIsOptimal()) throw new Net2PlanException ("An optimal solution was not found");

        DoubleMatrix2D x_de = op.getPrimalSolution("x_de").view2D();
        Set<Demand> setOfDemands = new TreeSet<>(netPlan.getDemands());
        netPlan.setRoutingFromDemandLinkCarriedTraffic(x_de,false, true, setOfDemands);

        /* Now you make the things here that modify the NetPlan object: the state of the NetPlan object at the end of this method, is the new network design */



        return "Ok! Total bandwidth consumed in the links:" + netPlan.getVectorLinkCarriedTraffic().zSum(); // this is the message that will be shown in the screen at the end of the algorithm
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
        return param;
    }
}
