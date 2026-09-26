package com.github.randir7.hibernatetask.exception;

/**
 * Данные не прошли бизнес-валидацию.
 *
 * <p><b>Смысл:</b> UserService проверил входные данные (имя не пустое,
 * email с «@», возраст 0–150, email не занят) и отказал. Это ошибка
 * пользователя, а не системы.
 *
 * <p><b>Кто бросает:</b> UserService (методы validate/check).
 *
 * <p><b>Кто обрабатывает:</b> ConsoleMenu — печатает сообщение с префиксом
 * «[НЕ ПОЛУЧИЛОСЬ]». В лог НЕ пишется: это не происшествие.
 *
 * <p><b>Почему нет параметра cause:</b> за отказом валидации не стоит
 * никакой технической цепочки, в отличие от {@link DataAccessException},
 * где cause обязателен — осознанная асимметрия.
 */
public class ValidationException extends RuntimeException {
    
    /**
     * @param message текст для пользователя, например
     *                «Возраст должен быть в диапазоне от 0 до 150».
     *                Единственный «груз» исключения — опустить его нельзя.
     */
    public ValidationException(String message) {
        super(message);
    }
}