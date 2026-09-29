package com.github.randir7.userservice.exception;

/**
 * Штатная ситуация (НЕ авария): пользователь с указанным id не найден в БД.
 *
 * <p><b>Смысл:</b> человек ввёл несуществующий id — это нормальный поворот
 * диалога, а не поломка. Поэтому класс живёт отдельно от
 * {@link DataAccessException} и обрабатывается иначе (см. ConsoleMenu).
 *
 * <p><b>Кто бросает:</b> UserDaoImpl.update/deleteById — перед изменением
 * несуществующей записи; UserService.getById — через orElseThrow.
 *
 * <p><b>Кто обрабатывает:</b> ConsoleMenu — показывает текст пользователю.
 * В лог НЕ пишется: не происшествие.
 */
public class UserNotFoundException extends RuntimeException {
    
    /**
     * @param id идентификатор, который искали и не нашли.
     *           Конструктор принимает только id: текст собирается здесь же,
     *           поэтому формат сообщения одинаков во всём приложении.
     */
    public UserNotFoundException(Long id) {
        super("Пользователь с id=" + id + " не найден");
    }
}