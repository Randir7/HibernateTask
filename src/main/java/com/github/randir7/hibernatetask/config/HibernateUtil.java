package com.github.randir7.hibernatetask.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Хранит и выдаёт единственный на всё приложение объект {@link SessionFactory}.
 *
 * SessionFactory это «фабрика сессий» Hibernate. При создании
 * читает конфигурацию (hibernate.cfg.xml), готовит маппинги сущностей и
 * управляет пулом соединений с базой данных.
 *
 * Паттерн Singleton используется т.к. создание фабрики — «дорогая» операция
 * (конфигурация + маппинги + пул соединений).
 *
 * <p><b>Кто пользуется этим классом:</b> Application — дважды: при старте
 * (отдаёт фабрику в UserDaoImpl) и при завершении (метод shutdown()).
 *
 * <p><b>Жизненный цикл:</b> фабрика создаётся при загрузке класса
 * (жадная инициализация) и живёт до конца работы приложения.
 * Объекты фабрики (Session) — лёгкие и короткоживущие: DAO открывает новую на каждую операцию.
 */
public final class HibernateUtil {
    
    private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);
    
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();
    
    
    // Утилитный класс: создавать экземпляры запрещено
    // Все методы класса статические — экземпляр ему не нужен.
    private HibernateUtil() {
    
    }
    
// Читает hibernate.cfg.xml из classpath и строит SessionFactory.
// Вызывается один раз — при инициализации поля SESSION_FACTORY.
//
// Файл обязан лежать в src/main/resources, т.к. эта папка
// попадает в classpath.
    
    
    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration()
                    .configure("hibernate.cfg.xml") // ищет файл в classpath
                    .buildSessionFactory();
        } catch (Throwable e) {
            // Отлов любых Throwable, а не Exception:
            // на старте возможны и непрогнозируемые Error'ы
            // и любая причина падения старта должна быть в логе.
            log.error("Не удалось инициализировать SessionFactory", e);
            
            // Стандартный способ сообщить JVM об ошибке статической
            // инициализации. Исходное исключение сохраняется в cause,
            // поэтому в стектрейсе останется цепочка Caused by.
            // Если при инициализации класса возникла эта ошибка,
            // класс переходит в состояние «ошибки инициализации» (InitializationError).
            // Любая последующая попытка использовать этот класс
            // (создать объект, вызвать статический метод)
            // приведет к мгновенному повторному выбросу той же самой ошибки
            // без выполнения кода инициализации.
            throw new ExceptionInInitializerError(e);
        }
    }
    
    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }
    
    //Аккуратно закрывает фабрику при завершении приложения
    // (закрываются соединения с БД). Вызывается из finally в Application.main
    // гарантированно, при любом исходе работы.
    public static void shutdown() {
        log.info("Закрываю SessionFactory");
        SESSION_FACTORY.close();
    }
}