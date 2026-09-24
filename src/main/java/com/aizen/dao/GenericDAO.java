package com.aizen.dao;

import com.aizen.exception.DatabaseException;

import java.util.List;
import java.util.Optional;

/** Generic CRUD contract implemented by every DAO. */
public interface GenericDAO<T> {
    T insert(T entity) throws DatabaseException;

    Optional<T> findById(int id) throws DatabaseException;

    List<T> findAll() throws DatabaseException;

    T update(T entity) throws DatabaseException;

    boolean delete(int id) throws DatabaseException;
}
