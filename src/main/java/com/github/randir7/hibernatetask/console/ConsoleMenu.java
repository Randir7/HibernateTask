package com.github.randir7.hibernatetask.console;

import com.github.randir7.hibernatetask.entity.User;
import com.github.randir7.hibernatetask.exception.DataAccessException;
import com.github.randir7.hibernatetask.exception.UserNotFoundException;
import com.github.randir7.hibernatetask.exception.ValidationException;
import com.github.randir7.hibernatetask.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;


/**
 * Консольный интерфейс: меню, диалог, вывод результатов.
 *
 * <p><b>Роль в архитектуре:</b> верхний слой. Знает ТОЛЬКО UserService
 * и ConsoleReader.
 * Не связан с DAO, Hibernate и SQL. Каждое действие
 * пользователя — один приватный метод-обработчик.
 *
 * <p><b>println и log — разные уровни:</b> println — интерфейс для
 * ПОЛЬЗОВАТЕЛЯ (меню, результаты, «не получилось»); лог — журнал для
 * РАЗРАБОТЧИКА (аварии). Физически консоль одна, смысл — разный.
 *
 * <p>Вывод лога в консоль выглядит не очень эстетично, но приемлем
 * для учебного проекта, т.к. явно демонстрирует ход работы программы
 */
public class ConsoleMenu {
    
    private static final Logger log = LoggerFactory.getLogger(ConsoleMenu.class);
    
    private final UserService userService;
    private final ConsoleReader reader;
    
    public ConsoleMenu(UserService userService, ConsoleReader reader) {
        this.userService = userService;
        this.reader = reader;
    }
    
    public void run() {
        System.out.println("=== USER-SERVICE: управление пользователями ===");
        while (true) {
            printMenu();
            int choice = reader.readInt("Выберите действие: ");
            
            if (choice == 0) {
                System.out.println("До встречи!");
                return;
            }
            
            try {
                dispatch(choice);
            } catch (ValidationException | UserNotFoundException e) {
                // Ошибка ввода или «не найдено» — штатная ситуация,
                // показываем пользователю её текст
                System.out.println("[НЕ ПОЛУЧИЛОСЬ] " + e.getMessage());
            } catch (DataAccessException e) {
                // Авария с БД: пользователю — коротко, в лог — всю правду
                log.error("Ошибка доступа к данным", e);
                System.out.println("[ОШИБКА] Проблема с базой данных. "
                        + "Подробности — в logs/app.log");
            }
        }
    }
    
    private void printMenu() {
        System.out.println();
        System.out.println("---- МЕНЮ ----");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти по id");
        System.out.println("3. Показать всех");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("0. Выход");
    }
    
    private void dispatch(int choice) {
        switch (choice) {
            case 1 -> createUser();
            case 2 -> findUserById();
            case 3 -> findAllUsers();
            case 4 -> updateUser();
            case 5 -> deleteUser();
            default -> System.out.println("Нет такого пункта: " + choice);
        }
    }
    
    private void createUser() {
        String name = reader.readString("Имя: ");
        String email = reader.readString("Email: ");
        int age = reader.readInt("Возраст: ");
        User user = userService.create(name, email, age);
        System.out.println("Создан: " + user);
    }
    
    private void findUserById() {
        long id = reader.readLong("id пользователя: ");
        Optional<User> found = userService.findById(id);
        found.ifPresentOrElse(
                user -> System.out.println("Найден: " + user),
                () -> System.out.println("Пользователь с id=" + id + " не найден")
        );
    }
    
    private void findAllUsers() {
        List<User> users = userService.findAll();
        if (users.isEmpty()) {
            System.out.println("Пользователей пока нет.");
        } else {
            System.out.println("Всего пользователей: " + users.size());
            users.forEach(user -> System.out.println("  " + user));
        }
    }
    
    private void updateUser() {
        long id = reader.readLong("id пользователя: ");
        String name = reader.readString("Новое имя: ");
        String email = reader.readString("Новый email: ");
        int age = reader.readInt("Новый возраст: ");
        User updated = userService.update(id, name, email, age);
        System.out.println("Обновлён: " + updated);
    }
    
    private void deleteUser() {
        long id = reader.readLong("id пользователя: ");
        userService.delete(id);
        System.out.println("Пользователь id=" + id + " удалён.");
    }
}