package thiagodnf.jacof.aco;

import java.util.Observable;
import java.util.Observer;

import thiagodnf.jacof.aco.ant.Ant;
import thiagodnf.jacof.aco.ant.daemonactions.AntDaemonActions;
import thiagodnf.jacof.aco.ant.exploration.AntExploration;
import thiagodnf.jacof.aco.ant.globalupdate.AntGlobalUpdate;
import thiagodnf.jacof.aco.ant.globalupdate.evaporation.AbstractEvaporation;
import thiagodnf.jacof.aco.ant.globalupdate.reinforce.AbstractReinforce;
import thiagodnf.jacof.aco.ant.initialization.AntInitialization;
import thiagodnf.jacof.aco.ant.localupdate.AntLocalUpdate;
import thiagodnf.jacof.aco.ant.selection.AntSelection;
import thiagodnf.jacof.aco.graph.AntGraph;
import thiagodnf.jacof.aco.graph.initialization.TrailInitialization;
import thiagodnf.jacof.problem.Problem;

public abstract class ACO implements Observer{
	
	/** Parameter for evaporation */
	public double rho = 0.1;
	
	/** Importance of pheromone*/
	protected double alpha;
	
	/** Importance of heuristic information*/
	protected double beta;
	
	/** The number of ants */
	protected int numberOfAnts;
	
	/** The number of iterations */
	protected int numberOfIterations;
	
	/** Ants **/
	protected Ant[] ants;
	
	/** The pheronome matrix */
	protected AntGraph graph;

	/** The current iteration */
	protected int it = 0;
	
	/** Total of ants that finished your tour */
	protected int finishedAnts = 0;
	
	/** Best Ant in tour */
	protected Ant globalBest;
	
	/** Best Current Ant in tour */
	protected Ant currentBest;
	
	/** The addressed problem */
	protected Problem problem;
	
	protected TrailInitialization trailInitialization;
	
	protected AntInitialization antInitialization;
	
	protected AntSelection antSelection;
	
	protected AntExploration antExploration;
	
	protected AntLocalUpdate antLocalUpdate;
	
	protected AntGlobalUpdate antGlobalUpdate;
	
	protected AntDaemonActions antDaemonActions;
	
	protected AbstractEvaporation evaporation;
	
	protected AbstractReinforce reinforce;
	
	public ACO(Problem problem) {
		this.problem = problem;
	}
	
	public int[] solve() {
		
		build();

		initializePheromones();
		initializeAnts();

		while (!terminationCondition()) {
			constructAntsSolutions();
			updatePheromones();
			daemonActions(); // optional
		}

		return globalBest.getSolution();
	}
	
	protected void initializePheromones() {
		this.graph = new AntGraph(problem);
		this.graph.initialize(trailInitialization);
	}
	
	protected void initializeAnts() {
		
		this.ants = new Ant[numberOfAnts];

		for (int k = 0; k < numberOfAnts; k++) {
			ants[k] = new Ant(this);
			ants[k].setAntInitialization(getAntInitialization());
			ants[k].addObserver(this);
		}
	}
	
	protected boolean terminationCondition() {
		return ++it > numberOfIterations;
	}
	
	private void updatePheromones() {
		evaporation.doEvaporation();
		reinforce.doReinforce();
		
//		for (int i = 0; i < problem.getNumberOfNodes(); i++) {
//			for (int j = i; j < problem.getNumberOfNodes(); j++) {
//				if (i != j) {
//
//					double evaporation = (1.0 - rho) * graph.getTau(i, j);
//					double deposition = antGlobalUpdate.getValue(ants, globalBest, i, j);
//
//					graph.setTau(i, j, evaporation + deposition);
//					graph.setTau(j, i, graph.getTau(i, j));
//				}
//			}
//		}
	}
	
	private synchronized void constructAntsSolutions() {
		
		currentBest = null;

		//Construct the ant solutions by using threads
		for (int k = 0; k < numberOfAnts; k++) {
			Thread t = new Thread(ants[k], "Ant " + ants[k].getId());
			t.start();
		}
		
		//Wait all ants finish your tour
		try{
			wait();
		}catch(InterruptedException ex){
			ex.printStackTrace();
		}	
	}
	
