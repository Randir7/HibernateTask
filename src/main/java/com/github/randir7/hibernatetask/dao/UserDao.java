package com.github.randir7.hibernatetask.dao;

import com.github.randir7.hibernatetask.entity.User;
import com.github.randir7.hibernatetask.exception.DataAccessException;
import com.github.randir7.hibernatetask.exception.UserNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * Контракт хранилища пользователей — DAO (Data Access Object).
 *
 * <p><b>Роль паттерна:</b> единственная граница между приложением и способом
 * хранения данных. Всё, что выше (UserService, консоль), работает с
 * пользователями ЧЕРЕЗ ЭТОТ ИНТЕРФЕЙС и не знает, что под ним Hibernate,
 * PostgreSQL и SQL. Замена реализации (JDBC, файл, заглушка в тестах)
 * не должна вызывать неоьходимость менять что-либо в слоях выше.
 *

 */
public interface UserDao {
    
    User save(User user);
    
    Optional<User> findById(Long id);
    
    Optional<User> findByEmail(String email);
    
    List<User> findAll();
    
    User update(User user);
    
    void deleteById(Long id);
}