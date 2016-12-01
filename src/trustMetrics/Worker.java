package trustMetrics;
import java.awt.Color;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

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

public class Worker extends Agent{
	private Context<?> context;
	private Network<Object> net;
	private HashMap<String,Float> skillSet;
	public boolean assigned;
	private String name;
	private ArrayList<RWSV> rwsvList = new ArrayList<RWSV>();
	private ArrayList<SimpleEntry<Task,Float>> pastProjectsRatings = new ArrayList<SimpleEntry<Task, Float>>();
	
	//Constructor
	public Worker(String name,HashMap<String,Float> sset)
	{
		this.name = name;
		skillSet = sset;
		this.assigned = false;
		Iterator it = sset.entrySet().iterator();	
		while(it.hasNext()){
			Map.Entry<String, Float> pair = (Map.Entry<String,Float>)it.next();
			RWSV rel = new RWSV(this, pair.getKey(), (float) 1);  // Manager always assume 1.0 when on a new worker.
			rwsvList.add(rel);
		}
	}
	

	public void setup()
	{
		DFAgentDescription template = new DFAgentDescription();
		template.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("Worker");
		template.addServices(sd);
		 try {
	         DFService.register(this, template);
	      } catch(FIPAException e) {
	         e.printStackTrace();
	      }
		
		
	}

	public void iterateOverFIRE(Task t, float rating){
		int pastrecordssize = pastProjectsRatings.size();
		//if(pastrecordss) TODO Add FIRE IT calculations.  Check need for IT storage.
	}

	// GETTERS
	// Exception to format, because using "getName()" would need to override Agent class
	public String getNamePrivate(){
		return this.name;
	}
	
	public HashMap<String, Float> getSkillSet(){
		return this.skillSet;
	}
	
	public float getSkillValue_RWSV(String skill){
		for(RWSV r: rwsvList){
			if(r.getSkill().equals(skill))
				return r.getValue();
		}
		return 0;  // If not in the skillSet, assume zero skill.
	}
}
