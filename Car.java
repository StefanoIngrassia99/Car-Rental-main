//FACTORY PATTERN
public abstract class Car {
    private String targa;
    private String modello;
    
    public Car(String t, String m){
        this.targa=t;
        this.modello=m;
    }
    
    public String GetTarga(){
        return targa;
    }
    
    public String GetModello(){
        return modello;
    }
    
    public void DetailsCar(){
        System.out.println(this.modello+" con targa ("+this.targa+")");
    }

    public abstract String GetTipoAuto();
}