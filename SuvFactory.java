public class SuvFactory extends CarFactory {
    @Override
    public Car createCar(String targa, String modello) {
        return new Suv(targa, modello);
    }
}