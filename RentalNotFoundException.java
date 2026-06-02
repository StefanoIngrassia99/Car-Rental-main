public class RentalNotFoundException extends Exception {
    public RentalNotFoundException() {
        super("Problema con rimozione noleggio");
    }
    @Override
    public String toString(){
        return getMessage()+": non sono registrati altri noleggi.";
    }
}
