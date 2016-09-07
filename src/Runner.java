import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Optimal meeting point calculator
 *
 * Class: CS342, Fall 2016
 * System: Windows 10, IntelliJ IDE
 * Author Code Number: 1530
 *
 *
 * Syed Bukhari
 */
public class Runner {
    public static void main(String[] args) {
        printAuthorInfo();

        try {
            runProgram(Runner.class.getResource("FilesToParse/CityNames.txt").getPath(),
                    Runner.class.getResource("FilesToParse/CityDistances.txt").getPath(),
                    Runner.class.getResource("FilesToParse/Participants.txt").getPath());
        } catch (IOException e) {
            System.out.println("Unable to locate a resource file, program will exit.");
        }

    }

    public static void printAuthorInfo() { //prints author info as mandated by syllabus
        System.out.println("Author Code Number: 1530");
        System.out.println("Class: CS342, Fall 2016");
        System.out.println("Program: #1, Optimal meeting point calculator");
    }
    public static void runProgram(String cityNamesFile, String cityDistancesFile, String participantsFile) throws IOException {
        //read in cities into city array
        City [] cityArray = parseCities(cityNamesFile);
        //read in distances between cities and populate DistanceTo arrays within cities array
        populateDistances(cityArray, cityDistancesFile);
        //read in participants and their locations
        Participant [] participants = populateParticipants(participantsFile);
        //display adjacency list
        displayAdjacencyList(cityArray);
        //run dijkstra's algorithm on every city and calculate min distance from all cities to each other
        runDijkstras(cityArray);
        //print distance from chicago (city 58) to all other cities
        System.out.println("Distance from Chicago, IL (City 58) to: ");
        City [] sortAlphabetically= cityArray.clone();
        Arrays.sort(sortAlphabetically, new Comparator<City>() {
            @Override
            public int compare(City o1, City o2) {
                return((o1.cityName + ", " + o1.state).compareTo(o2.cityName + ", " + o2.state));
            }
        });
        Map<Integer, Integer> arrayIndexMap= new HashMap<>();
        for(int i=0; i < sortAlphabetically.length; i++){
            arrayIndexMap.put(i,sortAlphabetically[i].cityNumber);
        }
        //now that our city array is sorted, we need to find the index of chicago
        int newIndex= Arrays.asList(sortAlphabetically).indexOf(cityArray[57]);
        for(int i = 0; i < sortAlphabetically[newIndex].allDistances.length-1; i++) {
            System.out.println(sortAlphabetically[i].cityName + ", " +sortAlphabetically[i].state + ": " + sortAlphabetically[newIndex].allDistances[arrayIndexMap.get(i)-1]);
        }
        double [] arr = findTotalAvgDistances(participants, cityArray);
        int minDistanceIndex = findSmallestAvgDistance(arr);
        DecimalFormat df = new DecimalFormat("##.##");
        System.out.println("The city with the smallest average travel distance is " + cityArray[minDistanceIndex].cityName + ", " +
                cityArray[minDistanceIndex].state + " with an average distance of " + new DecimalFormat("##.##").format(arr[minDistanceIndex]).toString() +" miles.");
    }
    public static int findSmallestAvgDistance(double avgDistances[]) { //finds smallest average distance in an array of average distances
        double min = avgDistances[0];
        int index = 0;
        for(int i = 0; i < avgDistances.length; i++) {
            if(avgDistances[i] < min) {
                index = i;
                min = avgDistances[i];
            }
        }
        return index;
    }

    public static double [] findTotalAvgDistances(Participant participants[], City cities[]) { // finds the average distance to all cities with regards to the participants' locations
        ArrayList<double[]> distancesToCheckFor = new ArrayList<>();
        double [] results = new double[cities.length];
        for(Participant participant : participants){
            distancesToCheckFor.add(cities[participant.cityNumber - 1].allDistances);
        }
        for(double distances[] : distancesToCheckFor) {
            for(int i = 0; i < distances.length; i++) {
                results[i] += distances[i];
            }
        }
        for(int i=0; i<results.length;i++){
            results[i] /= (double)participants.length;
        }
        return results;
    }

