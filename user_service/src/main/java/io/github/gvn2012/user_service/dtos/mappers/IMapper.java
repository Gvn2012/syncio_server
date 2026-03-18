package io.github.gvn2012.user_service.dtos.mappers;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public interface IMapper<E, D> {

    D toDto(E entity);

    E toEntity(D dto);

    default List<D> toDtoList(List<E> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    default List<E> toEntityList(List<D> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
                .map(this::toEntity)
                .toList();
    }

    default Set<D> toDtoSet(Collection<E> entities) {
        if (entities == null) return Set.of();
        return entities.stream()
                .map(this::toDto)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    default Set<E> toEntitySet(Collection<D> dtos) {
        if (dtos == null) return Set.of();
        return dtos.stream()
                .map(this::toEntity)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }
}
