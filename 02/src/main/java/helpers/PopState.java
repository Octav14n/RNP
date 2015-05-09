package helpers;

/**
 * States the Client and Server can be in.
 */
public enum PopState {
    EXPECTING_EXIT,
    DISCONECTED,
    CONNECTED,
    USERNAME_SEND,
    PASSWORD_SEND,
    TRANSACTION,
    MAIL_AVAILABLE,
    EXPECTING_QUIT
}
