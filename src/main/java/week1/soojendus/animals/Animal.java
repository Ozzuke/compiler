package week1.soojendus.animals;

public abstract class Animal {
    private static int number = 0;

    public void makeNoise(
    ) {
        System.out.printf("Loom #%d: %s\n", number, getNoise());
    }

    public Animal() {
        ++number;
    }

    abstract String getNoise();

    public int getNumber() {
        return number;
    }
}
