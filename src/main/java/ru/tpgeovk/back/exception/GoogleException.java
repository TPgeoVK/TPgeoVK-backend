package ru.tpgeovk.back.exception;

public class GoogleException extends Exception {

    public GoogleException(Exception cause) { super(cause); }

    public GoogleException(String message, Exception cause) { super(message, cause); }
}
