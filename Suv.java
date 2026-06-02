public class Suv extends Car {
    
    public Suv(String targa, String modello) {
        // Richiama il costruttore della classe padre (Car)
        super(targa, modello);
    }

    @Override
    public String GetTipoAuto() {
        return "SUV";
    }
}