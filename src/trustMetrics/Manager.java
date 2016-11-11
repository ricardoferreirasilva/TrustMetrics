package trustMetrics;
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
public class Manager extends Agent{
	private Context<?> context;
	private Network<Object> net;
	private RepastEdge<Object> edge = null;
	String name;
	public Manager(String name)
	{
		this.name = name;
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
		firstBehaviour f = new firstBehaviour();
		addBehaviour(f);
		System.out.println("Manager");
		
	}
	private class firstBehaviour extends SimpleBehaviour
	{
		boolean d = false;
		@Override
		public void action() {
			System.out.println("Connecting");
			context = ContextUtils.getContext(myAgent);
			net = (Network<Object>) context.getProjection("Network");     
			for(Object obj: context)
			{
				if(obj instanceof Manager && obj != myAgent)
				{
					Manager m = (Manager) obj;
					System.out.println(m.name);
					net.addEdge(myAgent,obj,2);
				}
			}
			d = true;
			
			
		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return d;
		}
		
	}
}
