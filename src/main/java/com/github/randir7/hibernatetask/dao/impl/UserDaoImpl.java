package com.github.randir7.hibernatetask.dao.impl;

import com.github.randir7.hibernatetask.dao.UserDao;
import com.github.randir7.hibernatetask.entity.User;
import com.github.randir7.hibernatetask.exception.DataAccessException;
import com.github.randir7.hibernatetask.exception.UserNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Реализация {@link UserDao} на Hibernate.
 *
 * <p><b>Роль:</b> превращает вызовы контракта (save, findById, ...) в SQL
 * через Session — и НЕ выносит наружу ничего hibernate-специфичного:
 * наверх летят только User, Optional, List и наши доменные исключения.
 *
 * <p><b>Модель работы:</b> каждая операция открывает СВОЮ короткоживущую
 * сессию (сессия не потокобезопасна, переиспользовать нельзя). Операции,
 * меняющие данные, обязательны к транзакции: commit — зафиксировать,
 * rollback — отменить целиком («всё или ничего»).
 *
 * В методах используются НЕ SQL, а HQL-запросы.
 * Внимание: указаны имена СУЩНОСТЕЙ (User) и ПОЛЕЙ (u.email),
 * а не таблиц и колонок — Hibernate сам переведёт в SQL.
 *
 * Пищущие операции требуют транзакции,
 * читающие операции могут обойтись без открытия транзакции
 * <p><b>Общий контракт исключений:</b> любой метод оборачивает технические
 * ошибки Hibernate/PostgreSQL в {@link DataAccessException}. Методы, которым
 * нужен существующий пользователь, кидают {@link UserNotFoundException}.
 * Оба — unchecked: чтобы не «заражать» сигнатуры слоёв ничем обрабатываемым.
 *
 * <p><b>Действия при null-значениях:</b> методы «найти» (find) возвращают Optional
 * т.е. в них null-значение - это нормальный результат;
 * методы «действия» (update/delete) должны кидать исключения — в них null-значения недопустимы.
 *
 * В методах есть дублирование кода открытия сессий,
 * но благодаря этому код легче понимать
 */
public class UserDaoImpl implements UserDao {
    
    private static final Logger log = LoggerFactory.getLogger(UserDaoImpl.class);
    
    private final SessionFactory sessionFactory;
    
    public UserDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    // catch в try-with-resources срабатывает до автоматического закрытия ресурса,
    // поэтому rollback в catch успевает выполниться, пока сессия ещё открыта.
    // Порядок всегда такой: catch → закрытие ресурса.
    
    @Override
    public User save(User user) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            return user;
        } catch (RuntimeException e) {
            rollbackQuietly(tx);
            throw new DataAccessException("Не удалось сохранить пользователя " + user, e);
        }
    }
    
    @Override
    public Optional<User> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.find(User.class, id));
        } catch (RuntimeException e) {
            throw new DataAccessException("Не удалось получить пользователя id=" + id, e);
        }
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("select u from User u where u.email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResultOptional();
        } catch (RuntimeException e) {
            throw new DataAccessException("Не удалось найти пользователя по email=" + email, e);
        }
    }
    
    @Override
    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("select u from User u order by u.id", User.class)
                    .getResultList();
        } catch (RuntimeException e) {
            throw new DataAccessException("Не удалось получить список пользователей", e);
        }
    }
    
    @Override
    public User update(User user) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            if (session.find(User.class, user.getId()) == null) {
                throw new UserNotFoundException(user.getId());
            }
            User merged = session.merge(user);
            tx.commit();
            return merged;
        } catch (UserNotFoundException e) {
            rollbackQuietly(tx);
            throw e;
        } catch (RuntimeException e) {
            rollbackQuietly(tx);
            throw new DataAccessException("Не удалось обновить пользователя id=" + user.getId(), e);
        }
    }
    
    @Override
    public void deleteById(Long id) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            User user = session.find(User.class, id);
            if (user == null) {
                throw new UserNotFoundException(id);
            }
            session.remove(user);
            tx.commit();
        } catch (UserNotFoundException e) {
            rollbackQuietly(tx);
            throw e;
        } catch (RuntimeException e) {
            rollbackQuietly(tx);
            throw new DataAccessException("Не удалось удалить пользователя id=" + id, e);
        }
    }
    
    private void rollbackQuietly(Transaction tx) {
        if (tx != null && tx.getStatus().canRollback()) {
            try {
                tx.rollback();
            } catch (RuntimeException e) {
                log.error("Не удалось откатить транзакцию", e);
            }
        }
    }
}
