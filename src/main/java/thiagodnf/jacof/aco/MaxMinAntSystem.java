package thiagodnf.jacof.aco;

import thiagodnf.jacof.aco.ant.exploration.ASExploration;
import thiagodnf.jacof.aco.ant.initialization.AnAntAtEachVertex;
import thiagodnf.jacof.aco.ant.selection.RouletteWheel;
import thiagodnf.jacof.aco.daemonactions.RestartCheck;
import thiagodnf.jacof.aco.daemonactions.UpdatePheromoneLimits;
import thiagodnf.jacof.aco.graph.initialization.MMASInitialization;
import thiagodnf.jacof.aco.rule.globalupdate.deposit.PartialDeposit;
import thiagodnf.jacof.aco.rule.globalupdate.evaporation.FullEvaporation;
import thiagodnf.jacof.aco.subset.single.GlobalBest;
import thiagodnf.jacof.problem.Problem;

public class MaxMinAntSystem extends ACO {

	protected double rho;

	public MaxMinAntSystem(Problem problem) {
		super(problem);
	}

	public double getRho() {
		return rho;
	}

	public void setRho(double rho) {
		this.rho = rho;
	}

	@Override
	public void build() {
		setTrailInitialization(new MMASInitialization(this));
		setAntInitialization(new AnAntAtEachVertex(this));

		setAntExploration(new ASExploration(this, new RouletteWheel()));

		// Global Update Pheromone Rule
		getEvaporations().add(new FullEvaporation(this, rho));
		getDeposits().add(new PartialDeposit(this, 1.0, new GlobalBest(this)));

		// Daemon Actions
		getDaemonActions().add(new UpdatePheromoneLimits(this, rho, 0.0, 1.0));
		getDaemonActions().add(new RestartCheck(this));
	}
}