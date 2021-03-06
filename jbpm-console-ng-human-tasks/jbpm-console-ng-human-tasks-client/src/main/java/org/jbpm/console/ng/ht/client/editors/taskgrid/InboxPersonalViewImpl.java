package org.jbpm.console.ng.ht.client.editors.taskgrid;


import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.DataGrid;
import com.github.gwtbootstrap.client.ui.SimplePager;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.console.ng.ht.client.i8n.Constants;
import org.jbpm.console.ng.ht.client.util.ResizableHeader;
import org.jbpm.console.ng.ht.model.TaskSummary;
import org.jbpm.console.ng.ht.model.events.TaskSelectionEvent;
import org.jbpm.console.ng.ht.model.events.UserTaskEvent;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.widgets.events.NotificationEvent;
import org.uberfire.security.Identity;
import org.uberfire.shared.mvp.PlaceRequest;
import org.uberfire.shared.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import org.jbpm.console.ng.ht.client.resources.ShowcaseImages;

@Dependent
@Templated(value = "InboxPersonalViewImpl.html")
public class InboxPersonalViewImpl extends Composite
        implements
        InboxPersonalPresenter.InboxView {

    @Inject
    private Identity identity;
    @Inject
    private PlaceManager placeManager;
    private InboxPersonalPresenter presenter;
    @Inject
    @DataField
    public Button refreshTasksButton;
    @Inject
    @DataField
    public Button startTaskButton;
    @Inject
    @DataField
    public Button completeTaskButton;
    @Inject
    @DataField
    public Button quickTaskButton;
    @Inject
    @DataField
    public DataGrid<TaskSummary> myTaskListGrid;
    @Inject
    @DataField
    public SimplePager pager;
    @Inject
    @DataField
    public FlowPanel listContainer;
    @Inject
    @DataField
    public CheckBox showCompletedCheck;
    @Inject
    @DataField
    public CheckBox showPersonalTasksCheck;
    @Inject
    @DataField
    public CheckBox showGroupTasksCheck;
    private Set<TaskSummary> selectedTasks;
    @Inject
    private Event<NotificationEvent> notification;
    @Inject
    private Event<TaskSelectionEvent> taskSelection;
    private ListHandler<TaskSummary> sortHandler;
    private MultiSelectionModel<TaskSummary> selectionModel;
    private Constants constants = GWT.create(Constants.class);
    private ShowcaseImages images = GWT.create(ShowcaseImages.class);

    @Override
    public void init(InboxPersonalPresenter presenter) {
        this.presenter = presenter;

        listContainer.add(myTaskListGrid);
        listContainer.add(pager);
        
        myTaskListGrid.setHeight("350px");
        // Set the message to display when the table is empty.
        myTaskListGrid.setEmptyTableWidget(new Label(constants.Hooray_you_don_t_have_any_pending_Task__()));

        // Attach a column sort handler to the ListDataProvider to sort the list.
        sortHandler =
                new ListHandler<TaskSummary>(presenter.getDataProvider().getList());

        myTaskListGrid.addColumnSortHandler(sortHandler);

        // Create a Pager to control the table.

        pager.setDisplay(myTaskListGrid);
        pager.setPageSize(30);

        // Add a selection model so we can select cells.
        selectionModel =
                new MultiSelectionModel<TaskSummary>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                selectedTasks = selectionModel.getSelectedSet();
                for (TaskSummary ts : selectedTasks) {
                    taskSelection.fire(new TaskSelectionEvent(ts.getId()));
                }
            }
        });

        
        myTaskListGrid.setSelectionModel(selectionModel,
                DefaultSelectionEventManager
                .<TaskSummary>createCheckboxManager());

        initTableColumns(selectionModel);
        presenter.addDataDisplay(myTaskListGrid);
        
         KeyPressHandler refreshPressHandler = new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
