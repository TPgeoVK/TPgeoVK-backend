package ru.tpgeovk.back.ru.tpgeovk.back.exception;

public class VkException extends Exception {

    public VkException(Exception cause) {
        super(cause);
    }

    public VkException(String message, Exception cause) {
        super(message, cause);
    }
}
