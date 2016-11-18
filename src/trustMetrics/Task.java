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

	
	private boolean finished;
	private boolean available;
	private int cost;
    //the cost of the task along the critical path
    private int criticalCost;
    //a name for the task for printing
    private String name;
    //the tasks on which this task is dependent
    private HashSet<Task> dependencies = new HashSet<Task>();
    //the skills on which this task is dependent
    private String[] skills;
    //Constructor
    public Task(String name, int cost, String[] skills, Task... dependencies) {
      this.name = name;
      this.cost = cost;
      this.skills = skills;
      for(Task t : dependencies){
        this.dependencies.add(t);
      }
      if(name.equals("Start")) finished = true;
      else finished = false;
      available = false;
    }
    // Agent setup function.
	public void setup()
	{
		DFAgentDescription template = new DFAgentDescription();
		template.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("Manager");   //  @Ricardo check this . 
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
	public void setAvailable() {
	      available = true;
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
	
	
	//Some GETTERS for better encapsulation
	public HashSet<Task> getDependencies(){
		return this.dependencies;
	}
	
	public boolean getFinished(){
		return this.finished;
	}
	
	public boolean getAvailable(){
		return this.available;
	}
	
	public int getCost(){
		return this.cost;
	}
	
	// Exception to format, because using "getName()" would need to override Agent class
	public String getNamePrivate(){
		return this.name;
	}
	
	public int getCriticalCost(){
		return this.criticalCost;
	}
	
	public String[] getSkills(){
		return this.skills;
	}
	
	//SETTERS
	
	public void setCriticalCost(int cc){
		this.criticalCost = cc;
	}
}
