package com.github.randir7.userservice;

import com.github.randir7.userservice.config.HibernateUtil;
import com.github.randir7.userservice.console.ConsoleMenu;
import com.github.randir7.userservice.console.ConsoleReader;
import com.github.randir7.userservice.dao.UserDao;
import com.github.randir7.userservice.dao.impl.UserDaoImpl;
import com.github.randir7.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Точка входа приложения.
 *
 * <p>Единственная задача этого класса — <b>собрать приложение и запустить его</b>:
 * здесь создаётся цепочка слоёв (DAO -> Service -> консольное меню) и запускается
 * главный цикл взаимодействия с пользователем.
 *
 * <p>Слои и их ответственность:
 * <ul>
 *   <li>{@code UserDaoImpl} (DAO) — все операции с БД через Hibernate;
 *   <li>{@code UserService} — валидация и бизнес-правила;
 *   <li>{@code ConsoleMenu} / {@code ConsoleReader} — диалог с пользователем через текстовое меню в консоли.
 * </ul>
 *
 * <p>Важно: слои зависят только друг от друга через интерфейсы (например, сервис
 * знает {@code UserDao} как интерфейс, а не реализацию). Конкретные классы
 * (« new UserDaoImpl(...)» и т.д.) встречаются ТОЛЬКО здесь.
 */

public class Application {
    
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    
    public static void main(String[] args) {
        log.info("Запуск приложения user-service");
        
        // Ручная сборка приложения
        // Направление зависимостей: Application -> Console -> Service -> DAO
        UserDao userDao = new UserDaoImpl(HibernateUtil.getSessionFactory());
        UserService userService = new UserService(userDao);
        // ConsoleReader отвечает за ввод с клавиатуры (Scanner инкапсулирован внутри).
        ConsoleReader reader = new ConsoleReader();
        // Меню — верхний слой: знает только UserService и ConsoleReader.
        ConsoleMenu menu = new ConsoleMenu(userService, reader);
        
        try {
            // Работает до выбора «0. Выход». Все ШТАТНЫЕ ошибки (валидация,
            // «не найдено», проблемы с БД) обрабатываются внутри меню —
            // сюда долетают только непредвиденные исключения.
            menu.run();
        } catch (RuntimeException e) {
            // Страховка на случай непредвиденной ошибки в приложении
            // в лог пишется и само сообщение об ошибке, и полный стектрейс
            // для теста: нажать Ctrl+D во время ввода значения в консоль
            log.error("Непредвиденная ошибка, приложение закрывается", e);
            System.out.println("Непредвиденная ошибка, приложение закрывается");
        } finally {
            // Остановка SessionFactory
            HibernateUtil.shutdown();
        }
        
        log.info("Приложение остановлено");
    }
}