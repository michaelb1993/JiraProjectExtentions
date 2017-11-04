package com.michabond.scheduling.Exceptions;

public class SchedulingException extends Exception {

    public SchedulingException(String message)
    {
        super(message);
    }

    public SchedulingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SchedulingException(Throwable cause)
    {
        super(cause);
    }
}
