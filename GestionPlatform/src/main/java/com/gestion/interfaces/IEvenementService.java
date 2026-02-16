package com.gestion.interfaces;

import java.sql.SQLException;
import java.util.List;

public interface IEvenementService<T> {
    void addEntity(T t)throws SQLException;
    void deleteEntity(T t) throws SQLException;
    void updateEntity(T t)  throws SQLException;
    List<T> getData()throws SQLException;
}
