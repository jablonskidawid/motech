package org.motechproject.tasks.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.tasks.domain.mds.channel.ActionEvent;
import org.motechproject.tasks.domain.mds.channel.ActionParameter;
import org.motechproject.tasks.domain.mds.channel.builder.ActionEventBuilder;
import org.motechproject.tasks.domain.mds.task.Task;
import org.motechproject.tasks.domain.mds.task.TaskActionInformation;
import org.motechproject.tasks.domain.mds.task.builder.TaskBuilder;
import org.motechproject.tasks.exception.ActionNotFoundException;
import org.motechproject.tasks.exception.TaskHandlerException;
import org.motechproject.tasks.service.TaskActivityService;
import org.motechproject.tasks.service.TaskService;
import org.motechproject.tasks.service.util.TaskContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.motechproject.tasks.domain.mds.ParameterType.MAP;

@RunWith(MockitoJUnitRunner.class)
public class TaskActionExecutorTest {

    @Mock
    private TaskService taskService;
    @Mock
    private TaskActivityService activityService;
    @Mock
    private EventRelay eventRelay;
    @Mock
    private BundleContext bundleContext;

    @Test
    public void shouldRaiseEventIfActionHasSubject() throws ActionNotFoundException, TaskHandlerException {
        TaskActionInformation actionInformation = new TaskActionInformation("action", "channel", "module", "0.1", "actionSubject");
        ActionEvent actionEvent = new ActionEventBuilder().setDisplayName("Action").setSubject("actionSubject")
                .setDescription("").setActionParameters(new TreeSet<>()).build();
        when(taskService.getActionEventFor(actionInformation)).thenReturn(actionEvent);

        Task task = new TaskBuilder().addAction(new TaskActionInformation("Action", "channel", "module", "0.1", "actionSubject")).build();

        TaskActionExecutor taskActionExecutor = new TaskActionExecutor(taskService, activityService, eventRelay);
        taskActionExecutor.setBundleContext(bundleContext);

        taskActionExecutor.execute(task, actionInformation, new TaskContext(task, new HashMap(), activityService));

        MotechEvent raisedEvent = new MotechEvent("actionSubject", new HashMap<>());
        verify(eventRelay).sendEventMessage(raisedEvent);
    }

    @Test
    public void shouldRaiseEventWhenActionHasSubjectAndService_IfServiceIsNotAvailable() throws TaskHandlerException, ActionNotFoundException {
        TaskActionInformation actionInformation = new TaskActionInformation("action", "channel", "module", "0.1", "serviceInterface", "serviceMethod");
        ActionEvent actionEvent = new ActionEventBuilder().setDisplayName("Action").setSubject("actionSubject")
                .setDescription("").setServiceInterface("serviceInterface").setServiceMethod("serviceMethod").setActionParameters(new TreeSet<ActionParameter>()).build();
        actionEvent.setActionParameters(new TreeSet<>());
        when(taskService.getActionEventFor(actionInformation)).thenReturn(actionEvent);

        when(bundleContext.getServiceReference("serviceInterface")).thenReturn(null);

        Task task = new TaskBuilder().addAction(new TaskActionInformation("Action", "channel", "module", "0.1", "actionSubject")).build();

        TaskActionExecutor taskActionExecutor = new TaskActionExecutor(taskService, activityService, eventRelay);
        taskActionExecutor.setBundleContext(bundleContext);

        taskActionExecutor.execute(task, actionInformation, new TaskContext(task, new HashMap(), activityService));

        verify(eventRelay).sendEventMessage(any(MotechEvent.class));
    }

