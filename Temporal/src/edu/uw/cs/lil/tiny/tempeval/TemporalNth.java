package edu.uw.cs.lil.tiny.tempeval;

public class TemporalNth extends TemporalPredicate{

	
	// Idea: Try to find general approach for finding 
	@Override
	public TemporalISO perform() {
		if (first instanceof TemporalNumber){
			throw new IllegalArgumentException("what the F are we doing here?");
			//return new TemporalDate("quarter", 2);
			
		}
		
		
		// search through first's set fields, if any are >0, do nothing.
		if (checkForLargeDuration())
			return first;
		
		
		if (!first.isConvexSet())
			throw new IllegalArgumentException("The first ISO stored in TemporalNth is not a convex set! (It really should  be.) ");
		if (!(second instanceof TemporalNumber))
			throw new IllegalArgumentException("The second ISO stored in TemporalNth is not a number!");
		TemporalNthOfEach nthOfEach = new TemporalNthOfEach();
		nthOfEach.storeISO(first);
		nthOfEach.storeISO(second);
		nthOfEach.storeISO(getThird());
		return nthOfEach.perform();
	}
	
	private boolean checkForLargeDuration(){
		for (String s : first.getKeys()){
			if (TemporalDate.getValueFromDate(first, s) > 0)
				return true;
		}
		return false;
	}
	
	private TemporalISO getThird(){
		if (first.isSet("quarter"))
			return new TemporalDate("year");
		else if (first.isSet("month"))
			return new TemporalDate("year");
		else if (first.isSet("day"))
			return new TemporalDate("month");
		else if (first.isSet("weekday"))
			return new TemporalDate("week");
		System.out.println("first: " + first);
		System.out.println(first.isConvexSet());
		throw new IllegalArgumentException("Problem with the first stored ISO in TemporalNth!");
	}
}