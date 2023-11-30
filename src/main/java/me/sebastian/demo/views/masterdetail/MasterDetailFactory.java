package me.sebastian.demo.views.masterdetail;

import com.vaadin.flow.function.SerializableRunnable;
import me.sebastian.demo.data.service.SamplePersonService;
import org.springframework.stereotype.Component;

@Component
class MasterDetailFactory {

    private final SamplePersonService samplePersonService;

    public MasterDetailFactory(SamplePersonService samplePersonService) {
        this.samplePersonService = samplePersonService;
    }

    PersonForm createForm(SerializableRunnable refreshGridRunnable) {
        return new PersonForm(samplePersonService, refreshGridRunnable);
    }

    SamplePersonService createService() {
        return samplePersonService;
    }

    public PersonGrid createGrid(SerializableRunnable clearForm) {
        return new PersonGrid(samplePersonService, clearForm);
    }
}
