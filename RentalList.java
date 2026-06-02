import java.util.*;

public class RentalList implements IteratorRental {
    private List<Rental> rentals = new ArrayList<>();

    public void AddRental(Rental r){
        rentals.add(r);
    }

    public void Removerental(Rental r){
        rentals.remove(r);
    }

    public List<Rental> GetRental(){
        return rentals;
    }

    @Override
    public MyIterator createIterator(){
        return new RentalIterator();
    } 

    private class RentalIterator implements MyIterator {
        private int index = 0;
        
        @Override
        public boolean hasNext(){
            while(index < rentals.size()){
                if(rentals.get(index).IsRitardo()){
                    return true;
                }
                index++;
            }
            return false;
        }

        @Override
        public Rental next(){
            return rentals.get(index++);
        }
    }
}