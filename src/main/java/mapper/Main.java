package mapper;



import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class Main {

  static class Wrapper {
    private Person[] personArr = new Person[]{new Person("PERSON1"), new Person("PERSON2"), new Person("PERSON3")};

    public Person[] getPersonArr() {
      return personArr;
    }

    public void setPersonArr(Person[] personArr) {
      this.personArr = personArr;
    }

    public Wrapper() {
    }
  }

  public static void main(String[] args) {

    bench();

  }

  public static void bench() {
    try {
      Person p = new Person("Old Name");

      ReflectionUtils.discoverObject(new Wrapper(), "Person");
      ReflectionUtils.getFieldMap("Person").keySet().forEach(System.out::println);
//            ReflectionUtils.parameterSet(p, "Person.Pet.Foods[2].Id", 12l);
//            System.out.println(ReflectionUtils.parameterGet(p, "Person.Pet.Foods[0].Id").toString());
//            System.out.println(ReflectionUtils.parameterGet(p, "Person.Pet.Foods[2].Id"));
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
  }

  public static class Person {
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

  public static class Animal {
    private String name = "DEFAULT";
    private int age;
    private LivingType livingType = LivingType.ANIMAL;
    private Food[] foods = new Food[] {new Food(), new Food(), new Food()};

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

  public static class Food {
    private String name = "nice";
    private long id;
    private LivingType livingType = LivingType.FOOD;

    public String getName() {
      return name;
    }

    public long getId() {
      return id;
    }

    public LivingType getLivingType() {
      return livingType;
    }

    public void setLivingType(LivingType livingType) {
      this.livingType = livingType;
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setId(long id) {
      this.id = id;
    }
  }

  public enum LivingType {
    HUMAN, ANIMAL, FOOD;
  }
}
