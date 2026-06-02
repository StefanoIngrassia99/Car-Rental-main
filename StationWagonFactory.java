public class StationWagonFactory extends CarFactory {
    @Override
    public Car createCar(String targa, String modello) {
        return new StationWagon(targa, modello);
    }
}