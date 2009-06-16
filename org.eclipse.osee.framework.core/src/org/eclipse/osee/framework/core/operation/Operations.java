/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Roberto E. Escobar
 */
public class Operations {

   private Operations() {
   }

   /**
    * @param workPercentage
    * @return amount from total work
    */
   public static int calculateWork(int totalWork, double workPercentage) {
      return (int) (totalWork * workPercentage);
   }

   /**
    * Checks to see if the user canceled the operation. If the operation was canceled, the method will throw an
    * OperationCanceledException
    * 
    * @param monitor
    * @throws OperationCanceledException
    */
   protected void checkForCancelledStatus(IProgressMonitor monitor, IStatus status) throws OperationCanceledException {
      if (monitor.isCanceled()) {
         boolean wasCancelled = false;
         IStatus[] children = status.getChildren();
         for (int i = 0; i < children.length; i++) {
            Throwable exception = children[i].getException();
            if (exception instanceof OperationCanceledException) {
               wasCancelled = true;
               break;
            }
         }
         if (!wasCancelled) {
            throw new OperationCanceledException();
         }
      }
   }

   /**
    * Checks to see if the status has errors. If the status contains errors, an exception will be thrown.
    * 
    * @param monitor
    * @throws Exception
    * @see {@link IStatus#matches(int)}
    */
   public static void checkForStatusSeverityMask(IStatus status, int severityMask) throws Exception {
      if ((severityMask & IStatus.CANCEL) != 0 && status.getSeverity() == IStatus.CANCEL) {
         throw new OperationCanceledException();
      } else if (status.matches(severityMask)) {
         List<StackTraceElement> traceElements = new ArrayList<StackTraceElement>();
         String message = status.getMessage();
         for (IStatus childStatus : status.getChildren()) {
            Throwable exception = childStatus.getException();
            String childMessage = childStatus.getMessage();
            if (Strings.isValid(childMessage)) {
               message = childMessage;
            }
            if (exception != null) {
               traceElements.addAll(Arrays.asList(exception.getStackTrace()));
            }
         }

         Exception ex = new Exception(message);
         if (!traceElements.isEmpty()) {
            ex.setStackTrace(traceElements.toArray(new StackTraceElement[traceElements.size()]));
         }
         throw ex;
      }
   }

   /**
    * Checks to see if the status has errors. If the status contains errors, an exception will be thrown.
    * 
    * @param monitor
    * @throws Exception
    * @see {@link IStatus#matches(int)}
    */
   public static void checkForErrorStatus(IStatus status) throws Exception {
      checkForStatusSeverityMask(status, IStatus.CANCEL | IStatus.ERROR | IStatus.WARNING);
   }

   /**
    * Executes an operation calling the monitor begin and done methods. If workPercentage is set greater than 0, monitor
    * will be wrapped into a SubProgressMonitor set to the appropriate number of ticks to consume from the main monitor.
    * 
    * @param operation
    * @param monitor
    * @param workPercentage
    */
   public static void executeWork(IOperation operation, IProgressMonitor monitor, double workPercentage) {
      if (workPercentage > 0) {
         monitor = new SubProgressMonitor(monitor, calculateWork(operation.getTotalWorkUnits(), workPercentage));
      }
      monitor.beginTask(operation.getName(), operation.getTotalWorkUnits());
      try {
         operation.run(monitor);
      } finally {
         monitor.subTask("");
         monitor.setTaskName("");
         monitor.done();
      }
   }

   public static Job executeAsJob(IOperation operation, boolean user) {
      return scheduleJob(new OperationJob(operation), user, Job.LONG, null);
   }

   public static Job scheduleJob(Job job, boolean user, int priority) {
      return scheduleJob(job, user, priority, null);
   }

   public static Job scheduleJob(Job job, boolean user, int priority, IJobChangeListener jobChangeListener) {
      job.setUser(user);
      job.setPriority(priority);
      if (jobChangeListener != null) {
         job.addJobChangeListener(jobChangeListener);
      }
      job.schedule();
      return job;
   }
}
