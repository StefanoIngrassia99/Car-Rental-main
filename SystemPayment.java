//Interfaccia STRATEGY
interface SystemPayment{
    void pay(double amount);
    String GetCode();
}
//STRATEGY A
class CreditCardPayment implements SystemPayment{
    private String CardNumber;
    public CreditCardPayment(String cardNumber){
        this.CardNumber=cardNumber;
    }
    @Override
    public void pay(double amount) {
        System.out.printf("Pagamento di %.2f euro con la carta %s%n",amount,CardNumber);
    }
    @Override
    public String GetCode(){
        return this.CardNumber;
    }
}
//STRATEGY B
class BancomatPayment implements SystemPayment{
    private String BancomatCode;
    public BancomatPayment(String bancomatCode){
        this.BancomatCode=bancomatCode;
    }
    @Override
    public void pay(double amount){
        System.out.printf("Pagamento di %.2f euro con il bancomat %s%n",amount,BancomatCode);
    }
     @Override
    public String GetCode(){
        return this.BancomatCode;
    }
}