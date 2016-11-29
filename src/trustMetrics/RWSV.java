package trustMetrics;

// Relation Worker/Skill/Value

public class RWSV {  
	private Worker worker;
	private String skill;
	private float value;
	
	RWSV(Worker worker, String skill, float value){
		this.worker = worker;
		this.skill = skill;
		this.value = value;
	}
	
	
	// GETTERS
	Worker getWorker(){
		return worker;
	}
	
	String getSkill(){
		return skill;
	}
	float getValue(){
		return value;
	}
	
	
	
}
