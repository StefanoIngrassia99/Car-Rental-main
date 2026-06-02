public class UserNotAbbonatoException extends Exception{
    public UserNotAbbonatoException(){
        super("Problema con l'assegnazione del noleggio all'utente");
    }
    @Override
    public String toString(){
        return getMessage()+": utente non abbonato";
    }
}
