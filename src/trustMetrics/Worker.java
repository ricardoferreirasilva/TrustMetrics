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
			RWSV rel = new RWSV(this, pair.getKey(), (float) pair.getValue(), (float)0.9);  // CHanged, Manager assume 0.9 when on a new worker.
			rwsvList.add(rel);
		}
	}
	//Gets the last rating on that skill.
	public double getLastRWSV()
	{
		if(rwsvList.size() > 0)
		{
			return rwsvList.get(rwsvList.size()-1).getValue();
		}
		else return 0;
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

	public void iterateOverFIRE_IT(Task t, float rating){
		
		pastProjectsRatings.add(new SimpleEntry<Task, Float>(t, rating));
		
		for(String skill: t.getRequiredSkills()){ // Iterate over each Skill
			
			//Variables necessary for equations
			int n = numberOfRecordsWithGivenSkill(skill);
			float weight = calculateSmallestWeight(n); //
			
			
			//Equation 1
			updateIT(skill, weight);
			
			//Equation 2
			float eq2;  // Equation 2 FIRE
			if(n<= 4)  // Equation 2 FIRE with m = 4
				eq2 = (float) n / 4;
			else
				eq2 = 1;
			
			
			//Equation 3
			float eq3;
			float eq3sum = 0;
			float currentIT = getITForGivenSkill(skill);
			for(int i = 0; i<pastProjectsRatings.size() ; i++){ // This syntax used to ensure we fetch the older ones first
																// Somatório in eq3 calculated in this for
				SimpleEntry<Task,Float> projrating = pastProjectsRatings.get(i);
				if(projrating.getKey().getRequiredSkills().contains(skill)){
					eq3sum += (float) weight * Math.abs(projrating.getValue() - currentIT) / 2; 
					weight *=2;
				}
			}
			eq3 = 1-eq3sum;  // End result of eq3
			
			float eq4 = eq3*eq2;  // Equation 4 of FIRE
			
			//Store reliability on RWSV of this skill 
			
			storeReliability(skill, eq4);
			
		}
	}
	
	public void iterateOverFIRE_WR(double real_worker_value, Task t){
		
		Random randomGenerator = new Random();
		int chance_of_event = randomGenerator.nextInt(100) - 50; // chance_of_event
															// [0..99] - 50  [-50% e 50%]
		double heteroevaluation = real_worker_value + (chance_of_event*real_worker_value) / 100;
		
		System.out.println("Task " + t.getNamePrivate() + " || " + this.getNamePrivate() + " || HeteroEval: " + heteroevaluation);	
		
		for(String skillz: t.getRequiredSkills()){
			for(RWSV rel : rwsvList){
				if(rel.getSkill().equals(skillz)){
					rel.calculateFIREValue(heteroevaluation);
				}
			}
		}
		
	}

	// GETTERS
	// Exception to format, because using "getName()" would need to override Agent class
	public String getNamePrivate(){
		return this.name;
	}
	
	public HashMap<String, Float> getSkillSet(){
		return this.skillSet;
	}
	
	//Returns supposed skill value (by manager), not real value.
	public float getSkillValue_RWSV(String skill){
		for(RWSV r: rwsvList){
			if(r.getSkill().equals(skill))
				return r.getValue();
		}
		return 0;  // If not in the skillSet, assume zero skill.
	}
	
	//UTILITIES
	
	public float calculateSmallestWeight(int size){
		float counter = 0;
		for(int i = 0; i < size;i++){
			counter += Math.pow(2, i);
		}
		return (float)1.0/(counter);
	}
	
	public int numberOfRecordsWithGivenSkill(String skill){
		int counter = 0;
		for(SimpleEntry<Task,Float> projrating: pastProjectsRatings)
			if(projrating.getKey().getRequiredSkills().contains(skill))
				counter++;
		return counter;
	}
	
	public float getITForGivenSkill(String skill){
		for(RWSV rel: rwsvList)
			if(rel.getSkill().equals(skill))
				return rel.getValue();
		return 0;
	}
	
	public void storeReliability(String skill, float reliability){
		for(RWSV rel: rwsvList)
			if(rel.getSkill().equals(skill))
				rel.setReliability(reliability);
	}
	
	public void updateIT(String skill, float weight){
		//Equation 1
		float thisweight = weight + 0;
		float sum = 0;
		for(int i = 0 ; i<pastProjectsRatings.size();i++){
			SimpleEntry<Task,Float> projrating = pastProjectsRatings.get(i);
			if(projrating.getKey().getRequiredSkills().contains(skill)){
				sum += (float) projrating.getValue() * thisweight;
				thisweight *=2;
			}
		}
		
		
		for(RWSV rel: rwsvList)
			if(rel.getSkill().equals(skill))
				rel.updateITValue(sum);
				
	}
}