    public static void runDijkstras(City cityArray []) { //runs dijkstra's algorithm on every city to find the smallest distance to every other city
        for(int i = 0; i < cityArray.length; i++){
            PriorityQueue<DistanceTo> pq = new PriorityQueue<>(new Comparator<DistanceTo>() {
                @Override
                public int compare(DistanceTo o1, DistanceTo o2) {
                    return Double.compare(o1.distance, o2.distance);
                }
            });
            cityArray[i].allDistances[i] = 0; //set distance to self as 0
            pq.add(new DistanceTo(cityArray[i].cityNumber, 0)); //push first node into PQ
            while(!pq.isEmpty()) {
                DistanceTo dt = pq.remove(); //remove from queue
                for(DistanceTo neighbor : cityArray[dt.cityNumber - 1].connections){
                    if(cityArray[i].allDistances[neighbor.cityNumber - 1] > neighbor.distance + dt.distance) { //if value currently in index > pred + value found, swap vals
                        pq.add(new DistanceTo(neighbor.cityNumber, neighbor.distance + dt.distance)); //distance= self + neighbor's distance
                        cityArray[i].allDistances[neighbor.cityNumber - 1] = neighbor.distance + dt.distance;
                    }
                }
            }
        }

    }

    public static void displayAdjacencyList(City cityArray[]) { //displays the adjacency lists of cities
        for(City city : cityArray) {
            System.out.print(city.cityNumber + " ");
            for(DistanceTo connection : city.connections){
                System.out.print("-> " + connection.cityNumber);
                System.out.print(" ");
            }
            System.out.print("\n");
        }
    }

    public static Participant [] populateParticipants(String filePath) throws IOException { //parses the participants file
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String input = br.readLine();
            int numberOfParticipants = Integer.parseInt(input);
            Participant [] participantList = new Participant[numberOfParticipants];
            System.out.println("Adjacency list using ids:");
            for(int i = 0; i < numberOfParticipants; i++) {
                input = br.readLine();
                StringTokenizer parseLine = new StringTokenizer(input, " ");
                String name = parseLine.nextToken().trim();
                int city = Integer.parseInt(parseLine.nextToken().trim());
                participantList[i] = new Participant(name, city);
            }
            return participantList;
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Could not open " + filePath);
        } catch (IOException e) {
            throw e;
        }
    }

    public static City [] parseCities(String filePath) throws IOException { //parses the cityNames file and initializes distances for later use by dijkstra's algorithm function
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String input = br.readLine();
            int numberOfCities = Integer.parseInt(input);
            City [] cityList = new City[numberOfCities];
            for(int i = 0; i < numberOfCities; i++){
                String line = br.readLine();
                StringTokenizer parseLine = new StringTokenizer(line, ",");
                String city = parseLine.nextToken().trim();
                String state = parseLine.nextToken().trim();
                cityList[i] = new City(city, i+1, state);
                cityList[i].allDistances = new double[numberOfCities];
                for(int j = 0; j < cityList.length; j++){
                    cityList[i].allDistances[j] = Double.POSITIVE_INFINITY;
                }
                cityList[i].allDistances[i] = 0.0;
            }
            return cityList;
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Unable to open " + filePath);
        } catch (IOException e) {
            throw e;
        }
    }

    public static void populateDistances(City cities[], String filePath) throws IOException { //parses the cityDistances file and populates cities' adjacency lists
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String input = br.readLine();
            int numberOfDistances = Integer.parseInt(input);
            while((input = br.readLine()) != null){
                StringTokenizer parseLine = new StringTokenizer(input, " ");
                int city1 = Integer.parseInt(parseLine.nextToken());
                int city2 = Integer.parseInt(parseLine.nextToken());
                double distance = Double.parseDouble(parseLine.nextToken());
                //go to city1's index in cities (city1-1 b/c arrays start @ 0)
                //create new ArrayList if null
                //add a connection with city2 w/ distance
                if(cities[city1 - 1].connections == null) //distance from city 1 to city 2
                    cities[city1 - 1].connections = new ArrayList<>();
                cities[city1 - 1].connections.add(new DistanceTo(city2, distance));
                if(cities[city2 - 1].connections == null) //distance from city 2 to city 1
                    cities[city2 - 1].connections = new ArrayList<>();
                cities[city2 - 1].connections.add(new DistanceTo(city1, distance));
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Unable to open " + filePath);
        } catch (IOException e) {
            throw e;
        }

    }
}