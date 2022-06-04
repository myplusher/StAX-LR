import java.util.List;

public class Location {
    int id;
    String locationType;
    String description;
    Double square;
    String address;
    List<Mechamism> sensors;
    List<Mechamism> devices;

    Location()
    {
        id = 0;
        locationType = "";
        description = "";
        square = 0.0;
        address = "";
    }
}
