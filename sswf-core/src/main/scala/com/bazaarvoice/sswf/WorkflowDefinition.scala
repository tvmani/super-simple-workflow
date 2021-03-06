package com.bazaarvoice.sswf

import com.amazonaws.services.simpleworkflow.model.WorkflowExecution
import com.bazaarvoice.sswf.model.history.StepsHistory
import com.bazaarvoice.sswf.model.result.StepResult
import com.bazaarvoice.sswf.model.{ScheduledStep, StepInput}

/**
  * This is where you specify how the workflow executes.
  *
  * @tparam SSWFInput The JVM object representing your workflow input.
  * @tparam StepEnum  The enum containing workflow step definitions
  */
trait WorkflowDefinition[SSWFInput, StepEnum <: (Enum[StepEnum] with WorkflowStep)] {
  /**
    * Simply return a list of the workflow steps to execute.
    *
    * @param input The input to the workflow
    */
  def workflow(input: SSWFInput): java.util.List[ScheduledStep[StepEnum]]

  /**
    * Run whatever behaviour needs to be run for the given step.
    * There is a recommended strategy for writing these things: see https://github.com/bazaarvoice/super-simple-workflow/blob/master/README.md#writing-steps
    *
    * @param step              The action to take next
    * @param input             The input to the workflow
    * @param stepInput         The input to this particular step
    * @param heartbeatCallback A function to call to report liveness and progress. Calling this method resets the timeout for the step. Response `true` if cancellation is requested.
    * @return The outcome of the execution.
    */
  def act(step: StepEnum, input: SSWFInput, stepInput: StepInput, heartbeatCallback: HeartbeatCallback, execution: WorkflowExecution): StepResult

  /**
    * A hook that gets called when the workflow fails. Use this for example to send
    * a notification with an explanation of what happened.
    *
    * @param input   The workflow input
    * @param history The history of what happened in the workflow execution.
    * @param message A summary of the failure. This is what we also send to SWF as the workflow "result".
    */
  def onFail(workflowId: String, runId: String, input: SSWFInput, history: StepsHistory[SSWFInput, StepEnum], message: String): Unit


  /**
    * A hook that gets called when the workflow completes successfully. Use this for example to send
    * a notification that the workflow is complete.
    *
    * @param input   The workflow input
    * @param history The history of what happened in the workflow execution.
    * @param message A description of the workflow result. This is what we also send to SWF as the workflow "result".
    */
  def onFinish(workflowId: String, runId: String, input: SSWFInput, history: StepsHistory[SSWFInput, StepEnum], message: String): Unit

  /**
    * A hook that gets called when the workflow is cancelled. Use this for example to send
    * a notification of the cancellation and to clean up any external state.
    *
    * @param input   The workflow input
    * @param history The history of what happened in the workflow execution.
    * @param message A description of the workflow result. This is what we also send to SWF as the workflow "result".
    */
  def onCancel(workflowId: String, runId: String, input: SSWFInput, history: StepsHistory[SSWFInput, StepEnum], message: String): Unit
}
