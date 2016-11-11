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
public class RepastStart extends RepastSLauncher {
	
	public static final boolean SEPARATE_CONTAINERS = false;
	private ContainerController mainContainer;
	private ContainerController agentContainer;
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
		Task t1 = new Task("Task 1");
		Task t2 = new Task("Task 2");
		Task t3 = new Task("Task 3");
		Task t4 = new Task("Task 4");
		Task t5 = new Task("Task 5");
		t1.addEdge(t2);
		t2.addEdge(t3);
		t2.addEdge(t4);
		t4.addEdge(t5);
		t3.addEdge(t5);
		// Project def
		try {
			agentContainer.acceptNewAgent("Task 1", t1).start();
			agentContainer.acceptNewAgent("Task 2", t2).start();
			agentContainer.acceptNewAgent("Task 3", t3).start();
			agentContainer.acceptNewAgent("Task 4", t4).start();
			agentContainer.acceptNewAgent("Task 5", t5).start();
			
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Launched.");
	}
	public Context build(Context<Object> context) {
		// http://repast.sourceforge.net/docs/RepastJavaGettingStarted.pdf
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("Network", context, true);
		netBuilder.buildNetwork();
		return super.build(context);
	}


}
