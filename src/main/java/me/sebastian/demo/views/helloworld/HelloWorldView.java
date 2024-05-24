package me.sebastian.demo.views.helloworld;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import me.sebastian.demo.data.entity.SamplePerson;
import me.sebastian.demo.data.service.SamplePersonService;
import me.sebastian.demo.views.masterdetail.MasterDetailView;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;


@Route(value = "")
public class HelloWorldView extends VerticalLayout {

    private final Grid<SamplePerson> grid;
    private final SamplePersonService samplePersonService;
    private final Button loadSlowGridButton;
    private ConfigurableFilterDataProvider<SamplePerson, Void, String> filterDataProvider;

    public HelloWorldView(SamplePersonService samplePersonService) {
        this.samplePersonService = samplePersonService;

        /**
         * 1. Adding Components
         */
        TextField textField = new TextField("Name:", "filter for first name");
        textField.addValueChangeListener(event -> filterDataProvider.setFilter(event.getValue()));
        textField.setWidthFull();
        textField.setValueChangeMode(ValueChangeMode.EAGER);

        /**
         * 2. styling components (ThemeVariants, LumoUtility, CSS, CSS Variables)
         */
        var button = new Button("filter");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClassName("button-light-blue");
        button.addClickListener(event -> filterDataProvider.setFilter(textField.getValue()));

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
        DataProvider<SamplePerson, String> dataProvider = DataProvider.fromFilteringCallbacks(
                        query -> samplePersonService.listByNameLike(VaadinSpringDataHelpers.toSpringPageRequest(query), query.getFilter().orElse("")).stream(),
                        query -> samplePersonService.countByNameLike(query.getFilter().orElse("")));

        filterDataProvider = dataProvider.withConfigurableFilter();
        grid.setDataProvider(filterDataProvider);
        grid.setColumns("id", "firstName", "lastName", "email");
        grid.addClassNames(LumoUtility.Margin.MEDIUM);

        VerticalLayout gridLayout = new VerticalLayout(filterForm, grid);
        gridLayout.addClassNames(LumoUtility.BoxShadow.MEDIUM, LumoUtility.Padding.MEDIUM);
        gridLayout.setSpacing(false);
        gridLayout.setSizeFull();

        /**
         * 4. Asynchronous Processes
         */
        loadSlowGridButton = new Button("Load slow grid in Dialog");
        loadSlowGridButton.addClickListener(this::showSlowGridDialog);
        loadSlowGridButton.addClassNames(
                LumoUtility.Background.SUCCESS_50,
                LumoUtility.TextColor.PRIMARY_CONTRAST,
                LumoUtility.Margin.MEDIUM,
                LumoUtility.Padding.XLARGE);

        add(gridLayout, loadSlowGridButton);
        addClassName(LumoUtility.AlignItems.CENTER);
        setPadding(false);
        setSizeFull();
    }

    private void showSlowGridDialog(ClickEvent<Button> buttonClickEvent) {

        var loadingButton = new Button("loading...");
        loadingButton.addClassNames(LumoUtility.Background.CONTRAST_20,
                LumoUtility.Margin.MEDIUM,
                LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.Padding.XLARGE);
        loadingButton.setEnabled(false);
        replace(this.loadSlowGridButton, loadingButton);

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
        dialog.addDialogCloseActionListener(dialogCloseActionEvent -> {
            replace(loadingButton, this.loadSlowGridButton);
            dialog.close();
        });

        CompletableFuture
                .supplyAsync(samplePersonService::slowList, Executors.newCachedThreadPool())
                .thenAccept(personList ->
                    getUI().ifPresent(
                        ui -> ui.access(
                                () -> {
                                    slowGrid.setItems(personList);
                                    dialog.open();
                                })));
    }
}
