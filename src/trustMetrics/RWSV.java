package trustMetrics;

// Relation Worker/Skill/Value

public class RWSV {  
	private Worker worker;
	private String skill;
	private float value;
	private float reliability;
	
	RWSV(Worker worker, String skill, float value, float reliability){
		this.worker = worker;
		this.skill = skill;
		this.value = value;
		this.reliability = reliability;
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
	
	
	// SETTERS
	
	public void setReliability(float rel){
		this.reliability = rel;
	}
	
	public void updateITValue(float newsum){
		this.value = newsum;
	}
	
}
