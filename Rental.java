public class Rental {
    protected User user;
    protected Car car;
    long startTime;
    long TimeRental;

    public Rental(User u,Car c,int h){
        this.user=u;
        this.car=c;
        this.startTime=System.currentTimeMillis();
        this.TimeRental=h*1000;
    }
    public boolean IsRitardo(){
        long now = System.currentTimeMillis();
        return (now - this.startTime) > this.TimeRental;
    }
}
