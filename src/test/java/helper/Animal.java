package helper;

public class Animal {
    private String name = "Rintintin";
    private int age;
    private LivingType livingType = LivingType.ANIMAL;
    private Food[] foods = new Food[]{new Food(), new Food(), new Food()};

    public Food[] getFoods() {
        return foods;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LivingType getLivingType() {
        return livingType;
    }

    public void setLivingType(LivingType livingType) {
        this.livingType = livingType;
    }

    public int getAge() {
        return age;
    }

    public void setFoods(Food[] foods) {
        this.foods = foods;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return String.format("Name: %s Age: %d ", name, age);
    }
}