    @Test
    public void shouldNotRaiseEventIfActionHasSubjectAndService_IfServiceIsAvailable() throws ActionNotFoundException, TaskHandlerException {
        TaskActionInformation actionInformation = new TaskActionInformation("action", "channel", "module", "0.1", "serviceInterface", "serviceMethod");
        ActionEvent actionEvent = new ActionEventBuilder().setDisplayName("Action").setSubject("actionSubject")
                .setDescription("").setServiceInterface("serviceInterface").setServiceMethod("serviceMethod")
                .setActionParameters(new TreeSet<>()).build();
        when(taskService.getActionEventFor(actionInformation)).thenReturn(actionEvent);

        ServiceReference serviceReference = mock(ServiceReference.class);
        when(bundleContext.getServiceReference("serviceInterface")).thenReturn(serviceReference);
        when(bundleContext.getService(serviceReference)).thenReturn(new TestService());

        Task task = new TaskBuilder().addAction(new TaskActionInformation("Action", "channel", "module", "0.1", "actionSubject")).build();

        TaskActionExecutor taskActionExecutor = new TaskActionExecutor(taskService, activityService, eventRelay);
        taskActionExecutor.setBundleContext(bundleContext);

        taskActionExecutor.execute(task, actionInformation, new TaskContext(task, new HashMap(), activityService));

        verify(eventRelay, never()).sendEventMessage(any(MotechEvent.class));
    }

    @Test
    public void shouldInvokeServiceIfActionHasService() throws ActionNotFoundException, TaskHandlerException {
        TaskActionInformation actionInformation = new TaskActionInformation("action", "channel", "module", "0.1", "serviceInterface", "serviceMethod");
        ActionEvent actionEvent = new ActionEventBuilder().setDisplayName("Action")
                .setDescription("").setServiceInterface("serviceInterface").setServiceMethod("serviceMethod")
                .setActionParameters(new TreeSet<>()).build();
        when(taskService.getActionEventFor(actionInformation)).thenReturn(actionEvent);

        ServiceReference serviceReference = mock(ServiceReference.class);
        when(bundleContext.getServiceReference("serviceInterface")).thenReturn(serviceReference);
        TestService testService = new TestService();
        when(bundleContext.getService(serviceReference)).thenReturn(testService);

        Task task = new TaskBuilder().addAction(new TaskActionInformation("Action", "channel", "module", "0.1", "actionSubject")).build();

        TaskActionExecutor taskActionExecutor = new TaskActionExecutor(taskService, activityService, eventRelay);
        taskActionExecutor.setBundleContext(bundleContext);

        taskActionExecutor.execute(task, actionInformation, new TaskContext(task, new HashMap(), activityService));

        assertTrue(testService.serviceMethodInvoked());
    }

    @Test(expected = TaskHandlerException.class)
    public void shouldThrowExceptionIfBundleContextIsNotAvailable() throws TaskHandlerException, ActionNotFoundException {
        TaskActionInformation actionInformation = new TaskActionInformation("action", "channel", "module", "0.1", "serviceInterface", "serviceMethod");
        ActionEvent actionEvent = new ActionEventBuilder().setDisplayName("Action")
                .setDescription("").setServiceInterface("serviceInterface").setServiceMethod("serviceMethod")
                .setActionParameters(new TreeSet<>()).build();
        actionEvent.setActionParameters(new TreeSet<>());
        when(taskService.getActionEventFor(actionInformation)).thenReturn(actionEvent);

        Task task = new TaskBuilder().addAction(new TaskActionInformation("Action", "channel", "module", "0.1", "actionSubject")).build();

        TaskActionExecutor taskActionExecutor = new TaskActionExecutor(taskService, activityService, eventRelay);

        taskActionExecutor.execute(task, actionInformation, new TaskContext(task, new HashMap(), activityService));
    }

    @Test(expected = TaskHandlerException.class)
    public void shouldThrowExceptionIfActionHasNeitherEventNorService() throws TaskHandlerException, ActionNotFoundException {
        TaskActionInformation actionInformation = new TaskActionInformation("action", "channel", "module", "0.1", "serviceInterface", "serviceMethod");
        ActionEvent actionEvent = new ActionEventBuilder().setDisplayName("Action")
                .setDescription("").setServiceInterface("serviceInterface").setServiceMethod("serviceMethod")
                .setActionParameters(new TreeSet<>()).build();
        actionEvent.setActionParameters(new TreeSet<>());
        when(taskService.getActionEventFor(actionInformation)).thenReturn(actionEvent);

        Task task = new TaskBuilder().addAction(new TaskActionInformation("Action", "channel", "module", "0.1", "actionSubject")).build();

        TaskActionExecutor taskActionExecutor = new TaskActionExecutor(taskService, activityService, eventRelay);

        taskActionExecutor.execute(task, actionInformation, new TaskContext(task, new HashMap(), activityService));
    }

