package com.github.randir7.userservice.exception;


/**
 * Техническая авария при работе с базой данных.
 *
 * <p><b>Смысл:</b> обёртка над «сырыми» исключениями Hibernate/PostgreSQL
 * (база недоступна, Connection refused, нарушение constraint и т.п.).
 * Верхние слои (сервис, консоль) не должны зависеть от классов Hibernate —
 * поэтому DAO переводит всё в этот доменный тип, а исходную причину
 * сохраняет в cause (цепочка Caused by в логе).
 *
 * <p><b>Кто бросает:</b> UserDaoImpl — каждый метод оборачивает
 * RuntimeException из Hibernate.
 *
 * <p><b>Кто обрабатывает:</b> ConsoleMenu (пользователю — короткое
 * сообщение, в logs/app.log — полный стектрейс); Application.main —
 * страховочная сетка на самый крайний случай.
 *
 * <p><b>Почему RuntimeException:</b> вызывающие слои не могут «вылечить»
 * упавшую базу — только сообщить. Checked заставил бы протаскивать throws
 * через все сигнатуры слоёв.
 */
public class DataAccessException extends RuntimeException {
    
    /**
     * @param message что случилось — короткое описание для пользователя,
     *                например «Не удалось сохранить пользователя ...»
     * @param cause   исходное исключение Hibernate — ОБЯЗАТЕЛЬНЫЙ параметр
     *                (без него в логе не будет цепочки Caused by, и причину
     *                аварии будет не найти)
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}