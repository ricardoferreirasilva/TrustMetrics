package trustMetrics;

import java.util.ArrayList;

import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import repast.simphony.context.Context;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.ContextUtils;
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
	ArrayList<Task> edges;
	
	String name;
	@SuppressWarnings("unchecked")
	Task(String name)
	{
		this.name = name;
		edges = new ArrayList<Task>();
	}
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
	public String returnName()
	{
		return name;
	}
	public void addEdge(Task t)
	{
		edges.add(t);
	}
	private class drawEdges extends SimpleBehaviour
	{
		boolean drawedYet = false;
		@Override
		public void action() 
		{
			context = ContextUtils.getContext(myAgent);
			net = (Network<Object>) context.getProjection("Network");     
			for(Task t: edges)
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
