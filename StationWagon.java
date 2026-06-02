public class StationWagon extends Car {
    
    public StationWagon(String targa, String modello) {
        // Richiama il costruttore della classe padre (Car)
        super(targa, modello);
    }

    @Override
    public String GetTipoAuto() {
        return "Station Wagon";
    }
}