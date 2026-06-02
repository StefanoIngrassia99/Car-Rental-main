import databasehandler.DBHandler;
import java.util.*;
public class CarRentalSystem {
    private static CarRentalSystem instance;
    private HashMap<String, Parking> posti;
    private RentalList activerental;
    private Map<String, CarFactory> carFactories;
    private double euroxora=5.0;
    private double euroxkm=0.5;

    private CarRentalSystem(){
        this.posti=new HashMap<>();
        this.activerental=new RentalList();
        this.carFactories=new HashMap<>();
        this.carFactories.put("suv", new SuvFactory());
        this.carFactories.put("stationwagon", new StationWagonFactory());
    }
    public static synchronized CarRentalSystem GetIstance(){
        if(instance==null){
            instance=new CarRentalSystem();
        }return instance;
    }

    public void registerCarFactory(String tipo, CarFactory factory) {
        this.carFactories.put(tipo.toLowerCase(), factory);
    }

    //AZIONI AMMINISTRAZIONE
    public void CreateParkingLot(String n){
        posti.put(n, new Parking(n));
        //Aggiorno il DB
        DBHandler.InsertParking(n);
    }

    public void AddNewCar(String t, String m, String tipo, String Parkingname){
        if(posti.containsKey(Parkingname)){
            // Risolto OCP: Selezione dinamica della factory tramite Map
            String tipoKey = (tipo != null) ? tipo.toLowerCase() : "";
            CarFactory factory = carFactories.getOrDefault(tipoKey, new StationWagonFactory());

            // 2. Chiamiamo il metodo polimorfico (il sistema non sa che classe esatta sta creando)
            Car car = factory.createCar(t, m);
            
            posti.get(Parkingname).AddCar(car);
            
            // Aggiorno il DB
            DBHandler.InsertCar(t, m, Parkingname);

            System.out.println("Aggiunta la macchina " + t + " " + m + " di tipo " + car.GetTipoAuto());
        }
    }

    public void MoveCar(String t, String partenza, String arrivo){
        Parking part=posti.get(partenza);
        Parking arr=posti.get(arrivo);

        Car carmove=null;
        for(Car c : part.GetListcars()){
            if(c.GetTarga().equals(t)){
                carmove=c;
                break;
            }
        }
        if(carmove!=null){
            try {
                part.removeCar(carmove);
                arr.AddCar(carmove);
                //Aggiorno il DB
                DBHandler.MoveCar(t, arrivo);
                System.out.println("Spostata la macchina "+carmove.GetTarga()+" "+carmove.GetModello()+" dal parcheggio "+part+" al parcheggio "+arr);
            } catch (CarsNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void CheckRitardoUser(){
        MyIterator it=activerental.createIterator();
        while(it.hasNext()){
            Rental r=it.next();
            System.out.println("L'utente "+r.user.GetName()+" con noleggio auto "+r.car.GetTarga()+" e in ritardo con la consegna della macchina.");
        }
    }

    //AZIONI CLIENTI
    public void UserIscriveAbbonamento(User u){
        u.Payabbonamento();
    }

    public void Bookcar(User u, String carTarga, int h) throws UserNotAbbonatoException, CarsNotFoundException {
        if(!u.Isabbonato()){
            throw new UserNotAbbonatoException();
        }
        
        Car targetCar = null;
        Parking targetParking = null;

        for (Parking p : posti.values()) {
            for (Car c : p.GetListcars()) {
                if (c.GetTarga().equalsIgnoreCase(carTarga)) {
                    targetCar = c;
                    targetParking = p;
                    break;
                }
            }
            if (targetCar != null) break;
        }

        if (targetCar != null && targetParking != null) {
            targetParking.removeCar(targetCar);
            Rental r = new Rental(u, targetCar, h);
            activerental.AddRental(r);
            DBHandler.InsertRental(u.GetName(), targetCar.GetTarga(), h);
            System.out.println("Noleggio avviato all'utente " + u.GetName() + " con la macchina " + targetCar.GetTarga());
        } else {
            throw new CarsNotFoundException();
        }
    }

    public void Coda(Parking posto, int hours){
        if(!posto.GetListcars().isEmpty() && !posto.GetQueueUsers().isEmpty()){
            User u=posto.GetQueueUsers().poll();
            try {
                Car c=posto.GetAviableCar();
                Rental r=new Rental(u, c, hours);
                activerental.AddRental(r);
                //Aggiorno il DB
                DBHandler.InsertRental(u.GetName(), c.GetTarga(), hours);
                System.out.println("Noleggio avviato all'utente "+u.GetName()+" con la mnacchina "+c.GetTarga());
            } catch (CarsNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }else{
            System.out.println("Non ci sono macchine disponibili al momento.");
        }
    }

    public void ReturnCar(User u, String Parkingname, double kmfatti, SystemPayment payment){
        Rental r=null;
        for(Rental i : activerental.GetRental()){
            if(i.user.equals(u)){
                r=i;
                break;
            }
        }

        if(r!=null){
            Parking p=posti.get(Parkingname);
            if(p==null){
                throw new RuntimeException("Parcheggio non esistente: " + Parkingname);
            }
            if(!p.GetListcars().isEmpty()){
                throw new RuntimeException("Errore: Il parcheggio " + Parkingname + " è già occupato.");
            }

            System.out.println("L'utente "+u.GetName()+" ha restituito la macchina "+r.car.GetTarga());
            long durationMillis=System.currentTimeMillis() - r.startTime;
            double hoursUsed=Math.max(1, durationMillis / 1000.0);
            double tot=(hoursUsed * euroxora) + (kmfatti * euroxkm);
            payment.pay(tot);
            DBHandler.InsertPayment(payment.getClass().getSimpleName(), payment.GetCode(), tot, u.GetName());
            activerental.Removerental(r);
            p.AddCar(r.car);
            DBHandler.ReturnCar(r.car.GetTarga(), Parkingname);
        }else{
            throw new RuntimeException("Nessun noleggio trovato per "+ u.GetName());
        }
    }
}