//                System.out.println("event.getUnicodeCharCode() -> "+event.getUnicodeCharCode());
//                System.out.println("event.getNativeEvent().getKeyCode() = "+event.getNativeEvent().getKeyCode());
                if(event.isControlKeyDown() && event.getUnicodeCharCode() == 114 ){
                   refreshTasks();
                }
                
                
            }
        };
        
        
        refreshTasksButton.addDomHandler(refreshPressHandler, KeyPressEvent.getType());
        
        refreshTasks();

    }

    public void recieveStatusChanged(@Observes UserTaskEvent event) {
        refreshTasks();

    }

    @EventHandler("refreshTasksButton")
    public void refreshTasksButton(ClickEvent e) {
        refreshTasks();
    }

    @EventHandler("startTaskButton")
    public void startTaskButton(ClickEvent e) {
        if (selectedTasks.isEmpty()) {
            displayNotification(constants.Please_Select_at_least_one_Task_to_Execute_a_Quick_Action());
            return;
        }
        presenter.startTasks(selectedTasks,
                identity.getName());
    }

    @EventHandler("quickTaskButton")
    public void quickTaskButton(ClickEvent e) {
        PlaceRequest placeRequestImpl = new DefaultPlaceRequest("Quick New Task");
        placeManager.goTo(placeRequestImpl);
    }

    @EventHandler("completeTaskButton")
    public void completeTaskButton(ClickEvent e) {
        if (selectedTasks.isEmpty()) {
            displayNotification(constants.Please_Select_at_least_one_Task_to_Execute_a_Quick_Action());
            return;
        }
        presenter.completeTasks(selectedTasks,
                identity.getName());

    }

    private void initTableColumns(final SelectionModel<TaskSummary> selectionModel) {
        // Checkbox column. This table will uses a checkbox column for selection.
        // Alternatively, you can call dataGrid.setSelectionEnabled(true) to enable
        // mouse selection.

        Column<TaskSummary, Boolean> checkColumn =
                new Column<TaskSummary, Boolean>(new CheckboxCell(true,
                false)) {
            @Override
            public Boolean getValue(TaskSummary object) {
                // Get the value from the selection model.
                return selectionModel.isSelected(object);
            }
        };
        myTaskListGrid.addColumn(checkColumn,
                SafeHtmlUtils.fromSafeConstant("<br/>"));
        myTaskListGrid.setColumnWidth(checkColumn, "40px");

        // Id 
        Column<TaskSummary, Number> taskIdColumn =
                new Column<TaskSummary, Number>(new NumberCell()) {
            @Override
            public Number getValue(TaskSummary object) {
                return object.getId();
            }
        };
        taskIdColumn.setSortable(true);
        myTaskListGrid.setColumnWidth(taskIdColumn, "40px");

        myTaskListGrid.addColumn(taskIdColumn,
                new ResizableHeader(constants.Id(), myTaskListGrid, taskIdColumn));
        sortHandler.setComparator(taskIdColumn,
                new Comparator<TaskSummary>() {
            public int compare(TaskSummary o1,
                    TaskSummary o2) {
                return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
            }
        });

        // Task name.
        Column<TaskSummary, String> taskNameColumn =
                new Column<TaskSummary, String>(new TextCell()) {
            @Override
            public String getValue(TaskSummary object) {
                return object.getName();
            }
        };
        taskNameColumn.setSortable(true);

        myTaskListGrid.addColumn(taskNameColumn,
                new ResizableHeader(constants.Task(), myTaskListGrid, taskNameColumn));
        sortHandler.setComparator(taskNameColumn,
                new Comparator<TaskSummary>() {
            public int compare(TaskSummary o1,
                    TaskSummary o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        // Task priority.
        Column<TaskSummary, Number> taskPriorityColumn =
                new Column<TaskSummary, Number>(new NumberCell()) {
            @Override
            public Number getValue(TaskSummary object) {
                return object.getPriority();
            }
        };
        taskPriorityColumn.setSortable(true);
        taskPriorityColumn.setSortable(true);
        myTaskListGrid.addColumn(taskPriorityColumn,
                new ResizableHeader(constants.Priority(), myTaskListGrid, taskPriorityColumn));
        myTaskListGrid.setColumnWidth(taskPriorityColumn, "100px");
        sortHandler.setComparator(taskPriorityColumn,
                new Comparator<TaskSummary>() {
            public int compare(TaskSummary o1,
                    TaskSummary o2) {
                return Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
            }
        });
        // Status.
        Column<TaskSummary, String> statusColumn = new Column<TaskSummary, String>(new TextCell()) {
            @Override
            public String getValue(TaskSummary object) {
                return object.getStatus();
            }
        };
        statusColumn.setSortable(true);


        myTaskListGrid.addColumn(statusColumn,
                new ResizableHeader(constants.Status(), myTaskListGrid, statusColumn));
        sortHandler.setComparator(statusColumn,
                new Comparator<TaskSummary>() {
            public int compare(TaskSummary o1,
                    TaskSummary o2) {
                return o1.getStatus().compareTo(o2.getStatus());
            }
        });
        myTaskListGrid.setColumnWidth(statusColumn, "100px");

        // Due Date.
        Column<TaskSummary, String> dueDateColumn = new Column<TaskSummary, String>(new TextCell()) {
            @Override
            public String getValue(TaskSummary object) {
                if (object.getExpirationTime() != null) {
                    return object.getExpirationTime().toString();
                }
                return "";
            }
        };
        dueDateColumn.setSortable(true);

        myTaskListGrid.addColumn(dueDateColumn,
                new ResizableHeader(constants.Due_On(), myTaskListGrid, dueDateColumn));
        sortHandler.setComparator(dueDateColumn,
                new Comparator<TaskSummary>() {
            public int compare(TaskSummary o1,
                    TaskSummary o2) {
                if (o1.getExpirationTime() == null || o2.getExpirationTime() == null) {
                    return 0;
                }
                return o1.getExpirationTime().compareTo(o2.getExpirationTime());
            }
        });


        // Task parent id.
        Column<TaskSummary, String> taskParentIdColumn =
                new Column<TaskSummary, String>(new TextCell()) {
            @Override
            public String getValue(TaskSummary object) {
                return (object.getParentId() > 0) ? String.valueOf(object.getParentId()) : constants.No_Parent();
            }
        };
        taskParentIdColumn.setSortable(true);

        myTaskListGrid.addColumn(taskParentIdColumn,
                new ResizableHeader(constants.Parent(), myTaskListGrid, taskParentIdColumn));
        myTaskListGrid.setColumnWidth(taskParentIdColumn, "100px");
        sortHandler.setComparator(taskParentIdColumn,
                new Comparator<TaskSummary>() {
            public int compare(TaskSummary o1,
                    TaskSummary o2) {
                return Integer.valueOf(o1.getParentId()).compareTo(o2.getParentId());
            }
        });


        List<HasCell<TaskSummary, ?>> cells = new LinkedList<HasCell<TaskSummary, ?>>();
        cells.add(new StartActionHasCell("Start", new Delegate<TaskSummary>() {
            @Override
            public void execute(TaskSummary task) {
                Set<TaskSummary> tasks = new HashSet<TaskSummary>(1);
                tasks.add(task);
                presenter.startTasks(tasks, identity.getName());
            }
        }));


        cells.add(new CompleteActionHasCell("Complete", new Delegate<TaskSummary>() {
            @Override
            public void execute(TaskSummary task) {
                Set<TaskSummary> tasks = new HashSet<TaskSummary>(1);
                tasks.add(task);
                presenter.completeTasks(tasks, identity.getName());
            }
        }));



        cells.add(new ClaimActionHasCell("Claim", new Delegate<TaskSummary>() {
            @Override
            public void execute(TaskSummary task) {
                Set<TaskSummary> tasks = new HashSet<TaskSummary>(1);
                tasks.add(task);
                presenter.claimTasks(tasks, identity.getName());
            }
        }));

        cells.add(new ReleaseActionHasCell("Release", new Delegate<TaskSummary>() {
            @Override
            public void execute(TaskSummary task) {
                Set<TaskSummary> tasks = new HashSet<TaskSummary>(1);
                tasks.add(task);
                presenter.releaseTasks(tasks, identity.getName());
            }
        }));


        cells.add(new EditHasCell("Edit", new Delegate<TaskSummary>() {
            @Override
            public void execute(TaskSummary task) {
                PlaceRequest placeRequestImpl = new DefaultPlaceRequest("Task Details Popup");
                placeRequestImpl.addParameter("taskId", Long.toString(task.getId()));
                placeManager.goTo(placeRequestImpl);
            }
        }));
        
        cells.add(new PopupActionHasCell("Work Popup", new Delegate<TaskSummary>() {
            @Override
            public void execute(TaskSummary task) {
                PlaceRequest placeRequestImpl = new DefaultPlaceRequest("Form Display");
                placeRequestImpl.addParameter("taskId", Long.toString(task.getId()));

                placeManager.goTo(placeRequestImpl);
            }
        }));

        CompositeCell<TaskSummary> cell = new CompositeCell<TaskSummary>(cells);
        Column<TaskSummary, TaskSummary> actionsColumn = new Column<TaskSummary, TaskSummary>(cell) {
                                                  @Override
                                                  public TaskSummary getValue(TaskSummary object) {
                                                      return object;
                                                  }
                                              };
        myTaskListGrid.addColumn(actionsColumn, "Actions");



    }

    public void displayNotification(String text) {
        notification.fire(new NotificationEvent(text));
    }



    public CheckBox getShowCompletedCheck() {
        return showCompletedCheck;
    }

    public DataGrid<TaskSummary> getDataGrid() {
        return myTaskListGrid;
    }

    public ListHandler<TaskSummary> getSortHandler() {
        return sortHandler;
    }

    public MultiSelectionModel<TaskSummary> getSelectionModel() {
        return selectionModel;
    }

    public CheckBox getShowGroupTasksCheck() {
        return showGroupTasksCheck;
    }

    public void refreshTasks() {
        Boolean isCheckedCompleted = showCompletedCheck.getValue();
        Boolean isCheckedGroupTasks = showGroupTasksCheck.getValue();
        Boolean isCheckedPersonalTasks = showPersonalTasksCheck.getValue();
        presenter.refreshTasks(identity.getName(), isCheckedPersonalTasks,
                isCheckedCompleted, isCheckedGroupTasks);
    }

    private class EditHasCell implements HasCell<TaskSummary, TaskSummary> {

        private ActionCell<TaskSummary> cell;

        public EditHasCell(String text, Delegate<TaskSummary> delegate) {
            cell = new ActionCell<TaskSummary>(text, delegate) {
                @Override
                public void render(Context context, TaskSummary value, SafeHtmlBuilder sb) {

                    ImageResource editIcon = images.editIcon();
                    AbstractImagePrototype imageProto = AbstractImagePrototype.create(editIcon);
                    SafeHtmlBuilder mysb = new SafeHtmlBuilder();
                    mysb.appendHtmlConstant("<span title='Details'>");
                    mysb.append(imageProto.getSafeHtml());
                    mysb.appendHtmlConstant("</span>");
                    sb.append(mysb.toSafeHtml());
                }
            };

        }

        @Override
        public Cell<TaskSummary> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<TaskSummary, TaskSummary> getFieldUpdater() {
            return null;
        }

        @Override
        public TaskSummary getValue(TaskSummary object) {
            return object;
        }
    }

   

    private class StartActionHasCell implements HasCell<TaskSummary, TaskSummary> {

        private ActionCell<TaskSummary> cell;

        public StartActionHasCell(String text, Delegate<TaskSummary> delegate) {
            cell = new ActionCell<TaskSummary>(text, delegate) {
                @Override
                public void render(Cell.Context context, TaskSummary value, SafeHtmlBuilder sb) {
                    if (value.getActualOwner() != null && (value.getStatus().equals("Reserved"))) {
                        AbstractImagePrototype imageProto = AbstractImagePrototype.create(images.startIcon());
                        SafeHtmlBuilder mysb = new SafeHtmlBuilder();
                        mysb.appendHtmlConstant("<span title='Start'>");
                        mysb.append(imageProto.getSafeHtml());
                        mysb.appendHtmlConstant("</span>");
                        sb.append(mysb.toSafeHtml());

                    }
                }
            };
        }

        @Override
        public Cell<TaskSummary> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<TaskSummary, TaskSummary> getFieldUpdater() {
            return null;
        }

        @Override
        public TaskSummary getValue(TaskSummary object) {
            return object;
        }
    }

    private class CompleteActionHasCell implements HasCell<TaskSummary, TaskSummary> {

        private ActionCell<TaskSummary> cell;

        public CompleteActionHasCell(String text, Delegate<TaskSummary> delegate) {
            cell = new ActionCell<TaskSummary>(text, delegate) {
                @Override
                public void render(Cell.Context context, TaskSummary value, SafeHtmlBuilder sb) {
                    if (value.getActualOwner() != null && value.getStatus().equals("InProgress")) { 
                        AbstractImagePrototype imageProto = AbstractImagePrototype.create(images.completeIcon());
                        SafeHtmlBuilder mysb = new SafeHtmlBuilder();
                        mysb.appendHtmlConstant("<span title='Complete'>");
                        mysb.append(imageProto.getSafeHtml());
                        mysb.appendHtmlConstant("</span>");
                        sb.append(mysb.toSafeHtml());

                    }
                }
            };
        }

        @Override
        public Cell<TaskSummary> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<TaskSummary, TaskSummary> getFieldUpdater() {
            return null;
        }

        @Override
        public TaskSummary getValue(TaskSummary object) {
            return object;
        }
    }

    private class PopupActionHasCell implements HasCell<TaskSummary, TaskSummary> {

        private ActionCell<TaskSummary> cell;

        public PopupActionHasCell(String text, Delegate<TaskSummary> delegate) {
            cell = new ActionCell<TaskSummary>(text, delegate) {
                @Override
                public void render(Cell.Context context, TaskSummary value, SafeHtmlBuilder sb) {
                    if (value.getActualOwner() != null && (value.getStatus().equals("Reserved") || value.getStatus().equals("InProgress"))) {
                        AbstractImagePrototype imageProto = AbstractImagePrototype.create(images.popupIcon());
                        SafeHtmlBuilder mysb = new SafeHtmlBuilder();
                        mysb.appendHtmlConstant("<span title='Work'>");
                        mysb.append(imageProto.getSafeHtml());
                        mysb.appendHtmlConstant("</span>");
                        sb.append(mysb.toSafeHtml());

                    }
                }
            };
        }

        @Override
        public Cell<TaskSummary> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<TaskSummary, TaskSummary> getFieldUpdater() {
            return null;
        }

        @Override
        public TaskSummary getValue(TaskSummary object) {
            return object;
        }
    }

    private class ClaimActionHasCell implements HasCell<TaskSummary, TaskSummary> {

        private ActionCell<TaskSummary> cell;

        public ClaimActionHasCell(String text, Delegate<TaskSummary> delegate) {
            cell = new ActionCell<TaskSummary>(text, delegate) {
                @Override
                public void render(Cell.Context context, TaskSummary value, SafeHtmlBuilder sb) {
                    if (value.getPotentialOwners() != null && !value.getPotentialOwners().isEmpty() && value.getStatus().equals("Ready")) {
                        AbstractImagePrototype imageProto = AbstractImagePrototype.create(images.unlockIcon());
                        SafeHtmlBuilder mysb = new SafeHtmlBuilder();
                        mysb.appendHtmlConstant("<span title='Claim'>");
                        mysb.append(imageProto.getSafeHtml());
                        mysb.appendHtmlConstant("</span>");
                        sb.append(mysb.toSafeHtml());


                    }
                }
            };
        }

        @Override
        public Cell<TaskSummary> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<TaskSummary, TaskSummary> getFieldUpdater() {
            return null;
        }

        @Override
        public TaskSummary getValue(TaskSummary object) {
            return object;
        }
    }

    private class ReleaseActionHasCell implements HasCell<TaskSummary, TaskSummary> {

        private ActionCell<TaskSummary> cell;

        public ReleaseActionHasCell(String text, Delegate<TaskSummary> delegate) {
            cell = new ActionCell<TaskSummary>(text, delegate) {
                @Override
                public void render(Cell.Context context, TaskSummary value, SafeHtmlBuilder sb) {
                    if (value.getPotentialOwners() != null && !value.getPotentialOwners().isEmpty() && !value.getPotentialOwners().contains(identity.getName()) && value.getStatus().equals("Reserved")) {
                        AbstractImagePrototype imageProto = AbstractImagePrototype.create(images.lockIcon());
                        SafeHtmlBuilder mysb = new SafeHtmlBuilder();
                        mysb.appendHtmlConstant("<span title='Release'>");
                        mysb.append(imageProto.getSafeHtml());
                        mysb.appendHtmlConstant("</span>");
                        sb.append(mysb.toSafeHtml());

                    }
                }
            };
        }

        @Override
        public Cell<TaskSummary> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<TaskSummary, TaskSummary> getFieldUpdater() {
            return null;
        }

        @Override
        public TaskSummary getValue(TaskSummary object) {
            return object;
        }
    }
}