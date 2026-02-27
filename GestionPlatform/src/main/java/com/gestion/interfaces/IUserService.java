package com.gestion.interfaces;
import java.sql.Timestamp;

import com.gestion.entities.User;
import java.util.List;

public interface IUserService {

    /**
     * Get all users from the database
     *
     * @return List of all users
     */
    List<User> getAllUsers();

    /**
     * Add a new user to the database
     *
     * @param user User to add
     * @return true if successful, false otherwise
     */
    boolean addUser(User user);

    /**
     * Update an existing user in the database
     *
     * @param user User to update
     * @return true if successful, false otherwise
     */
    boolean updateUser(User user);

    /**
     * Delete a user from the database
     *
     * @param idUser ID of the user to delete
     * @return true if successful, false otherwise
     */
    boolean deleteUser(int idUser);

    /**
     * Get a user by ID
     *
     * @param idUser ID of the user
     * @return User object or null if not found
     */
    User getUserById(int idUser);

    /**
     * Get a user by email
     *
     * @param email Email of the user
     * @return User object or null if not found
     */
    User getUserByEmail(String email);
    void updateLastConnection(int idUser, Timestamp ts);
}
