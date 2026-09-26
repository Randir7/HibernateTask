package com.github.randir7.hibernatetask.console;

import java.util.Scanner;


/**
 * В этом классе методы обрабатывающие запросы пользователя.
 * В зависимости от запроса пользователя они возвращают String, Integer, Long, либо сообщают об ошибке
 * Здесь проверяется только ТИП данных, корректность данных валидируется в UserService
 */
public class ConsoleReader {
    
    private final Scanner scanner = new Scanner(System.in);
    
    public String readString(String userInput) {
        System.out.print(userInput);
        return scanner.nextLine();
    }
    
    public int readInt(String userInput) {
        while (true) {
            String line = readString(userInput);
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("  Это не число («" + line + "»), попробуйте ещё раз.");
            }
        }
    }
    
    public long readLong(String userInput) {
        while (true) {
            String line = readString(userInput);
            try {
                return Long.parseLong(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("  Это не число («" + line + "»), попробуйте ещё раз.");
            }
        }
    }
}