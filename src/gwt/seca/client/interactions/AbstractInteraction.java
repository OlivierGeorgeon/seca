package gwt.seca.client.interactions;

import java.util.ArrayList;

public abstract class AbstractInteraction implements IInteraction {
	
	protected String mName;
	protected String mStimulation;
	protected ArrayList<String> mStimulationList;
	
	/**
	 * Enacts the interaction: does the action and saves the stimulation to return.
	 */
	abstract public void enact();
	/**
	 * Returns the name of the interaction.
	 */
	abstract public String getName();
	abstract public int getStimulationID();
	
	public void init() {
		initImpl();
	}
	abstract public void initImpl();
	
	

}
