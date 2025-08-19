package practice8;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jom.OptimizationProblem;
import com.net2plan.interfaces.networkDesign.*;
import com.net2plan.libraries.GraphUtils;
import com.net2plan.utils.Triple;
import sun.nio.ch.Net;

/** This is a template to be used in the lab work, a starting point for the students to develop their programs
 *
 */
public class TCPReno implements IAlgorithm
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
        /*2. Eliminar el tráfico cursado y borrar todas las rutas de la red*/
        netPlan.removeAllRoutes();
        /*3. Para cada demanda unicast, crear una ruta que lleve 0 unidades de tráfico, eligiendo para ello la
            ruta más corta entre el nodo origen y destino de la demanda, medida en número de saltos (p.ej.
            utilizando el metodo getShortestPath de GraphUtils).*/

        for(Demand demand : netPlan.getDemands()){
            List<Link> shortestPath = GraphUtils.getShortestPath(
                    netPlan.getNodes(),
                    netPlan.getLinks(),
                    demand.getIngressNode(),
                    demand.getEgressNode(),
                    null
            );
            if(shortestPath.isEmpty()) throw new Net2PlanException("No shortest path found for demand " + demand.getIndex());

            netPlan.addRoute(demand,0,0, shortestPath,null);
        }
        /*4. Crear un objeto tipo OptimizationProblem (p.ej., op)*/

        OptimizationProblem op = new OptimizationProblem();

        /*5. Añadir las variables de decisión del problema, con el nombre h_d: una variable por cada (ruta).
        La coordenada i-th corresponde al índice i del objeto Route (que está asociado a una demanda
        específica). El valor mínimo de la variable es 0 y el máximo Double.MAX_VALUE.
        */
        op.addDecisionVariable("h_d", false, new int[]{1,netPlan.getNumberOfRoutes()}, 0 , Double.MAX_VALUE);
        /*6. Establecer la función objetivo del problema.*/
        double [] VectorRoutePropagationTimes = netPlan.getVectorRoutePropagationDelayInMiliseconds().toArray();
        double [] RTT_d = new double[VectorRoutePropagationTimes.length];
        for (Demand demand : netPlan.getDemands()){
            RTT_d[demand.getIndex()] = VectorRoutePropagationTimes[demand.getIndex()] * 2;
        }
        double [] z_d = new double[VectorRoutePropagationTimes.length];
        for (Demand demand : netPlan.getDemands()){
            z_d[demand.getIndex()] = -3/2*Math.pow(RTT_d[demand.getIndex()],2);
        }
        op.setInputParameter("z_d",z_d, "row");

        op.setObjectiveFunction("maximize","z_d ./ h_d");

        /*7. Utilice un bucle for con tantas iteraciones como enlaces, para añadir las restricciones de capacidad de los enlaces(1)b.*/
        for(Link link : netPlan.getLinks()){
            op.setInputParameter("P_e", NetPlan.getIndexes(link.getTraversingRoutes()), "row");
            op.setInputParameter("u_e", link.getCapacity());
            op.addConstraint("sum(h_d(P_e) <= u_e)");

        }
        op.solve("ipopt");

        if (!op.solutionIsOptimal()) throw new Net2PlanException ("An optimal solution was not found");


        double [] h_d = op.getPrimalSolution("h_d").to1DArray();



        /* Save the solution in the netPlan object */
        for (Route r : netPlan.getRoutes())
        {
            final double traf = h_d [r.getIndex()];
            r.setCarriedTraffic(traf , traf);
        }

        for (Demand d: netPlan.getDemands())
        {
            final double traf = h_d [d.getIndex()];
            d.setOfferedTraffic(traf);
        }

        double totalOfferedTraffic = netPlan.getVectorDemandCarriedTraffic().zSum();
        double totalCarriedTrafffic = netPlan.getVectorDemandCarriedTraffic().zSum();

        if(totalOfferedTraffic != totalCarriedTrafffic) throw new Net2PlanException("");

        /* Now you make the things here that modify the NetPlan object: the state of the NetPlan object at the end of this method, is the new network design */



        return "Ok! Total throughput:"  + totalOfferedTraffic; // this is the message that will be shown in the screen at the end of the algorithm
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
