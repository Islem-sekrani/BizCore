package edu.Connexion3A7.interfaces;

import edu.Connexion3A7.entities.coach;

import java.sql.SQLException;
import java.util.List;

public interface IService <T>{

    void addCoach(coach coach) throws SQLException;

    void deleteCoach(coach coach) throws SQLException;

    void updateCoach(coach coach) throws SQLException;
    List<coach> getData() throws SQLException;
}
