package trustMetrics;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;
import sajas.core.Agent;
import sajas.core.Runtime;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.*;
public class RepastStart extends RepastSLauncher {
	
	public static final boolean SEPARATE_CONTAINERS = false;
	private ContainerController mainContainer;
	private ContainerController agentContainer;
	Set<Task> TASKS;
	@Override
	public String getName() {
		return "Trust Metric Project";
	}

	@Override
	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);
		
		if(SEPARATE_CONTAINERS) {
			Profile p2 = new ProfileImpl();
			agentContainer = rt.createAgentContainer(p2);
		} else {
			agentContainer = mainContainer;
		}
		// Project def.
		Task end = new Task("End",0);
	
		Task F = new Task("F",3,end);
		F.addRequiredSkillset("QualB");
		
		Task E = new Task("E",3,end);
		E.addRequiredSkillset("QualA");

		Task D = new Task("D",3,F);
		D.addRequiredSkillset("DevB");
		
		Task C = new Task("C",5,E,F);
		C.addRequiredSkillset("DevA","DevB");
		
		Task B = new Task("B",3,E);
		B.addRequiredSkillset("DevA");
		
		Task A = new Task("A",2,B,C,D);
		A.addRequiredSkillset("DevB");
		
		Task start = new Task("Start",0,A);
		
		HashMap<String, Float> skillSet = new HashMap<String,Float>();
		
		skillSet.clear();
		skillSet.put("DevA", (float)1);
		skillSet.put("DevB", (float)0.5);
		Worker w1 = new Worker("Worker 1",skillSet);
		
		
		skillSet = new HashMap<String,Float>();    // important, so the same reference isn't made twice
		skillSet.put("DevA", (float)0.5);
		skillSet.put("DevB", (float)1);
		Worker w2 = new Worker("Worker 2",skillSet);
		
		skillSet = new HashMap<String,Float>();    // important, so the same reference isn't made twice
		skillSet.put("DevA", (float)0.8);
		skillSet.put("DevB", (float)0.6);
		Worker w3 = new Worker("Worker 3",skillSet);
		
		skillSet = new HashMap<String,Float>();    // important, so the same reference isn't made twice
		skillSet.put("QualA", (float)0.8);
		Worker w4 = new Worker("Worker 4",skillSet);
		
		skillSet = new HashMap<String,Float>();    // important, so the same reference isn't made twice
		skillSet.put("QualB", (float)0.9);
		Worker w5 = new Worker("Worker 5",skillSet);
		
		Manager m = new Manager("Manager",start,A,B,C,D,E,F,end);
		m.addWorkers(w1,w2,w3,w4,w5);
		// Project def
		try {
			agentContainer.acceptNewAgent("Task 1", end).start();
			agentContainer.acceptNewAgent("Task 2", F).start();
			agentContainer.acceptNewAgent("Task 3", E).start();
			agentContainer.acceptNewAgent("Task 4", D).start();
			agentContainer.acceptNewAgent("Task 5", C).start();
			agentContainer.acceptNewAgent("Task 6", B).start();
			agentContainer.acceptNewAgent("Task 7", A).start();
			agentContainer.acceptNewAgent("Task 8", start).start();
			agentContainer.acceptNewAgent("Manager", m).start();
			agentContainer.acceptNewAgent("Worker 1", w1).start();
			agentContainer.acceptNewAgent("Worker 2", w2).start();
			agentContainer.acceptNewAgent("Worker 3", w3).start();
			agentContainer.acceptNewAgent("Worker 4", w4).start();
			agentContainer.acceptNewAgent("Worker 5", w5).start();
			
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Context build(Context<Object> context) {
		// http://repast.sourceforge.net/docs/RepastJavaGettingStarted.pdf
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("Network", context, true);
		netBuilder.buildNetwork();
		return super.build(context);
	}


}
