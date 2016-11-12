package trustMetrics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import repast.simphony.context.Context;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.ContextUtils;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import sajas.core.Agent;
import jade.core.AID;
import sajas.core.behaviours.Behaviour;
import sajas.core.behaviours.SimpleBehaviour;
import sajas.core.behaviours.WakerBehaviour;
import sajas.core.behaviours.WrapperBehaviour;
import sajas.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import sajas.proto.ContractNetInitiator;
import sajas.proto.SubscriptionInitiator;

// This will be the actual task, associated with an edge between to Project Nodes.
public class Task extends Agent{
	
	private Context<?> context;
	private Network<Object> net;

	
	
	public int cost;
    //the cost of the task along the critical path
    public int criticalCost;
    //a name for the task for printing
    public String name;
    //the tasks on which this task is dependant
    public HashSet<Task> dependencies = new HashSet<Task>();
    //Constructor
    public Task(String name, int cost, Task... dependencies) {
      this.name = name;
      this.cost = cost;
      for(Task t : dependencies){
        this.dependencies.add(t);
      }
    }
    // Agent setup function.
	public void setup()
	{
		DFAgentDescription template = new DFAgentDescription();
		template.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("Manager");
		template.addServices(sd);
		 try {
	         DFService.register(this, template);
	      } catch(FIPAException e) {
	         e.printStackTrace();
	      }
		 drawEdges drawEdges = new drawEdges();
		addBehaviour(drawEdges);
	}
	@Override
    public String toString() {
      return name+": "+criticalCost;
    }
    public boolean isDependent(Task t){
      //is t a direct dependency?
      if(dependencies.contains(t)){
        return true;
      }
      //is t an indirect dependency
      for(Task dep : dependencies){
        if(dep.isDependent(t)){
          return true;
        }
      }
      return false;
    }
	private class drawEdges extends SimpleBehaviour
	{
		boolean drawedYet = false;
		@Override
		public void action() 
		{
			context = ContextUtils.getContext(myAgent);
			net = (Network<Object>) context.getProjection("Network");     
			for(Task t: dependencies)
			{
				net.addEdge(myAgent,t);
			}
			drawedYet = true;
			
			
		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return drawedYet;
		}
		
	}
}
