package iuh.fit.se.userservice.services;

import iuh.fit.se.userservice.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    public Optional<User> findByUsername(String username);
    public User findById(Long id);
    public User save(User user);
    public List<User> findAll();
    public void deleteById(Long id);
    public User updateUser (User user);
//    public List<User> filterUsers(String keyword, String gender, String role);

//    public List<User> findByRole(Role role);
    public List<User> findAllByIds(List<Long> ids);
}
