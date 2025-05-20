package iuh.fit.se.userservice.services;

import iuh.fit.se.userservice.dtos.UserDTO;
import iuh.fit.se.userservice.entities.User;
//import iuh.fit.se.userservice.index.UserIndex;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    public Optional<User> findByUsername(String username);
    public User findById(Long id);
    public User save(User user);
    public List<UserDTO> findAll();
    public void deleteById(Long id);
    public User updateUser (User user);
    public List<UserDTO> filterUsers(String keyword, String gender, String role);    public User findByUserId(Long userId);
//    public List<User> findByRole(Role role);
    public List<UserDTO> findAllByIds(List<Long> ids);
//    public List<UserIndex> searchUsers(String keyword, String gender, String role) throws IOException;
    
    // Các phương thức phân trang mới
    public Page<UserDTO> findAllPaginated(int page, int size);
    public Page<UserDTO> findAllByIdsPaginated(List<Long> ids, int page, int size);
    public Page<UserDTO> filterUsersPaginated(String keyword, String gender, String role, int page, int size);
}
