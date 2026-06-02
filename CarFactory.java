public abstract class CarFactory {
    // Il "Factory Method" puro delegato alle sottoclassi
    public abstract Car createCar(String targa, String modello);
}