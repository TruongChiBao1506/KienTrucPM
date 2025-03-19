package iuh.fit.se.userservice.repositories;

import iuh.fit.se.userservice.index.UserIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserElasticsearchRepository extends ElasticsearchRepository<UserIndex, String> {
    List<UserIndex> findByFullnameContainingOrEmailContainingOrPhoneContainingOrUsernameContainingOrAddressContaining(
            String fullname, String email, String phone, String username, String address);

    Optional<UserIndex> findByUserId(Long userId);
}
