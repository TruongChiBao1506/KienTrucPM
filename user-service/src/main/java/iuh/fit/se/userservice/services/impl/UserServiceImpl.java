package iuh.fit.se.userservice.services.impl;

import iuh.fit.se.userservice.entities.User;
import iuh.fit.se.userservice.repositories.UserRepository;
import iuh.fit.se.userservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return this.userRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy người dùng với ID: " + id);
        }
//        if (userRepository.findById(id).get().getOrders().size() > 0) {
//            throw new RuntimeException("Không thể xóa người dùng đã đặt hàng");
//        }
        userRepository.deleteById(id);
    }

    @Override
    public User updateUser(User user) {
        User userInDB = userRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException("User not found"));

        if (userInDB == null) {
            return null;
        } else {
            userInDB.setFullname(user.getFullname());
//            userInDB.setEmail(user.getEmail());
            userInDB.setPhone(user.getPhone());
            userInDB.setAddress(user.getAddress());
            userInDB.setGender(user.isGender());
            userInDB.setDob(user.getDob());
//            userInDB.setRole(user.getRole());
            userInDB.setUsername(user.getUsername());
            userRepository.save(userInDB);
            return userInDB;
        }
    }

    @Override
    public List<User> findAllByIds(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

}
