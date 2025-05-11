package example;

public class FAACasUniversalTest {

    // Définition de l'état MyState
    public static class MyState implements Universal.State<MyState> {
        private int value;

        public MyState(int value) {
            this.value = value;
        }

        @Override
        public MyState copy() {
            return new MyState(this.value);
        }

        public int getValue() {
            return value;
        }

        public void add(int amount) {
            this.value += amount;
        }
    }

    // Définition de l'opération AddOperation
    public static class AddOperation implements Universal.Operation<MyState, Integer> {
        private final int amount;

        public AddOperation(int amount) {
            this.amount = amount;
        }

        @Override
        public Integer apply(MyState state) {
            state.add(amount);  // Ajouter à l'état
            return state.getValue();  // Retourner la valeur actuelle de l'état
        }
    }

    // Méthode principale pour effectuer le test
    public static void main(String[] args) {
        // Initialiser l'état avec une valeur de 0
        MyState initialState = new MyState(0);

        // Créer une instance de FAACasUniversal avec l'état initial
        FAACasUniversal<MyState> universal = new FAACasUniversal<>(initialState);

        // Créer des opérations d'addition
        AddOperation add5 = new AddOperation(5);  // Ajouter 5
        AddOperation add10 = new AddOperation(10); // Ajouter 10
        AddOperation add3 = new AddOperation(3);
        AddOperation add7 = new AddOperation(7);
        AddOperation add6 = new AddOperation(6);
        AddOperation add2 = new AddOperation(2);


        // Test des opérations avec invoke

        // Exécuter l'opération d'ajout de 5
        Integer result5 = universal.invoke(add5);
        System.out.println("Résultat après ajout de 5 : " + result5); // Attendu : 5

        // Exécuter l'opération d'ajout de 10
        Integer result10 = universal.invoke(add10);
        System.out.println("Résultat après ajout de 10 : " + result10); // Attendu : 15

        // Exécuter l'opération d'ajout de 3
        Integer result3 = universal.invoke(add3);
        System.out.println("Résultat après ajout de 3 : " + result3); // Attendu : 18

        // Exécuter l'opération d'ajout de 7
        Integer result7 = universal.invoke(add7);
        System.out.println("Résultat après ajout de 7 : " + result7); // Attendu : 25

        // Exécuter l'opération d'ajout de 6
        Integer result6 = universal.invoke(add6);
        System.out.println("Résultat après ajout de 6 : " + result6); // Attendu : 31

        // Exécuter l'opération d'ajout de 2
        Integer result2 = universal.invoke(add2);
        System.out.println("Résultat après ajout de 2 : " + result2); // Attendu : 33

        // Affichage des nœuds d'annonce dans la liste
        FAACasUniversal<MyState>.AnnounceNode current = universal.announces;
        while (current != null) {
            System.out.println("ANode rank: " + current.rank);
            current = current.next.get();
        }
    }

}

