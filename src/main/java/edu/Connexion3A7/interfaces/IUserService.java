package edu.Connexion3A7.interfaces;

import edu.Connexion3A7.entities.user;

import java.sql.SQLException;

public interface IUserService {
    user authenticate(String email, String password) throws SQLException;
}
