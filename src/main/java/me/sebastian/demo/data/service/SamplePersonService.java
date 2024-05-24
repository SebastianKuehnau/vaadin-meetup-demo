package me.sebastian.demo.data.service;

import me.sebastian.demo.data.entity.SamplePerson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class SamplePersonService {

    private final SamplePersonRepository repository;

    public SamplePersonService(SamplePersonRepository repository) {
        this.repository = repository;
    }

    public Optional<SamplePerson> get(Long id) {
        return repository.findById(id);
    }

    public SamplePerson update(SamplePerson entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<SamplePerson> list() {
        return repository.findAll();
    }

    public int count() {
        return (int) repository.count();
    }

    public Page<SamplePerson> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<SamplePerson> list(Pageable pageable, Specification<SamplePerson> filter) {
        return repository.findAll(filter, pageable);
    }

    public Page<SamplePerson> listByNameLike(Pageable pageable, String name) {
        return repository.findAll(byNameLike(name), pageable);
    }

    public int countByNameLike(String name) {
        return (int) repository.count(byNameLike(name));
    }

    private static Specification<SamplePerson> byNameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            var firstNameExpression = criteriaBuilder.lower(root.get("firstName"));
            var lastNameExpression = criteriaBuilder.lower(root.get("lastName"));
            var inputPattern = "%" + name.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(firstNameExpression, inputPattern),
                    criteriaBuilder.like(lastNameExpression, inputPattern)
            );
        };
    }

    public List<SamplePerson> slowList() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return repository.findAll();
    }
}
