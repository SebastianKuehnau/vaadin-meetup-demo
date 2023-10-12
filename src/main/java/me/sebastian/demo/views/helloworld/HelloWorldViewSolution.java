package me.sebastian.demo.views.helloworld;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import me.sebastian.demo.data.entity.SamplePerson;
import me.sebastian.demo.data.service.SamplePersonService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class HelloWorldViewSolution extends VerticalLayout implements HasUrlParameter<String> {

    /**
     * 1. adding components
     * 2. styling components (ThemeVariants, LumoUtility, CSS, CSS Variables)
     * 3. Routing
     * 3. add grid with data
     *      a. filtering
     *      b. lazy loading
     * 4. Asynchronous Processes
     */

    private final Grid<SamplePerson> grid;
    private final TextField textField;
    private final SamplePersonService samplePersonService;

    public HelloWorldViewSolution(SamplePersonService samplePersonService) {
        this.samplePersonService = samplePersonService;
        textField = new TextField("Name:", "filter for first name");
        textField.addValueChangeListener(event -> this.filter());
        textField.setWidthFull();
        textField.setValueChangeMode(ValueChangeMode.EAGER);

        var button = new Button("filter");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(event -> this.filter());

        var layout = new HorizontalLayout(textField, button);
        layout.setWidthFull();
        layout.setAlignItems(Alignment.END);

        grid = new Grid<>(SamplePerson.class);
        grid.setItems(
                query -> samplePersonService.list(PageRequest.of(query.getPage(), query.getPageSize())).stream(),
                query -> samplePersonService.count());
        grid.setColumns("id", "firstName", "lastName", "email");

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

