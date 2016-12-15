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
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.OneShotBehaviour;
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

public class Worker extends Agent {
	private Context<?> context;
	private Network<Object> net;
	private HashMap<String, Float> skillSet;
	public boolean assigned;
	private String name;
	private ArrayList<RWSV> rwsvList = new ArrayList<RWSV>();
	private ArrayList<SimpleEntry<Task, Float>> pastProjectsRatings = new ArrayList<SimpleEntry<Task, Float>>();

	// Hetero Evaluation.
	boolean heteroDone = true;
	Task currentTask;
	double receivedheteroeval = -1;

	// Constructor
	public Worker(String name, HashMap<String, Float> sset) {
		this.name = name;
		skillSet = sset;
		this.assigned = false;
		currentTask = null;
		Iterator it = sset.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Float> pair = (Map.Entry<String, Float>) it.next();
			RWSV rel = new RWSV(this, pair.getKey(), (float) 0, (float) 1); // Manager
																			// always
																			// assume
																			// 1.0
																			// when
																			// on
																			// a
																			// new
																			// worker.
			rwsvList.add(rel);
		}
	}

	// Gets the last rating on that skill.
	public double getLastRWSV() {
		// if(this.name.equals("Worker 1"))
		// {
		if (rwsvList.size() > 0) {
			return rwsvList.get(rwsvList.size() - 1).getValue();
		}
		// }
		return 0;
	}

	public void setup() {
		DFAgentDescription template = new DFAgentDescription();
		template.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("Worker");
		template.addServices(sd);
		try {
			DFService.register(this, template);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		sendHeteroBehaviour sendHeteroBehaviour = new sendHeteroBehaviour();
		receiveHeteroBehaviour receiveHeteroBehaviour = new receiveHeteroBehaviour();
		addBehaviour(sendHeteroBehaviour);
		addBehaviour(receiveHeteroBehaviour);

	}

	// SEND BEHAVIOUR HERE
	// FOR EACH WORKER IN CURRENT TASK, EVALS WITH RANDOM AND SENDS IT ALL TO
	// MANAGER.
	class sendHeteroBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1837679922616403427L;
		private int n = 0;

		/*
		 * public sendHeteroBehaviour(Agent a) { super(a); }
		 */

		// método action
		@Override
		public void action() {

			if (heteroDone == false) {

				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				String HeteroEvaluation = "";

				// ACLMessage reply = msg.createReply();
				String eval = "";
				for (Worker w : currentTask.assignedWorkers) {
					if (w != (Worker) myAgent) {
						Random randomGenerator = new Random();
						int chance_of_event = randomGenerator.nextInt(100) - 50; // chance_of_event
						// [0..99] - 50 [-50% e 50%]

						double workerValueReal = getRealValue(w, currentTask);
						double evaluation = workerValueReal + (chance_of_event * workerValueReal) / 100;

						eval += w.getNamePrivate() + ":" + evaluation + "|";

						System.out.println("Sent message to Manager _ _ _ _ _ _ ");
						System.out.println("Worker: " + w.getNamePrivate());
						System.out.println("Task: " + currentTask.getNamePrivate());
						System.out.println("workerValueReal: " + workerValueReal);
						System.out.println("evaluation: " + evaluation);
						msg.addReceiver(new AID(w.getNamePrivate() + "@Trust Metric Project", true));
					}
				}

				msg.setContent(eval);
				// Sending to all, but only manager will read.
				send(msg);
				System.out.println("sent!!! " + msg + "END sent message ___________ \n");
				heteroDone = true;
			}
		}
	}

	class receiveHeteroBehaviour extends CyclicBehaviour {
		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				System.out.println("Vindo do Worker: " + msg.getContent());
				String aux = msg.getContent();
				aux = aux.substring(aux.indexOf(":") + 1);
				aux = aux.substring(0, aux.indexOf("|"));

				receivedheteroeval = Double.parseDouble(aux);
				double heteroevaluation = 0;
				// TODO HERE
				if (currentTask.assignedWorkers.size() > 1)
					if (receivedheteroeval != -1) {
						heteroevaluation = receivedheteroeval;
						receivedheteroeval = -1;
					}

				System.out.println("Task " + currentTask.getNamePrivate() + " || " + Worker.this.getNamePrivate()
						+ " || Team Evaluation: " + heteroevaluation);

				for (String skillz : currentTask.getRequiredSkills()) {
					for (RWSV rel : rwsvList) {
						if (rel.getSkill().equals(skillz)) {
							rel.calculateFIREValue(heteroevaluation);
						}
					}
				}
			} else
				block(5);
		}
	}

	public void iterateOverFIRE_IT(Task t, float rating) {

		pastProjectsRatings.add(new SimpleEntry<Task, Float>(t, rating));

		for (String skill : t.getRequiredSkills()) { // Iterate over each Skill

			// Variables necessary for equations
			int n = numberOfRecordsWithGivenSkill(skill);
			float weight = calculateSmallestWeight(n); //

			// Equation 1
			updateIT(skill, weight);

			// Equation 2
			float eq2; // Equation 2 FIRE
			if (n <= 4) // Equation 2 FIRE with m = 4
				eq2 = (float) n / 4;
			else
				eq2 = 1;

			// Equation 3
			float eq3;
			float eq3sum = 0;
			float currentIT = getITForGivenSkill(skill);
			for (int i = 0; i < pastProjectsRatings.size(); i++) { // This
																	// syntax
																	// used to
																	// ensure we
																	// fetch the
																	// older
																	// ones
																	// first
																	// Somatório
																	// in eq3
																	// calculated
																	// in this
																	// for
				SimpleEntry<Task, Float> projrating = pastProjectsRatings.get(i);
				if (projrating.getKey().getRequiredSkills().contains(skill)) {
					eq3sum += (float) weight * Math.abs(projrating.getValue() - currentIT) / 2;
					weight *= 2;
				}
			}
			eq3 = 1 - eq3sum; // End result of eq3

			float eq4 = eq3 * eq2; // Equation 4 of FIRE

			// Store reliability on RWSV of this skill

			storeReliability(skill, eq4);

		}
	}

	public void setTask(Task t) {
		currentTask = t;
	}

	public void iterateOverFIRE_WR(double real_worker_value, Task t) {

		Random randomGenerator = new Random();
		int chance_of_event = randomGenerator.nextInt(100) - 50; // chance_of_event
		// [0..99] - 50 [-50% e 50%]
		double heteroevaluation = 0;
		// TODO HERE
		if (currentTask.assignedWorkers.size() <= 1) {
			heteroevaluation = real_worker_value + (chance_of_event * real_worker_value) / 100;
		}

		System.out.println("Task " + t.getNamePrivate() + " || " + this.getNamePrivate() + " || Team Evaluation: "
				+ heteroevaluation);

		for (String skillz : t.getRequiredSkills()) {
			for (RWSV rel : rwsvList) {
				if (rel.getSkill().equals(skillz)) {
					rel.calculateFIREValue(heteroevaluation);
				}
			}
		}
	}

	// GETTERS
	// Exception to format, because using "getName()" would need to override
	// Agent class
	public String getNamePrivate() {
		return this.name;
	}

	public HashMap<String, Float> getSkillSet() {
		return this.skillSet;
	}

	// Returns supposed skill value (by manager), not real value.
	public float getSkillValue_RWSV(String skill) {
		for (RWSV r : rwsvList) {
			if (r.getSkill().equals(skill))
				return r.getValue();
		}
		return 0; // If not in the skillSet, assume zero skill.
	}

	// UTILITIES

	public float calculateSmallestWeight(int size) {
		float counter = 0;
		for (int i = 0; i < size; i++) {
			counter += Math.pow(2, i);
		}
		return (float) 1.0 / (counter);
	}

	public int numberOfRecordsWithGivenSkill(String skill) {
		int counter = 0;
		for (SimpleEntry<Task, Float> projrating : pastProjectsRatings)
			if (projrating.getKey().getRequiredSkills().contains(skill))
				counter++;
		return counter;
	}

	public float getITForGivenSkill(String skill) {
		for (RWSV rel : rwsvList)
			if (rel.getSkill().equals(skill))
				return rel.getValue();
		return 0;
	}

	public void storeReliability(String skill, float reliability) {
		for (RWSV rel : rwsvList)
			if (rel.getSkill().equals(skill))
				rel.setReliability(reliability);
	}

	public void updateIT(String skill, float weight) {
		// Equation 1
		float thisweight = weight + 0;
		float sum = 0;
		for (int i = 0; i < pastProjectsRatings.size(); i++) {
			SimpleEntry<Task, Float> projrating = pastProjectsRatings.get(i);
			if (projrating.getKey().getRequiredSkills().contains(skill)) {
				sum += (float) projrating.getValue() * thisweight;
				thisweight *= 2;
			}
		}

		for (RWSV rel : rwsvList)
			if (rel.getSkill().equals(skill))
				rel.updateITValue(sum);

	}

	public double getRealValue(Worker w, Task t) {
		double rssize = t.getRequiredSkills().size();
		double sum_ratings = 0;
		/*
		 * for(String skill: t.getRequiredSkills()){ for(int i = 0; i
		 * <w.getSkillSet().size(); i++){
		 * if(w.getSkillSet().containsKey(skill)){ w.getSkillSet() } } }
		 */
		ArrayList<String> skills = t.getRequiredSkills();
		for (String s : w.getSkillSet().keySet()) {
			if (skills.contains(s)) {
				sum_ratings += w.getSkillSet().get(s);
			}
		}
		return (sum_ratings / rssize);
	}
}
