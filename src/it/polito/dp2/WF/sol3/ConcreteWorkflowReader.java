package it.polito.dp2.WF.sol3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.polito.dp2.WF.ActionReader;
import it.polito.dp2.WF.ProcessActionReader;
import it.polito.dp2.WF.ProcessReader;
import it.polito.dp2.WF.WorkflowMonitor;
import it.polito.dp2.WF.WorkflowReader;
import it.polito.dp2.WF.lab3.gen.Action;
import it.polito.dp2.WF.lab3.gen.Workflow;

/**
 * This is a concrete implementation of the interface {@link WorkflowReader} based on the JAX-WS framework.
 * 
 * @author Luca
 */
public class ConcreteWorkflowReader implements WorkflowReader, Comparable<WorkflowReader> {

	private String name;
	private Map<String, ActionReader> actionReaders;
	private Set<ProcessReader> processes;
	
	public ConcreteWorkflowReader(Workflow workflow) {
		this.actionReaders = new HashMap<String, ActionReader>();
		this.processes = new HashSet<ProcessReader>();		//it must remains empty
		
		if(workflow == null) return;	//safety lock
		
		this.name = workflow.getName();
		
		for( Action action : workflow.getAction() ){
			ActionReader ar;
			
			if( (action.getWorkflow() != null) && (! action.getWorkflow().equals("") ) )
				ar = new ProcessActionR(action, this);
			else
				ar = new SimpleActionR(action, this);
			
			actionReaders.put(ar.getName(), ar);
		}
		
		for( Action action : workflow.getAction() ){
			ActionReader actReader = actionReaders.get(action.getName());
			
			if(actReader instanceof SimpleActionR) {
				List<String> nextActionNames = action.getNextAction();
				
				SimpleActionR sar = (SimpleActionR)actReader;
				sar.setPossibleNextActions(nextActionNames, actionReaders);
			}
		}
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Set<ActionReader> getActions() {
		return new LinkedHashSet<ActionReader>(actionReaders.values());
	}

	@Override
	public ActionReader getAction(String actionName) {
		return actionReaders.get(actionName);
	}

	@Override
	public Set<ProcessReader> getProcesses() {
		return processes;
	}

	@Override
	public int compareTo(WorkflowReader o) {
		return this.name.compareTo(o.getName());
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("Workflow Name: "+name+"\n");
		
		buf.append("\tActions:\n");
		for(ActionReader ar : actionReaders.values()) {
			buf.append("\t\t"+ar.toString()+"\n");
		}
		
		buf.append("\tProcesses:\n");
		if(processes.isEmpty() == false) {
			for(ProcessReader pr : processes) {
				buf.append("\t"+pr.toString()+"\n");
			}
		}
		else {
			buf.append("\t\t No Processes \n");
		}
		
		return buf.toString();
	}
	
	/**
	 * This method set inside each {@link ProcessActionReader} of this {@link WorkflowReader}
	 * the Workflow that will be instantiated after that this action will be completed.
	 *  
	 * @param workflows - All the workflows of the {@link WorkflowMonitor}
	 */
	public void setWfsInsideProcessActions(Map<String, WorkflowReader> workflows) {
		for( ActionReader actReader : actionReaders.values() ) {
			
			if(actReader instanceof ProcessActionR) {
				ProcessActionR par = (ProcessActionR)actReader;
				par.setNextWorkflow(workflows);
			}
		}
	}

}
