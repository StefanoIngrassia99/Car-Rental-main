class User{
    private String name=null;
    private int ID=0;
    private boolean abbonato=false;

    public User(String n){
        this.name=n;
        this.ID+=1;
        this.abbonato=false;
    }
    public boolean Isabbonato(){
        return abbonato;
    }
    public void Payabbonamento(){
        this.abbonato=true;
        System.out.println("L'utente "+this.name+" si e abbonato");
    }
    public String GetName(){
        return this.name;
    }
    // --- AGGIUNGI QUESTI METODI IN FONDO A USER.JAVA ---

    @Override
    public boolean equals(Object o) {
        // 1. Se sono lo stesso oggetto in memoria, sono uguali
        if (this == o) return true;
        
        // 2. Se l'oggetto passato è nullo o di un'altra classe, non sono uguali
        if (o == null || getClass() != o.getClass()) return false;
        
        // 3. Controllo se i nomi sono identici
        User user = (User) o;
        // Sostituisci 'nome' con il nome esatto della tua variabile (es. name, Name, ecc.)
        // Se nel tuo User hai un campo ID o Tessera univoca, usa quella per il confronto!
        return this.GetName().equals(user.GetName());
    }

    @Override
    public int hashCode() {
        // Genera un codice univoco basato sul nome
        return this.GetName().hashCode();
    }
}
