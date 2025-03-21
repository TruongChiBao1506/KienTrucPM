package iuh.fit.se.userservice.services;

public interface UserIndexService {
    public void syncUsersToElasticsearch();
    public void addUserToElasticsearch(Long userId);
    public void deleteUserByUserId(Long userId);
}
