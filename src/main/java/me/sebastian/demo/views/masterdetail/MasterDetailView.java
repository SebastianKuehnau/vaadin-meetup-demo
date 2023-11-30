package me.sebastian.demo.views.masterdetail;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import me.sebastian.demo.data.entity.SamplePerson;
import me.sebastian.demo.data.service.SamplePersonService;

import java.util.Optional;

@PageTitle("Master-Detail")
@Route(value = "master-detail/:samplePersonID?/:action?(edit)")
@Uses(Icon.class)
public class MasterDetailView extends Div implements BeforeEnterObserver {

    static final String SAMPLEPERSON_ID = "samplePersonID";
    static final String SAMPLEPERSON_EDIT_ROUTE_TEMPLATE = "master-detail/%s/edit";
    final PersonGrid grid;
    private final SamplePersonService samplePersonService;
    final PersonForm form;

    public MasterDetailView(MasterDetailFactory componentFactory) {
        addClassNames("master-detail-view");

        samplePersonService = componentFactory.createService();
        grid = componentFactory.createGrid(this::clearForm);
        form = componentFactory.createForm(this::refreshGrid);

        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.addToPrimary(grid);
        splitLayout.addToSecondary(form);

        add(splitLayout);
    }

    private void refreshGrid() {
        grid.refreshGrid();
    }

    private void clearForm() {
        form.clearForm();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> samplePersonId = event.getRouteParameters().get(SAMPLEPERSON_ID).map(Long::parseLong);
        if (samplePersonId.isPresent()) {
            Optional<SamplePerson> samplePersonFromBackend = samplePersonService.get(samplePersonId.get());
            if (samplePersonFromBackend.isPresent()) {
                form.populateForm(samplePersonFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested samplePerson was not found, ID = %s", samplePersonId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                this.refreshGrid();
                event.forwardTo(MasterDetailView.class);
            }
        }
    }
}
