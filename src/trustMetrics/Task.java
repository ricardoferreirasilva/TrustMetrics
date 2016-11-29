package trustMetrics;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;

import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import repast.simphony.context.Context;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.space.graph.JungEdgeTransformer;
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
	private RepastEssentials re = new RepastEssentials();
	
	private boolean finished;
	private boolean available;
	private int cost; //this should be the estimated time for the task to take.
    //the cost of the task along the critical path
    private int criticalCost;
    //a name for the task for printing
    private String name;
    //the tasks on which this task is dependent
    private HashSet<Task> dependencies = new HashSet<Task>();
    //the skills on which this task is dependent
    private  ArrayList<String> skills;
    
    
    ArrayList<Worker> assignedWorkers;
    double completion; //From 0-100%, when it reaches 100 finishes.
    double rate; //How much the task is progressing each week.
    
    //Constructor
    public Task(String name, int cost, Task... dependencies) {
      this.name = name;
      this.cost = cost;
      this.available = false;
      this.assignedWorkers = new ArrayList<Worker>();
      this.skills = new ArrayList<String>();
      this.completion = 0;
      this.rate = 0;
      for(Task t : dependencies){
        this.dependencies.add(t);
      }
      if(name.equals("Start")) finished = true;
      else finished = false;
    }
    // Agent setup function.
	public void setup()
	{
		DFAgentDescription template = new DFAgentDescription();
		template.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("Task");   //  @checked
		template.addServices(sd);
		 try {
	         DFService.register(this, template);
	      } catch(FIPAException e) {
	         e.printStackTrace();
	      }
		progressTask progressTask = new progressTask();
		drawEdges drawEdges = new drawEdges();
		addBehaviour(progressTask);
		addBehaviour(drawEdges);
	}
	@Override
    public String toString() {
      return name+": "+criticalCost;
    }
	public void setAvailable() {
	      available = true;
	      if(getNamePrivate().equals("End")) finished=true;
	    }
	public void addRequiredSkillset(String... skills)
	{
		for(String s: skills) this.skills.add(s);
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
    public void addWorker(Worker w) {
	      assignedWorkers.add(w);
	      w.assigned = true;
	      context = ContextUtils.getContext(this);
		  net = (Network<Object>) context.getProjection("Network");
		  ArrayList<RepastEdge> list = new ArrayList<RepastEdge>();
		  for (RepastEdge edge : net.getEdges()) {
			if(edge.getSource() instanceof Worker)
			{
				Worker edgeWorker = (Worker) edge.getSource();
			    if(w.getNamePrivate().equals(edgeWorker.getNamePrivate()))list.add(edge);
			}
		  }
		  for (RepastEdge edge : list) {
		  	net.removeEdge(edge);
		  }
		  net.addEdge(w,this);
		  System.out.println(w.getNamePrivate()+" (Perceived Value: "+getWorkerValue(w)+")" + " (Real Value: "+getRealWorkerValue(w)+")" + " -> " + getNamePrivate()+"\n");
    }
    public double getWorkerValue(Worker w) {
    	float sum_ratings = 0; //Sum of the ratings of the workers skills that are required by the Task.
    	//For each skill the worker has.
    	for(String s: w.getSkillSet().keySet())
    	{
    		if(skills.contains(s))
    		{
    			sum_ratings += w.getSkillValue_RWSV(s); // Manager Uses RWSV values not real values
    		}
    	}
	    return (sum_ratings / skills.size());
    }
    
    public double getRealWorkerValue(Worker w){
    	float sum_ratings = 0;
    	for(String s: w.getSkillSet().keySet())
    	{
    		if(skills.contains(s))
    			sum_ratings += w.getSkillSet().get(s);
    	}
    	return (sum_ratings /skills.size());
    	
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
	private class progressTask extends SimpleBehaviour
	{
		@Override
		public void action() 
		{
			if(available && !finished && assignedWorkers.size() > 0 && !getNamePrivate().equals("End"))
			{
				if(re.GetTickCount() % 10 == 0) //lets say 10 ticks is  one week.
				{
					double totalWorkerWorth = 0;
					for(Worker w : assignedWorkers)
					{
						totalWorkerWorth += getRealWorkerValue(w);
					}
					double calculatedDuration = cost / totalWorkerWorth;
					rate = (100)/(calculatedDuration * 4.34812141);
					//System.out.println(getName() + " I am progressing.\n");
					
					//here we would calculate rate
					
					completion += rate; //-> actual operation
					System.out.println(getNamePrivate()+" Rate: "+rate+"%/week\n");
					System.out.println(getNamePrivate()+" Done: "+completion+"% \n");
					//completion += 25; //for debbugging and checking if progress is working
					if(completion >= 100)
					{
						// Relevant commentary:  Here we should recalculate IT/Value of the Workers involved.
						// How can we mix it up ?  Give out a delay factor for completion? 
						finished = true;
						for(Worker w: assignedWorkers) w.assigned = false;
					}
				}	
			}
			
			
		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return finished;
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
	
	
	//SETTERS
	
	public void setCriticalCost(int cc){
		this.criticalCost = cc;
	}
}
