package io.github.gvn2012.messaging_service.repositories;

import io.github.gvn2012.messaging_service.models.GroupCallRoom;
import org.springframework.data.repository.CrudRepository;

public interface GroupCallRoomRepository extends CrudRepository<GroupCallRoom, String> {
}
