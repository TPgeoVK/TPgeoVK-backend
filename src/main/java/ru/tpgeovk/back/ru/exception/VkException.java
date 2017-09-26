package ru.tpgeovk.back.ru.exception;

public class VkException extends Exception {

    public VkException(Exception cause) {
        super(cause);
    }

    public VkException(String message, Exception cause) {
        super(message, cause);
    }
}
