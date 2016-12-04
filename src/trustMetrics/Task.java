package trustMetrics;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

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
public class Task extends Agent {

	private Context<?> context;
	private Network<Object> net;
	private RepastEssentials re = new RepastEssentials();

	private int expectedweeks = -1;
	private int weeksTook = 0;
	private boolean finished;
	private boolean available;
	private int cost; // this should be the estimated time for the task to take.
	// the cost of the task along the critical path
	private int criticalCost;
	// a name for the task for printing
	String name;
	// the tasks on which this task is dependent
	private HashSet<Task> dependencies = new HashSet<Task>();
	// the skills on which this task is dependent
	private ArrayList<String> skills;

	ArrayList<Worker> assignedWorkers;
	double completion; // From 0-100%, when it reaches 100 finishes.
	double rate; // How much the task is progressing each week.

	// Constructor
	public Task(String name, int cost, Task... dependencies) {
		this.name = name;
		this.cost = cost;
		this.available = false;
		this.assignedWorkers = new ArrayList<Worker>();
		this.skills = new ArrayList<String>();
		this.completion = 0;
		this.rate = 0;
		for (Task t : dependencies) {
			this.dependencies.add(t);
		}
		if (name.equals("Start"))
			finished = true;
		else
			finished = false;
	}

	// Agent setup function.
	public void setup() {
		DFAgentDescription template = new DFAgentDescription();
		template.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("Task"); // @checked
		template.addServices(sd);
		try {
			DFService.register(this, template);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		progressTask progressTask = new progressTask();
		drawEdges drawEdges = new drawEdges();
		addBehaviour(progressTask);
		addBehaviour(drawEdges);
	}

	@Override
	public String toString() {
		return name + ": " + criticalCost;
	}

	public void setAvailable() {
		available = true;
		if (getNamePrivate().equals("End"))
			finished = true;
	}

	public void addRequiredSkillset(String... skills) {
		for (String s : skills)
			this.skills.add(s);
	}

	public boolean isDependent(Task t) {
		// is t a direct dependency?
		if (dependencies.contains(t)) {
			return true;
		}
		// is t an indirect dependency
		for (Task dep : dependencies) {
			if (dep.isDependent(t)) {
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
			if (edge.getSource() instanceof Worker) {
				Worker edgeWorker = (Worker) edge.getSource();
				if (w.getNamePrivate().equals(edgeWorker.getNamePrivate()))
					list.add(edge);
			}
		}
		for (RepastEdge edge : list) {
			net.removeEdge(edge);
		}
		net.addEdge(w, this);
		System.out.println(w.getNamePrivate() + " (Perceived Value: " + getExpectedWorkerValue(w) + ")"
				+ " (Real Value: " + getRealWorkerValue(w) + ")" + " -> " + getNamePrivate() + "\n");
	}

	public double getExpectedWorkerValue(Worker w) {
		float sum_ratings = 0; // Sum of the ratings of the workers skills that
								// are required by the Task.
		// For each skill the worker has.
		for (String s : w.getSkillSet().keySet()) {
			if (skills.contains(s)) {
				sum_ratings += w.getSkillValue_RWSV(s); // Manager Uses RWSV
														// values not real
														// values
			}
		}
		return (sum_ratings / skills.size());
	}

	public double getRealWorkerValue(Worker w) {
		float sum_ratings = 0;
		for (String s : w.getSkillSet().keySet()) {
			if (skills.contains(s))
				sum_ratings += w.getSkillSet().get(s);
		}
		return (sum_ratings / skills.size());

	}

	private class drawEdges extends SimpleBehaviour {
		boolean drawedYet = false;

		@Override
		public void action() {
			context = ContextUtils.getContext(myAgent);
			net = (Network<Object>) context.getProjection("Network");
			for (Task t : dependencies) {
				net.addEdge(myAgent, t);
			}
			drawedYet = true;

		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return drawedYet;
		}

	}

	private class progressTask extends SimpleBehaviour {
		@Override
		public void action() {
			if (available && !finished && assignedWorkers.size() > 0 && !getNamePrivate().equals("End")) {
				// IF not yet calculated,
				// Calculate how many weeks we expect to take,
				// given the number of workers assigned +
				// their assumed worth given the skillset required
				if (expectedweeks == -1) {
					float expectedTotalWorkerWorth = 0;
					for (Worker w : assignedWorkers) {
						expectedTotalWorkerWorth += getExpectedWorkerValue(w) + 1;
					}
					float expectedmonths = cost / expectedTotalWorkerWorth;
					expectedweeks = (int) Math.ceil(expectedmonths * 4.34812141); // Number
																					// of
																					// weeks
																					// per
																					// month
																					// 4.34812141

				}
				if (re.GetTickCount() % 10 == 0) // lets say 10 ticks is one
													// week.
				{
					weeksTook++;
					float totalWorkerWorth = 0;
					for (Worker w : assignedWorkers) {
						totalWorkerWorth += getRealWorkerValue(w) + 1;
					}
					float calculatedDuration = cost / totalWorkerWorth;
					rate = (100) / (calculatedDuration * 4.34812141); // Number
																		// of
																		// weeks
																		// per
																		// month
																		// 4.34812141

					// Randomize unexpected event
					Random randomGenerator = new Random();
					int chance_of_event = randomGenerator.nextInt(100); // chance_of_event
																		// [0..99]
					if (chance_of_event < 10) // 10% chance of 50% faster rate
						rate += 0.5 * rate;
					else if (chance_of_event >= 90) // 10% chance of 50% slower
													// rate
						rate -= 0.5 * rate;

					// System.out.println(getName() + " I am progressing.\n");

					// here we would calculate rate

					completion += rate; // -> actual operation
					System.out.println(getNamePrivate() + " Rate: " + rate + "%/week\n");
					System.out.println(getNamePrivate() + " Done: " + completion + "% \n");
					// completion += 25; //for debbugging and checking if
					// progress is working
					if (completion >= 100) {
						finished = true;
						float rating;
						for (Worker w : assignedWorkers) {
							
							rating = 0; // if(expectedweeks == weeksTook)
							if (expectedweeks > weeksTook) {
								// To simplify, we use <positive value in ]0,
								// 1]> = (1/5) * weeks_Before_Expected
								// if project ended 5 or more weeks before
								// expected,
								// maximum rating is given
								if (expectedweeks - weeksTook >= 5)
									rating = 1;
								else
									rating = (float) 0.2 * (expectedweeks - weeksTook);
							} else if (expectedweeks < weeksTook) {
								// Same as before,only for negative values
								if (weeksTook - expectedweeks >= 5)
									rating = -1;
								else{
									rating = (float) -0.2 * (weeksTook - expectedweeks);
								}
							}
							System.out.println("Calculated weeks: " + expectedweeks + " || weeks Took: " + weeksTook);
							System.out.println("Task " + name + " Rating : " + rating + " Worker: " + w.getNamePrivate());
							//TODO: // Change each worker perceivedvalue on
							// assignedWorkers
							// with FIRE parameters [IT, equation (1)]
							// based on rating (depending on skillSet for this task)
							w.iterateOverFIRE(Task.this,  rating);
							w.assigned = false;
						}
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

	// Some GETTERS for better encapsulation
	public HashSet<Task> getDependencies() {
		return this.dependencies;
	}

	public boolean getFinished() {
		return this.finished;
	}

	public boolean getAvailable() {
		return this.available;
	}

	public int getCost() {
		return this.cost;
	}

	public ArrayList<String> getRequiredSkills(){
		return this.skills;
	}
	// Exception to format, because using "getName()" would need to override
	// Agent class
	public String getNamePrivate() {
		return this.name;
	}

	public int getCriticalCost() {
		return this.criticalCost;
	}

	// SETTERS

	public void setCriticalCost(int cc) {
		this.criticalCost = cc;
	}
}