    @Test
    public void shouldAddActivityNotificationIfServiceIsNotAvailable() throws TaskHandlerException, ActionNotFoundException {
        TaskActionInformation actionInformation = new TaskActionInformation("action", "channel", "module", "0.1", "serviceInterface", "serviceMethod");
        ActionEvent actionEvent = new ActionEventBuilder().setDisplayName("Action").setSubject("actionSubject")
                .setDescription("").setServiceInterface("serviceInterface").setServiceMethod("serviceMethod")
                .setActionParameters(new TreeSet<>()).build();
        actionEvent.setActionParameters(new TreeSet<>());
        when(taskService.getActionEventFor(actionInformation)).thenReturn(actionEvent);

        when(bundleContext.getServiceReference("serviceInterface")).thenReturn(null);

        Task task = new TaskBuilder().addAction(new TaskActionInformation("Action", "channel", "module", "0.1", "actionSubject")).build();

        TaskActionExecutor taskActionExecutor = new TaskActionExecutor(taskService, activityService, eventRelay);
        taskActionExecutor.setBundleContext(bundleContext);

        taskActionExecutor.execute(task, actionInformation, new TaskContext(task, new HashMap(), activityService));

        verify(activityService).addWarning(task, "task.warning.serviceUnavailable", "serviceInterface");
    }

    @Test
    public void shouldExecuteTaskIfActionMapParameterHasValueWithMixedTypes() throws Exception {
        TaskActionInformation actionInformation = prepareTaskActionInformation();
        ActionEvent actionEvent = prepareActionEvent();

        when(taskService.getActionEventFor(actionInformation)).thenReturn(actionEvent);

        Task task = new TaskBuilder().addAction(new TaskActionInformation("Action", "channel", "module", "0.1", "actionSubject")).build();

        TaskActionExecutor taskActionExecutor = new TaskActionExecutor(taskService, activityService, eventRelay);
        taskActionExecutor.setBundleContext(bundleContext);

        taskActionExecutor.execute(task, actionInformation, prepareTaskContext(task));

        verify(eventRelay).sendEventMessage(eq(prepareMotechEvent()));
    }

    private MotechEvent prepareMotechEvent() {
        Map<String, Object> parameters = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value123");
        parameters.put("map", map);
        return new MotechEvent("actionSubject", parameters);
    }

    private TaskContext prepareTaskContext(Task task) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", 123);
        return new TaskContext(task, parameters, activityService);
    }

    private TaskActionInformation prepareTaskActionInformation() {
        TaskActionInformation actionInformation = new TaskActionInformation();
        actionInformation.setDisplayName("action");
        actionInformation.setChannelName("channel");
        actionInformation.setModuleName("module");
        actionInformation.setModuleVersion("0.1");
        actionInformation.setSubject("actionSubject");

        Map<String, String> values = new HashMap<>();
        values.put("map", "key1:value{{trigger.id}}");
        actionInformation.setValues(values);

        return actionInformation;
    }

    private ActionEvent prepareActionEvent() {
        ActionEvent actionEvent = new ActionEvent();
        actionEvent.setDisplayName("Action");
        actionEvent.setSubject("actionSubject");
        actionEvent.setDescription("");

        SortedSet<ActionParameter> parameters = new TreeSet<>();
        ActionParameter parameter = new ActionParameter();
        parameter.setDisplayName("Map");
        parameter.setKey("map");
        parameter.setType(MAP);
        parameter.setOrder(1);
        parameters.add(parameter);
        actionEvent.setActionParameters(parameters);

        return actionEvent;
    }

    private class TestService {

        private boolean invoked;

        private boolean serviceMethodInvoked() {
            return invoked;
        }

        public void serviceMethod() {
            invoked = true;
        }
    }
}
