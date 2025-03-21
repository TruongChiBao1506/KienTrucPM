package iuh.fit.se.userservice.services;

import iuh.fit.se.userservice.entities.User;
import iuh.fit.se.userservice.index.UserIndex;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    public Optional<User> findByUsername(String username);
    public User findById(Long id);
    public User save(User user);
    public List<User> findAll();
    public void deleteById(Long id);
    public User updateUser (User user);
    public List<User> filterUsers(String keyword, String gender, String role);
    public User findByUserId(Long userId);
//    public List<User> findByRole(Role role);
    public List<User> findAllByIds(List<Long> ids);
    public List<UserIndex> searchUsers(String keyword, String gender, String role) throws IOException;

}
