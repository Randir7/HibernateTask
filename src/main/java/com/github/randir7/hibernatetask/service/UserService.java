package com.github.randir7.hibernatetask.service;

import com.github.randir7.hibernatetask.dao.UserDao;
import com.github.randir7.hibernatetask.entity.User;
import com.github.randir7.hibernatetask.exception.UserNotFoundException;
import com.github.randir7.hibernatetask.exception.ValidationException;

import java.util.List;
import java.util.Optional;

/**
 * Слой бизнес-логики: решает, ЧТО можно делать с пользователями.
 *
 * <p><b>Ответственность:</b> валидация входных данных (имя, email, возраст),
 * нормализация email (trim + lowercase), защита уникальности email ДО
 * похода в базу. Слой НЕ знает ни Hibernate, ни SQL: работает с
 * {@link UserDao} через интерфейс.
 *
 * <p><b>Обработка ошибок:</b>
 * некорректные данные — {@link ValidationException} (сообщение для пользователя);
 * отсутствие пользователя — {@link UserNotFoundException};
 * технические аварии DAO НЕ перехватывает —
 * исправлять их тут нечем, они уходят в консольный слой, который умеет
 * показывать сообщения человеку.
 */
public class UserService {
    
    // Дублирование ограничений из User
    private static final int NAME_MAX_LENGTH = 100;
    private static final int EMAIL_MAX_LENGTH = 255;
    private static final int MAX_AGE = 150;
    
    private final UserDao userDao;
    
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }
    
    //CREATE
    
    public User create(String name, String email, Integer age) {
        validateName(name);
        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);
        validateAge(age);
        checkEmailFree(normalizedEmail);
        
        return userDao.save(new User(name.trim(), normalizedEmail, age));
    }
    
    //READ
    
    public Optional<User> findById(Long id) {
        return userDao.findById(id);
    }
    
    public User getById(Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
    
    public List<User> findAll() {
        return userDao.findAll();
    }
    
    //UPDATE
    
    public User update(Long id, String name, String email, Integer age) {
        User user = getById(id); // UserNotFoundException, если такого нет
        
        validateName(name);
        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);
        validateAge(age);
        checkEmailNotOccupiedByAnother(normalizedEmail, id);
        
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setAge(age);
        return userDao.update(user);
    }
    
    //DELETE
    
    public void delete(Long id) {
        userDao.deleteById(id);
    }
    
    //МЕТОДЫ ПРОВЕРКИ ДАННЫХ
    
    private void checkEmailFree(String email) {
        if (userDao.findByEmail(email).isPresent()) {
            throw new ValidationException("Email '" + email + "' уже занят");
        }
    }
    
    private void checkEmailNotOccupiedByAnother(String email, Long currentId) {
        userDao.findByEmail(email).ifPresent(other -> {
            if (!other.getId().equals(currentId)) {
                throw new ValidationException("Email '" + email
                        + "' уже занят другим пользователем (id=" + other.getId() + ")");
            }
        });
    }
    
    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
    
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }
        if (name.trim().length() > NAME_MAX_LENGTH) {
            throw new ValidationException("Имя не может быть длиннее " + NAME_MAX_LENGTH + " символов");
        }
    }
    
    private void validateEmail(String email) {
        if (email.isEmpty()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (email.length() > EMAIL_MAX_LENGTH) {
            throw new ValidationException("Email не может быть длиннее " + EMAIL_MAX_LENGTH + " символов");
        }
        // Крайне упрощенная проверка email чтобы не перегружать код
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            throw new ValidationException("Email выглядит некорректно: '" + email + "'");
        }
    }
    
    private void validateAge(Integer age) {
        if (age == null) {
            throw new ValidationException("Возраст обязателен");
        }
        if (age < 0 || age > MAX_AGE) {
            throw new ValidationException("Возраст должен быть в диапазоне от 0 до " + MAX_AGE);
        }
    }
}