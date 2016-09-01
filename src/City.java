import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by Syed on 8/27/2016.
 */
public class City {
    public String cityName;
    public String state;
    public int cityNumber;
    public List<DistanceTo> connections; //adjacency list
    public double [] allDistances; //will hold distances to all cities after dijkstras is run

    public City(String cityName, int cityNumber, String state) {
        this.cityName = cityName;
        this.cityNumber = cityNumber;
        this.state = state;
    }

}
