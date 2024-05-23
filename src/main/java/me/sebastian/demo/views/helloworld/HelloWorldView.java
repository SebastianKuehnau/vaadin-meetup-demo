package me.sebastian.demo.views.helloworld;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import me.sebastian.demo.data.entity.SamplePerson;
import me.sebastian.demo.data.service.SamplePersonService;
import me.sebastian.demo.views.masterdetail.MasterDetailView;
import org.springframework.data.domain.PageRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;


@Route(value = "")
public class HelloWorldView extends VerticalLayout implements HasUrlParameter<String> {

    private final Grid<SamplePerson> grid;
    private final TextField textField;
    private final SamplePersonService samplePersonService;
    private final Button loadSlowGridButton;

    public HelloWorldView(SamplePersonService samplePersonService) {
        this.samplePersonService = samplePersonService;

        /**
         * 1. Adding Components
         */
        textField = new TextField("Name:", "filter for first name");
        textField.addValueChangeListener(event -> this.filter());
        textField.setWidthFull();
        textField.setValueChangeMode(ValueChangeMode.EAGER);

        var button = new Button("filter");
        /**
         * 2. styling components (ThemeVariants, LumoUtility, CSS, CSS Variables)
         */
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClassName("button-light-orange");
        button.addClickListener(event -> this.filter());

        var filterForm = new HorizontalLayout(textField, button);
        filterForm.addClassNames(LumoUtility.AlignItems.END,
                LumoUtility.Width.FULL,
                LumoUtility.Padding.MEDIUM);

        /**
         * 3. Routing
         */
        var linkToMasterDetailView = new RouterLink("Master Detail View", MasterDetailView.class);

        /**
         * 3. add grid with data
         *    a. filtering (see below the filter method
         */
        grid = new Grid<>(SamplePerson.class);

        /**
         * b. lazy loading (also considering taking a look into the filter method
         */
        grid.setItems(
                query -> samplePersonService.list(PageRequest.of(query.getPage(), query.getPageSize())).stream(),
                query -> samplePersonService.count());
        grid.setColumns("id", "firstName", "lastName", "email");
        grid.addClassNames(LumoUtility.Margin.MEDIUM);

        /**
         * 4. Asynchronous Processes
         */
        loadSlowGridButton = new Button("Load slow grid in Dialog");
        loadSlowGridButton.addClickListener(this::showSlowGridDialog);
        loadSlowGridButton.addClassNames(
                LumoUtility.Background.ERROR,
                LumoUtility.TextColor.PRIMARY_CONTRAST,
                LumoUtility.Margin.MEDIUM);

        VerticalLayout gridLayout = new VerticalLayout(filterForm, grid);
        gridLayout.addClassNames(LumoUtility.BoxShadow.MEDIUM, LumoUtility.Padding.MEDIUM);
        gridLayout.setSpacing(false);

        add(gridLayout, loadSlowGridButton);
        setPadding(false);
    }

    private void showSlowGridDialog(ClickEvent<Button> buttonClickEvent) {

        var slowGrid = new Grid<>(SamplePerson.class);
        slowGrid.setColumns("id", "firstName", "lastName", "email");
        slowGrid.addClassName(LumoUtility.Padding.MEDIUM);
        slowGrid.setSizeFull();

        var dialog = new Dialog(slowGrid);
        dialog.setWidth("80%");
        dialog.setHeight("80%");
        dialog.setModal(true);
        dialog.isCloseOnEsc();
        dialog.setCloseOnOutsideClick(true);
        dialog.addOpenedChangeListener(event -> {
            this.loadSlowGridButton.setVisible(!event.isOpened());
        });

        var loadingSlowGridButton = new Button("loading...");
        loadingSlowGridButton.addClassNames(LumoUtility.Background.CONTRAST_70, LumoUtility.Margin.MEDIUM,
                LumoUtility.TextColor.PRIMARY_CONTRAST);
        loadingSlowGridButton.setEnabled(false);
        replace(this.loadSlowGridButton, loadingSlowGridButton);

        var openSlowGridDialogButton = new Button("Open Dialog with Grid");
        openSlowGridDialogButton.addClassNames(LumoUtility.Margin.MEDIUM, LumoUtility.Background.SUCCESS,
                LumoUtility.TextColor.PRIMARY_CONTRAST);
        openSlowGridDialogButton.addClickListener(event -> {
            replace(openSlowGridDialogButton, this.loadSlowGridButton);
            this.loadSlowGridButton.addClassName(LumoUtility.Background.CONTRAST_50);
            dialog.open();
        } );

        CompletableFuture
                .supplyAsync(samplePersonService::slowList, Executors.newCachedThreadPool())
                .thenAccept(personList ->
                    getUI().ifPresent(
                        ui -> ui.access(
                                () -> {
                                    slowGrid.setItems(personList);
                                    replace(loadingSlowGridButton, openSlowGridDialogButton);
                                })));
    }

    private void filter() {
        grid.setItems(
                query -> samplePersonService.listByFirstNameLike(VaadinSpringDataHelpers.toSpringPageRequest(query), textField.getValue()).stream(),
                query -> samplePersonService.countByFirstNameLike(textField.getValue()));
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameterValue) {
        textField.setValue(parameterValue == null ? "" : parameterValue);
    }
}
