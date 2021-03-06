package org.jbpm.console.ng.es.client.editors.servicesettings;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.jbpm.console.ng.es.service.ExecutorServiceEntryPoint;
import org.uberfire.client.annotations.OnReveal;
import org.uberfire.client.annotations.OnStart;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchPopup;
import org.uberfire.client.mvp.UberView;
import org.uberfire.client.workbench.widgets.events.BeforeClosePlaceEvent;
import org.uberfire.shared.mvp.PlaceRequest;

import com.google.gwt.user.client.ui.Focusable;

@Dependent
@WorkbenchPopup(identifier = "Job Service Settings")
public class JobServiceSettingsPresenter {

    public interface JobServiceSettingsView
   			extends
   			UberView<JobServiceSettingsPresenter> {

    	Focusable getNumberOfExecutorsText();
    	
        void displayNotification(String notification);

		void setFrequencyText(String frequency);
		
		void setNumberOfExecutors(Integer numberOfExecutors);
		
        void setStartedLabel(Boolean started);

		void alert(String message);
    }
    @Inject
    JobServiceSettingsView view;
    @Inject
    private Caller<ExecutorServiceEntryPoint> executorServices;
    @Inject
    private Event<BeforeClosePlaceEvent> closePlaceEvent;
    private PlaceRequest place;
    
    @WorkbenchPartTitle
    public String getTitle() {
        return "Job Service Settings";
    }

    @WorkbenchPartView
    public UberView<JobServiceSettingsPresenter> getView() {
        return view;
    }

    public JobServiceSettingsPresenter() {
    }

    @PostConstruct
    public void init() {
    	executorServices.call(new RemoteCallback<Integer>() {
    		@Override
    		public void callback(Integer interval) {
    			view.setFrequencyText(fromIntervalToFrequency(interval));
    		}
    	}).getInterval();
    	executorServices.call(new RemoteCallback<Integer>() {
    		@Override
    		public void callback(Integer threadPoolSize) {
    			view.setNumberOfExecutors(threadPoolSize);
    		}
    	}).getThreadPoolSize();
    	executorServices.call(new RemoteCallback<Boolean>() {
    		public void callback(Boolean started) {
    			view.setStartedLabel(started);
    		}
    	}).isActive();
    }

	public void initService(final Integer numberOfExecutors, String frequency) {
		try {
			Integer interval = fromFrequencyToInterval(frequency);
			executorServices.call(new RemoteCallback<Boolean>() {
				@Override
				public void callback(Boolean serviceStatus) {
					view.displayNotification(serviceStatus ? "Service started" : "Service stopped");
					close();
				}
			}).startStopService(interval, numberOfExecutors);
		} catch (NumberFormatException e) {
			view.alert("Invalid frequency expression: " + frequency);
		}
	}

    @OnReveal
    public void onReveal() {
        view.getNumberOfExecutorsText().setFocus(true);
    }
    
	@OnStart
    public void onStart( final PlaceRequest place ) {
        this.place = place;
    }

	public void close() {
		closePlaceEvent.fire(new BeforeClosePlaceEvent(this.place));
	}

	private String fromIntervalToFrequency(Integer interval) {
		int seconds = interval % 60;
		int minutes = (interval / 60) % 60;
		int hours = (interval / 3600) % 24;
		int days = (interval / 86400);
		StringBuilder frequencyText = new StringBuilder();
		if (days > 0) {
			frequencyText.append(days).append("d ");
		}
		if (hours > 0) {
			frequencyText.append(hours).append("h ");
		}
		if (minutes > 0) {
			frequencyText.append(minutes).append("m ");
		}
		if (seconds > 0) {
			frequencyText.append(seconds).append("s");
		}
		return frequencyText.toString();
	}
	
	private Integer fromFrequencyToInterval(String frequency) throws NumberFormatException {
		String[] sections = frequency.split(" ");
		int interval = 0;
		for (String section : sections) {
			if (section.trim().endsWith("d")) {
				int value = Integer.parseInt(section.replace("d", ""));
				interval += (value * 86400);
			} else if (section.trim().endsWith("h")) {
				int value = Integer.parseInt(section.replace("h", ""));
				interval += (value * 3600);
			} else if (section.trim().endsWith("m")) {
				int value = Integer.parseInt(section.replace("m", ""));
				interval += (value * 60);
			} else if (section.trim().endsWith("s")) {
				int value = Integer.parseInt(section.replace("s", "")); 
				interval += value;
			}
		}
		return interval;
	}
}
