package me.sebastian.demo.views.masterdetail;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.function.SerializableRunnable;
import me.sebastian.demo.data.entity.SamplePerson;
import me.sebastian.demo.data.service.SamplePersonService;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class PersonForm extends Div {

    final TextField firstName = new TextField("First Name");
    final TextField lastName = new TextField("Last Name");
    final TextField email = new TextField("Email");
    private final TextField phone = new TextField("Phone");
    private final DatePicker dateOfBirth = new DatePicker("Date Of Birth");
    private final TextField occupation = new TextField("Occupation");
    private final TextField role = new TextField("Role");
    private final Checkbox important = new Checkbox("Important");

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final SamplePersonService samplePersonService;
    private final SerializableRunnable refreshGridRunnable;
    private final BeanValidationBinder<SamplePerson> binder;
    private SamplePerson samplePerson;

    PersonForm(SamplePersonService samplePersonService, SerializableRunnable refreshGridRunnable) {
        this.samplePersonService = samplePersonService;
        this.refreshGridRunnable = refreshGridRunnable;
        setClassName("editor-layout");


        add(createFormLayout());
        add(createButtonLayout());

        // Configure Form
        binder = new BeanValidationBinder<>(SamplePerson.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.bindInstanceFields(this);

        save.addClickListener(this::clickSaveButton);
        cancel.addClickListener(this::clickCancelButton);
    }

    private void clickCancelButton(ClickEvent<Button> buttonClickEvent) {
        clearForm();
        refreshGridRunnable.run();
    }

    private void clickSaveButton(ClickEvent<Button> buttonClickEvent) {
        try {
            if (this.samplePerson == null) {
                this.samplePerson = new SamplePerson();
            }
            binder.writeBean(this.samplePerson);
            this.samplePersonService.update(this.samplePerson);
            clearForm();
            refreshGridRunnable.run();
            Notification.show("Data updated");
            UI.getCurrent().navigate(MasterDetailView.class);
        } catch (ObjectOptimisticLockingFailureException exception) {
            Notification n = Notification.show(
                    "Error updating the data. Somebody else has updated the record while you were making changes.");
            n.setPosition(Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (ValidationException validationException) {
            Notification.show("Failed to update the data. Check again that all values are valid");
        }
    }

    private Div createFormLayout() {

        firstName.setRequired(true);
        lastName.setId("lastname-field");

        Div editorDiv = new Div();
        editorDiv.add(
                new FormLayout(
                        firstName, lastName, email, phone, dateOfBirth,
                        occupation, role, important));

        editorDiv.setClassName("editor");

        return editorDiv;
    }

    private HorizontalLayout createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        save.setId("save-button");
        cancel.setId("cancel-button");

        buttonLayout.add(save, cancel);
        return buttonLayout;
    }

    void clearForm() {
        populateForm(null);
    }

    void populateForm(SamplePerson value) {
        this.samplePerson = value;
        binder.readBean(this.samplePerson);
    }

}
