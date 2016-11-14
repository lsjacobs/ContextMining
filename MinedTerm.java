
public class MinedTerm {
	
	private String term;
	private String hitsNoContext;
	private String context;
    private String frequencyInText;
    private double relation;
    
	public MinedTerm() {
		// TODO Auto-generated constructor stub
	}

    public MinedTerm(String context, String term, String frequencyInText, String hitsNoContext, float relation) {
    	this.term = term;
    	this.hitsNoContext = hitsNoContext;
        this.context = context;
        this.frequencyInText = frequencyInText;
        this.relation = relation;
    }

    public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}
    
	public String getHitsNoContext() {
		return hitsNoContext;
	}

	public void setHitsNoContext(String hitsNoContext) {
		this.hitsNoContext = hitsNoContext;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
	
	public String getFrequencyInText() {
		return frequencyInText;
	}

	public void setFrequencyInText(String frequencyInText) {
		this.frequencyInText = frequencyInText;
	}

	public double getRelation() {
		return relation;
	}

	public void setRelation(double relation) {
		this.relation = relation;
	}
}
