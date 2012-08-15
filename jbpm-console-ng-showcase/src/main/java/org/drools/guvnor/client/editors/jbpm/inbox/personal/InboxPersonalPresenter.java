/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.guvnor.client.editors.jbpm.inbox.personal;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import org.jboss.bpm.console.client.model.TaskSummary;
import org.jboss.bpm.console.shared.TaskServiceEntryPoint;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.workbench.widgets.events.NotificationEvent;

@Dependent
@WorkbenchScreen(identifier = "Personal Tasks")
public class InboxPersonalPresenter {

    
    public interface InboxView
            extends
            IsWidget {
        
        void displayNotification(String text);
    }
    @Inject
    InboxView view;
    @Inject
    Caller<TaskServiceEntryPoint> taskServices;
    private ListDataProvider<TaskSummary> dataProvider = new ListDataProvider<TaskSummary>();

    @WorkbenchPartTitle
    public String getTitle() {
        return "Personal Tasks";
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return view;
    }

    public InboxPersonalPresenter() {
    }

    @PostConstruct
    public void init() {
        
    }

   

    public void refreshTasks(String userId) {
        taskServices.call(new RemoteCallback<List<TaskSummary>>() {
            @Override
            public void callback(List<TaskSummary> tasks) {
                
                dataProvider.setList(tasks);
                dataProvider.refresh();

            }
        }).getTasksOwned(userId);
        
    }

    public void startTasks(final Set<TaskSummary> selectedTasks, final String userId) {
        for (TaskSummary ts : selectedTasks) {
            taskServices.call(new RemoteCallback<List<TaskSummary>>() {
                @Override
                public void callback(List<TaskSummary> tasks) {
                    view.displayNotification("Task(s) Started");
                    refreshTasks(userId);
                }
            }).start(ts.getId(), userId);
        }
        

    }
    
    public void releaseTasks(Set<TaskSummary> selectedTasks, final String userId) {
        for (TaskSummary ts : selectedTasks) {
            taskServices.call(new RemoteCallback<List<TaskSummary>>() {
                @Override
                public void callback(List<TaskSummary> tasks) {
                    view.displayNotification("Task(s) Released");
                    refreshTasks(userId);
                }
            }).release(ts.getId(), userId);
        }
    }


   

    public void completeTasks(Set<TaskSummary> selectedTasks, final String userId) {
        for (TaskSummary ts : selectedTasks) {
            taskServices.call(new RemoteCallback<List<TaskSummary>>() {
                @Override
                public void callback(List<TaskSummary> tasks) {
                    view.displayNotification("Task(s) Completed");
                    refreshTasks(userId);
                }
            }).complete(ts.getId(), userId, null);
        }
        
    }

    public void addDataDisplay(HasData<TaskSummary> display) {
        dataProvider.addDataDisplay(display);
    }

    public ListDataProvider<TaskSummary> getDataProvider() {
        return dataProvider;
    }

    public void refreshData() {
        dataProvider.refresh();
    }
}