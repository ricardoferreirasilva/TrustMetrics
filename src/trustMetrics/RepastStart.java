package trustMetrics;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
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
		Manager m1 = new Manager();
		Manager m2 = new Manager();
		try {
			agentContainer.acceptNewAgent("Manager1", m1).start();
			agentContainer.acceptNewAgent("Manager2", m2).start();
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
