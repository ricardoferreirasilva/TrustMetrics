package trustMetrics;
import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
public class Manager extends Agent{
	private Context<?> context;
	private Network<Object> net;
	HashSet<Task> projectTasks;
	String name;
	public Manager(String name,Task... tasks)
	{
		this.name = name;
		projectTasks = new HashSet<Task>();
		for(Task t: tasks)
		{
			projectTasks.add(t);
		}
		
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
			System.out.println("Critical Path: "+Arrays.toString(criticalPath(projectTasks)));
			d = true;
			
			
		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return d;
		}
		
	}
	public static Task[] criticalPath(Set<Task> tasks){
	    //tasks whose critical cost has been calculated
	    HashSet<Task> completed = new HashSet<Task>();
	    //tasks whose ciritcal cost needs to be calculated
	    HashSet<Task> remaining = new HashSet<Task>(tasks);

	    //Backflow algorithm
	    //while there are tasks whose critical cost isn't calculated.
	    while(!remaining.isEmpty()){
	      boolean progress = false;

	      //find a new task to calculate
	      for(Iterator<Task> it = remaining.iterator();it.hasNext();){
	        Task task = it.next();
	        if(completed.containsAll(task.dependencies)){
	          //all dependencies calculated, critical cost is max dependency
	          //critical cost, plus our cost
	          int critical = 0;
	          for(Task t : task.dependencies){
	            if(t.criticalCost > critical){
	              critical = t.criticalCost;
	            }
	          }
	          task.criticalCost = critical+task.cost;
	          //set task as calculated an remove
	          completed.add(task);
	          it.remove();
	          //note we are making progress
	          progress = true;
	        }
	      }
	      //If we haven't made any progress then a cycle must exist in
	      //the graph and we wont be able to calculate the critical path
	      if(!progress) throw new RuntimeException("Cyclic dependency, algorithm stopped!");
	    }

	    //get the tasks
	    Task[] ret = completed.toArray(new Task[0]);
	    //create a priority list
	    Arrays.sort(ret, new Comparator<Task>() {

	      @Override
	      public int compare(Task o1, Task o2) {
	        //sort by cost
	        int i= o2.criticalCost-o1.criticalCost;
	        if(i != 0)return i;

	        //using dependency as a tie breaker
	        //note if a is dependent on b then
	        //critical cost a must be >= critical cost of b
	        if(o1.isDependent(o2))return -1;
	        if(o2.isDependent(o1))return 1;
	        return 0;
	      }
	    });

	    return ret;
	  }
}
