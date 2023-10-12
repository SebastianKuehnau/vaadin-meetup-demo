package me.sebastian.demo.views.helloworld;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import me.sebastian.demo.data.service.SamplePersonService;


@Route(value = "")
public class HelloWorldView extends VerticalLayout {

    /**
     * 1. adding components (alignment, padding)
     * 2. styling components (ThemeVariants, LumoUtility, CSS-Classes, CSS-Variables)
     * 3. add grid with data
     *      a. filtering
     *      b. lazy loading
     * 4. Asynchronous Processes
     * 5. Routing (other views, router links, parameter)
     *
     */

    public HelloWorldView(SamplePersonService samplePersonService) {

    }
}
