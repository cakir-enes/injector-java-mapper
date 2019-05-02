package helper;

public class Food {
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