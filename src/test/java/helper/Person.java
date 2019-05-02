package helper;

public class Person {
    private String name;
    private int i = 5;
    private LivingType livingType = LivingType.HUMAN;
    private Animal pet = new Animal();

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setI(int i) {
        this.i = i;
    }

    public void setPet(Animal pet) {
        this.pet = pet;
    }

    public LivingType getLivingType() {
        return livingType;
    }

    public void setLivingType(LivingType livingType) {
        this.livingType = livingType;
    }

    public int getI() {

        return i;
    }

    public Animal getPet() {

        return pet;
    }

    @Override
    public String toString() {
        return "NAME: " + name + " PET: " + pet.getName();
    }
}