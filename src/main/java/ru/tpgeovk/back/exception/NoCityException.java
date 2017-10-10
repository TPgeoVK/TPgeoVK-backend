package ru.tpgeovk.back.exception;

public class NoCityException extends Exception {

    public NoCityException(String message) { super(message); }

    public NoCityException(Exception cause) { super(cause); }

    public NoCityException(String message, Exception cause) { super(message, cause); }
}
