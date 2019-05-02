import helper.Animal;
import helper.LivingType;
import helper.Person;
import mapper.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReflectionUtilTests {

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
