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
	
	
	//Core Processing
	public void calculateFIREValue(double wr){
		
		//Equation 7
		
		double FIREValue;
		double wr_reliability = 1 - reliability;   // To achieve a total value of 1.
		System.out.println("wr_reliability " + wr_reliability + "  IT  " + value + "reliability normal " + reliability);
		FIREValue = wr_reliability * wr + reliability * value; // Ommited division by 1
		
		
		this.value = (float)FIREValue;
		//Ommit equation 8. We fully trust our own trust
	}
	
}
