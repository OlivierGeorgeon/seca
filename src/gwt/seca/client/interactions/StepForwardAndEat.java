package gwt.seca.client.interactions;

public class StepForwardAndEat extends AbstractInteraction {
	
	@Override
	public void enact() {
		// TODO Auto-generated method stub
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStimulationID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void initImpl() {
		// TODO Auto-generated method stub
		mStimulationList.add("Stepped");
		mStimulationList.add("Bumped");
		mStimulationList.add("Eaten");
	}

}
