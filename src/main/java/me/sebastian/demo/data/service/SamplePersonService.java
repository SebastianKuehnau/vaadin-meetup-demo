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

    public Page<SamplePerson> listByFirstNameLike(Pageable pageable, String firstName) {
        return repository.findAll(byFirstNameLike(firstName), pageable);
    }

    public int countByFirstNameLike(String firstName) {
        return (int) repository.count(byFirstNameLike(firstName));
    }

    private static Specification<SamplePerson> byFirstNameLike(String firstName) {
        return (root, query, criteriaBuilder) -> {
            var firstNameExpression = criteriaBuilder.lower(root.get("firstName"));
            var inputPattern = "%" + firstName.toLowerCase() + "%";
            return criteriaBuilder.like(firstNameExpression, inputPattern);
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