	public void daemonActions() {
		if (antDaemonActions != null) {
			antDaemonActions.doAction();
		}
	}

	public synchronized void update(Observable obj, Object arg) {
		
		Ant ant = (Ant) obj;

		ant.tourLength = problem.evaluate(ant.getSolution());

		if (currentBest == null || problem.better(ant.getSolution(), currentBest.getSolution())) {
			currentBest = ant.clone();
		}
		
		if (globalBest == null || problem.better(ant.getSolution(), globalBest.getSolution())) {
			globalBest = ant.clone();
		}

		if (++finishedAnts == numberOfAnts) {
			// Restart the counter to build the solutions again
			finishedAnts = 0;
			// Continue all execution
			notify();
		}
	}	

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getBeta() {
		return beta;
	}

	public void setBeta(double beta) {
		this.beta = beta;
	}
	
	public double getRho() {
		return rho;
	}

	public void setRho(double rho) {
		this.rho = rho;
	}
	
	public AntGraph getGraph() {
		return graph;
	}

	public void setGraph(AntGraph graph) {
		this.graph = graph;
	}
	
	public int getNumberOfAnts() {
		return numberOfAnts;
	}

	public void setNumberOfAnts(int numberOfAnts) {
		this.numberOfAnts = numberOfAnts;
	}

	public int getNumberOfIterations() {
		return numberOfIterations;
	}

	public void setNumberOfIterations(int numberOfIterations) {
		this.numberOfIterations = numberOfIterations;
	}

	public Problem getProblem() {
		return problem;
	}

	public void setProblem(Problem problem) {
		this.problem = problem;
	}	
	
	public Ant[] getAnts() {
		return ants;
	}

	public void setAnts(Ant[] ants) {
		this.ants = ants;
	}	

	public Ant getGlobalBest() {
		return globalBest;
	}

	public void setGlobalBest(Ant globalBest) {
		this.globalBest = globalBest;
	}

	public Ant getCurrentBest() {
		return currentBest;
	}

	public void setCurrentBest(Ant currentBest) {
		this.currentBest = currentBest;
	}

	public AntInitialization getAntInitialization() {
		return antInitialization;
	}

	public void setAntInitialization(AntInitialization antInitialization) {
		this.antInitialization = antInitialization;
	}
	
	public void setTrailInitialization(TrailInitialization trailInitialization) {
		this.trailInitialization = trailInitialization;
	}
	
	public TrailInitialization getTrailInitialization() {
		return trailInitialization;
	}	

	public AntSelection getAntSelection() {
		return antSelection;
	}

	public void setAntSelection(AntSelection antSelection) {
		this.antSelection = antSelection;
	}
	
	public AntExploration getAntExploration() {
		return antExploration;
	}

	public void setAntExploration(AntExploration antExploration) {
		this.antExploration = antExploration;
	}	

	public AntLocalUpdate getAntLocalUpdate() {
		return antLocalUpdate;
	}

	public void setAntLocalUpdate(AntLocalUpdate antLocalUpdate) {
		this.antLocalUpdate = antLocalUpdate;
	}
		
	public AntGlobalUpdate getAntGlobalUpdate() {
		return antGlobalUpdate;
	}

	public void setAntGlobalUpdate(AntGlobalUpdate antGlobalUpdate) {
		this.antGlobalUpdate = antGlobalUpdate;
	}
	
	public AntDaemonActions getAntDaemonActions() {
		return antDaemonActions;
	}

	public void setAntDaemonActions(AntDaemonActions antDaemonActions) {
		this.antDaemonActions = antDaemonActions;
	}
		
	public AbstractEvaporation getEvaporation() {
		return evaporation;
	}

	public void setEvaporation(AbstractEvaporation evaporation) {
		this.evaporation = evaporation;
	}
	
	public AbstractReinforce getReinforce() {
		return reinforce;
	}

	public void setReinforce(AbstractReinforce reinforce) {
		this.reinforce = reinforce;
	}

	public abstract void build();
	
}
