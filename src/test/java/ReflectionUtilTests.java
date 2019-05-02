import mapper.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReflectionUtilTests {
    public class Person {
        private String name;
        private int i = 5;
        private LivingType livingType = LivingType.HUMAN;
        private Animal pet = new Animal();

        Person(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        void setI(int i) {
            this.i = i;
        }

        void setPet(Animal pet) {
            this.pet = pet;
        }

        LivingType getLivingType() {
            return livingType;
        }

        void setLivingType(LivingType livingType) {
            this.livingType = livingType;
        }

        int getI() {

            return i;
        }

        Animal getPet() {

            return pet;
        }

        @Override
        public String toString() {
            return "NAME: " + name + " PET: " + pet.getName();
        }
    }

    public class Animal {
        private String name = "Rintintin";
        private int age;
        private LivingType livingType = LivingType.ANIMAL;
        private  Food[] foods = new Food[]{new Food(), new Food(), new Food()};

        Food[] getFoods() {
            return foods;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        LivingType getLivingType() {
            return livingType;
        }

        void setLivingType(LivingType livingType) {
            this.livingType = livingType;
        }

        int getAge() {
            return age;
        }

        void setFoods(Food[] foods) {
            this.foods = foods;
        }

        void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return String.format("Name: %s Age: %d ", name, age);
        }
    }

    public class Food {
        private String name = "nice";
        private long id;
        private LivingType livingType = LivingType.FOOD;

        String getName() {
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
        HUMAN, ANIMAL, FOOD
    }

    private Person person;

    @Before
    public void setUp() {
        person = new Person("OLD NAME");
        ReflectionUtils.discoverObject(Person.class, person, "Person");
    }

    @Test
    public void testGettingString() {
        assertEquals(person.getName(), ReflectionUtils.parameterGet("Person", person, "Person.Name"));
    }

    @Test
    public void testNestedGetWithArrayObject() {
        assertEquals(person.getPet().getFoods()[1].getName(), ReflectionUtils.parameterGet("Person", person, "Person.Pet.Foods[0].Name"));
    }
    // Yes I'm actually testing their mem adresses.
    @Test
    public void testNestedGet() {
        assertTrue(person.getPet().getFoods() == ReflectionUtils.parameterGet("Person", person, "Person.Pet.Foods"));
    }

    @Test
    public void testGettingEnum() {
        assertEquals(person.getLivingType(), ReflectionUtils.parameterGet("Person", person, "Person.LivingType"));
    }

    @Test
    public void testStringSet() {
        Person pp = new Person("Old Name");
        ReflectionUtils.parameterSet("Person", pp, "Person.Name", "New Name");
        assertEquals("New Name", pp.getName());
    }

    @Test
    public void testNestedIntSet() {
        Person pp = new Person("--");
        ReflectionUtils.parameterSet("Person", pp, "Person.Pet.Age", 52);
        assertEquals(52, pp.getPet().getAge());
    }

    @Test
    public void testObjectSet() {
        Person pp = new Person("--");
        Animal goodestBoy = new Animal();
        ReflectionUtils.parameterSet("Person", pp, "Person.Pet", goodestBoy);
        assertTrue(goodestBoy == pp.getPet());
    }

    @Test
    public void testArrayObjectSet() {
        Person pp = new Person("--");
        ReflectionUtils.parameterSet("Person", pp, "Person.Pet.Foods[0].LivingType", LivingType.ANIMAL);
        assertEquals(LivingType.ANIMAL, ReflectionUtils.parameterGet("Person", pp, "Person.Pet.Foods[0].LivingType"));
    }

//    @Test
//    public void testMultiGet() {
//      Person pp = new Person("--");
//      Animal pet = new Animal();
//      pp.setPet(pet);
//      List<Object> vals = ReflectionUtils.parameterMultiGet(pp,
//              Stream.of(
//                      "Person.Name",
//                      "Person.Pet",
//                      "Person.Pet.Foods[0]",
//                      "Person.Pet.Foods[1].LivingType"
//              )).toArray();
//      assertTrue(vals.containsAll(
//          List.of(pet, "--" , pp.getPet().getFoods()[0], pp.getPet().getFoods()[1].getLivingType())
//      ));
//    }

    @Test(expected = IllegalArgumentException.class)
    public void testNestedGetException() {
        ReflectionUtils.parameterGet("Person", person, "zxczxcz.zxcz");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetException() {
        ReflectionUtils.parameterGet("Person", person, "zxczxcz");
    }


}
