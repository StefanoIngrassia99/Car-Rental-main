//ECCEZZIONE PER LE MACCHINE
public class CarsNotFoundException extends Exception{
    public CarsNotFoundException(){
        super("Problema con l'assegnazione di una macchina");           //MESSAGGIO NEL TERMINALE ALLA CHIAMATA DELL'ECCEZIONE
    }
    @Override
    public String toString(){
        return getMessage()+": macchine esaurite";
    }
}
