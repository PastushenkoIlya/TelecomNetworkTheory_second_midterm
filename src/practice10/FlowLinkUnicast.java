package practice10;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        double maximumLinkCapacity = Double.parseDouble(algorithmParameters.get("MaximumLinkCapacity"));
        double costPerGbps = Double.parseDouble(algorithmParameters.get("CostPerGbps"));
        double costPerKm = Double.parseDouble(algorithmParameters.get("CostPerKm"));

        netPlan.removeAllRoutes();
        for (Node node1 : netPlan.getNodes()){
            for (Node node2 : netPlan.getNodes()){
                if(!node1.equals(node2)){
                    netPlan.addLink(node1, node2, 0 netPlan.getNodePairEuclideanDistance(node1,node2),200000);
                }
            }
        }

        double L = netPlan.getNumberOfLinks();
        double D = netPlan.getNumberOfDemands();
        OptimizationProblem op = new OptimizationProblem();
        op.addDecisionVariable("z_ue",true, new int []{1,L}, 0,1);
        op.addDecisionVariable("u_e", false, new int []{1,L},0,maximumLinkCapacity);
        op.addDecisionVariable("x_de", false, new int []{D,L}, 0, Double.MAX_VALUE);

        op.setInputParameter("costPerKm", costPerKm);
        op.setInputParameter("maximumLinkCapacity", maximumLinkCapacity);
        op.setInputParameter("costPerGbps",costPerGbps);
        double [] vectorLinkLength = netPlan.getVectorLinkLengthInKm().toArray();
        op.setInputParameter("d_e", vectorLinkLength, "row");

        op.setObjectiveFunction("minimize", "costPerKm*sum(d_e.*z_e + costPerGbps*sum(u_e))");

        for (Link link : netPlan.getLinks()){
            op.setInputParameter("e",link.getIndex());
            op.addConstraint("u_e <= maximumLinkCapacity .* z_e");

        }

        for(Node node : netPlan.getNodes()){
            op.setInputParameter("deltaPlus", NetPlan.getIndexes(node.getOutgoingDemands()), "row");
            op.setInputParameter("deltaMinus", NetPlan.getIndexes(node.getIncomingDemands()), "row");
            for(Demand demand : netPlan.getDemands()){
                op.setInputParameter("d", demand.getIndex());
                op.setInputParameter("h_d", demand.getOfferedTraffic());
                if(node == demand.getIngressNode()) op.addConstraint("u_e == h_d");
            }
        }


        /* Now you make the things here that modify the NetPlan object: the state of the NetPlan object at the end of this method, is the new network design */



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
        param.add (Triple.of ("maximumLinkCapacity" , "1000" , "capacidad máxima"));
        param.add (Triple.of ("costPerGbps" , "1" , "un coste fijo cu por unidad de capacidad instalado "));
        param.add (Triple.of ("costPerKm" , "1" , "un coste fijo ckm por kilómetro de enlace instalado"));
        return param;
    }
}
