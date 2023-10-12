package me.sebastian.demo.views.helloworld;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import me.sebastian.demo.data.entity.SamplePerson;
import me.sebastian.demo.data.service.SamplePersonService;
import me.sebastian.demo.views.masterdetail.MasterDetailView;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Route(value = "")
public class HelloWorldView extends VerticalLayout implements HasUrlParameter<String> {

    private final Grid<SamplePerson> grid;
    private final TextField textField;
    private final SamplePersonService samplePersonService;

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

        var layout = new HorizontalLayout(textField, button);
        layout.setWidthFull();
        layout.setAlignItems(Alignment.END);

        /**
         * 3. Routing
         */
        var linkToMasterDetailView = new RouterLink("Master Detail View", MasterDetailView.class);
        add(linkToMasterDetailView);

        /**
         * 3. add grid with data
         *    a. filtering (see below the filter method
         */
        grid = new Grid<>(SamplePerson.class);
        //grid.setItems(samplePersonService.list());

        /**
         * b. lazy loading (also considering taking a look into the filter method
         */
        grid.setItems(
                query -> samplePersonService.list(PageRequest.of(query.getPage(), query.getPageSize())).stream(),
                query -> samplePersonService.count());
        grid.setColumns("id", "firstName", "lastName", "email");

        /**
         * 4. Asynchronous Processes
         */
        var slowGrid = new Grid<>(SamplePerson.class);
        slowGrid.setEnabled(false);

        ExecutorService executorService = Executors.newCachedThreadPool();
        CompletableFuture<List<SamplePerson>> listCompletableFuture = CompletableFuture.supplyAsync(() -> samplePersonService.slowList(), executorService);
        listCompletableFuture.thenAccept(personList ->
                getUI().ifPresent(
                        ui -> ui.access(
                                () -> {
                                    slowGrid.setItems(personList);
                                    slowGrid.setEnabled(true);
                                })));

        add(layout, grid, slowGrid);
    }

    private void filter() {
        //samplePersonGridListDataView.setFilter(samplePerson -> samplePerson.getFirstName().toLowerCase().startsWith(textField.getValue().toLowerCase()));

        grid.setItems(
                query -> samplePersonService.listByFirstNameLike(VaadinSpringDataHelpers.toSpringPageRequest(query), textField.getValue()).stream(),
                query -> samplePersonService.countByFirstNameLike(textField.getValue()));
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameterValue) {
        textField.setValue(parameterValue == null ? "" : parameterValue);
    }
}
