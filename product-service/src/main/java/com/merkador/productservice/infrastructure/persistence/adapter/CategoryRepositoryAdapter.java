package com.merkador.productservice.infrastructure.persistence.adapter;

import com.merkador.productservice.core.domain.Category;
import com.merkador.productservice.core.port.out.CategoryRepository;
import com.merkador.productservice.infrastructure.persistence.repository.JpaCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final JpaCategoryRepository jpa;
    private final PersistenceMapper mapper;

    @Override
    public Category save(Category category) {
        return mapper.toDomain(jpa.save(mapper.toEntity(category)));
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return jpa.findBySlug(slug).map(mapper::toDomain);
    }

    @Override
    public List<Category> findByParentIdIsNull() {
        return jpa.findByParentIdIsNull().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Category> findByParentId(UUID parentId) {
        return jpa.findByParentId(parentId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Category> findAllActive() {
        return jpa.findAllActive().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpa.existsBySlug(slug);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public boolean hasChildren(UUID id) {
        return jpa.hasChildren(id);
    }

    @Override
    public boolean hasProducts(UUID id) {
        return jpa.hasProducts(id);
    }
}
