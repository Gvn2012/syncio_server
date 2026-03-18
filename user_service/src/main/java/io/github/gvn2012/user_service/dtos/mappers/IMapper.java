package io.github.gvn2012.user_service.dtos.mappers;

import java.util.List;

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
}
