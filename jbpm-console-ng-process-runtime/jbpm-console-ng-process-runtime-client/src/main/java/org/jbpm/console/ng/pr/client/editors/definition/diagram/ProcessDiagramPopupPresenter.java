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
package org.jbpm.console.ng.pr.client.editors.definition.diagram;

import java.util.List;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.jbpm.console.ng.bd.service.DataServiceEntryPoint;

import org.jbpm.console.ng.pr.model.NodeInstanceSummary;
import org.uberfire.client.annotations.OnReveal;
import org.uberfire.client.annotations.OnStart;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchPopup;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.client.workbench.widgets.events.BeforeClosePlaceEvent;
import org.uberfire.security.Identity;
import org.uberfire.shared.mvp.PlaceRequest;

@Dependent
@WorkbenchPopup(identifier = "Process Diagram Popup")
public class ProcessDiagramPopupPresenter {

    public interface InboxView
            extends
            UberView<ProcessDiagramPopupPresenter> {

        void displayNotification(String text);

        TextBox getProcessDefIdText();

        TextBox getProcessDiagramURLText();

        Button getGenerateUrlButton();
        
        TextBox getProcessInstanceIdText();
        
        TextBox getProcessPackageNameText();

        TextBox getProcessVersionText();
    }
    @Inject
    private PlaceManager placeManager;
    @Inject
    InboxView view;
    @Inject
    Identity identity;
    @Inject
    private Caller<DataServiceEntryPoint> dataServices;
    
    @Inject
    private Event<BeforeClosePlaceEvent> closePlaceEvent;
    private PlaceRequest place;

    @OnStart
    public void onStart(final PlaceRequest place) {
        this.place = place;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Process Diagram Popup";
    }

    @WorkbenchPartView
    public UberView<ProcessDiagramPopupPresenter> getView() {
        return view;
    }

    public void generateURL(final String processDefinitionId, final Long processInstanceId, 
                            final String packageName, final String version) {
        dataServices.call( new RemoteCallback<List<NodeInstanceSummary>>() {
            @Override
            public void callback( List<NodeInstanceSummary> details ) {
                String fullLog = "?processDefId="+processDefinitionId+""
                         + "?processPackage="+packageName
                        + "?processVersion="+version
                        + "?nodeIds=";
                for ( NodeInstanceSummary nis : details ) {
                    fullLog +=  nis.getNodeUniqueName()+ ",";
                }
                view.getProcessDiagramURLText().setText( fullLog );
            }
        } ).getProcessInstanceHistory( processInstanceId );

    }

    @OnReveal
    public void onReveal() {
        String processDefinitionId = place.getParameter("processDefId", "").toString();
        Long processInstanceId = Long.parseLong( place.getParameter("processInstanceId", "0").toString());
        String packageName = place.getParameter("processPackage", "").toString();
        String version = place.getParameter("processVersion", "0").toString();
        view.getProcessDefIdText().setText(processDefinitionId);
        view.getProcessInstanceIdText().setText(processInstanceId.toString());
        view.getProcessPackageNameText().setText(packageName);
        view.getProcessVersionText().setText(version);
        generateURL(processDefinitionId, processInstanceId, packageName, version);
    }

    public void close() {
        closePlaceEvent.fire(new BeforeClosePlaceEvent(this.place));
    }
}
