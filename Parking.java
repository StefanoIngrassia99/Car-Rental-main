import java.util.*;
//IMPLEMENTAZIONE PARCHEGGI
public class Parking{
    private String name=null;
    private List<Car> cars;     //LISTA CHE CONTERRA LE MACCHINE (IL VERO E PRORPIO PARCHEGGIO)
    private Queue<User> reservations;           //QUEUE CHE GESTISCE LE PRENOTAZIONI DEGLI USERS (CON POLITICA FIFO)
    
    public Parking(String n){
        this.name=n;
        this.cars=new ArrayList<>();
        this.reservations=new LinkedList<>();
    }
    public void AddCar(Car c){
        cars.add(c);
    }
    public void removeCar(Car c) throws CarsNotFoundException {
        if(cars.isEmpty()){
            throw new CarsNotFoundException();          //ECCEZIONE CREATA
        }
        cars.remove(c);
    }
    public Car GetAviableCar() throws CarsNotFoundException {
        if(cars.isEmpty()){
            throw new CarsNotFoundException();
        }
        return cars.remove(0);
    }
    public void AddReservation(User u){
        reservations.add(u);
    }
    public String GetName(){
        return name;
    }
    public List<Car> GetListcars(){
        return cars;
    }
    public Queue<User> GetQueueUsers(){
        return reservations;
    }
}
