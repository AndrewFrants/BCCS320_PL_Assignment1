import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Entry point to the bus stop application
public class Main {

    // Method for printing route stops
    private static void getRouteStops(String route)
    {
        String url = Constants.routeURL + route;
        URL routeUrl = null;
        InputStream routesInputStream = null;
        BufferedReader routesHtmlReader;
        String line = null;

        System.out.println("The link for your route is: " + url);

        try {
            routeUrl = new URL(url);
        } catch (MalformedURLException e) {
            System.out.println("There was an error constructing the schedule URL: " + url);
            return;
        }

        try {
            routesInputStream = routeUrl.openStream();  // throws an IOException
        } catch (IOException e) {
            System.out.println("There was an error connecting to URL: " + url + " please check your internet connection and the website.");
            return;
        }

        routesHtmlReader = new BufferedReader(new InputStreamReader(routesInputStream));

        Boolean isInRoute = false;
        int stopNum = 1;

        do {
            try {
                line = routesHtmlReader.readLine();

                if (line != null) {

                    String startRouteChart = ".*<h2>Weekday<small>(.*)</small></h2>.*";
                    String busStopPattern = ".*<p>(.*)</p>.*";
                    String endRouteChart = "</thead>";

                    if (!isInRoute) {
                        Pattern p = Pattern.compile(startRouteChart, Pattern.DOTALL);
                        Matcher m = p.matcher(line);

                        isInRoute = m.matches();
                        if (isInRoute) {
                            System.out.println("Destination: " + makePretty(m.group(1)));
                            stopNum = 1;
                        }
                    }
                    else
                    {
                        Pattern p = Pattern.compile(busStopPattern, Pattern.DOTALL);
                        Matcher m = p.matcher(line);

                        if (m.matches())
                        {
                            System.out.println("Stop number: " + stopNum + " is " + makePretty(m.group(1)));
                            stopNum++;
                        }
                        else if (line.contains(endRouteChart)) {
                            isInRoute = false;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("There was an error reading from URL: " + url + " the connection was suddenly ended.");
                return;
            }
        } while (line != null);
    }

    /// Entry point
    public static void main(String[] args) {
        URL scheduleUrl = null;
        InputStream is = null;
        BufferedReader htmlReader;
        String line = null;
        Scanner scanner = new Scanner(System.in);
        String cityLetter = null;

        do {
            do {
                cityLetter = readEntry("Please enter a letter that your destination starts with: ");

                if (cityLetter.length() == 0 ||
                        cityLetter == null)
                    continue;

                cityLetter = cityLetter.toUpperCase();
                cityLetter = cityLetter.substring(0, 1);
            } while (cityLetter == null);

            try {
                scheduleUrl = new URL(Constants.scheduleURL);
            } catch (MalformedURLException e) {
                System.out.println("There was an error constructing the schedule URL: " + Constants.scheduleURL);
                continue;
            }

            try {
                is = scheduleUrl.openStream();  // throws an IOException
            } catch (IOException e) {
                System.out.println("There was an error connecting to URL: " + Constants.scheduleURL + " please check your internet connection and the website.");
                continue;
            }

            htmlReader = new BufferedReader(new InputStreamReader(is));

            Boolean isInRoutes = false;

            do {
                try {
                    line = htmlReader.readLine();

                    if (line != null) {

                        String startElement = ".*<h3>(" + cityLetter + ".*)</h3>.*";
                        String routeNumElement = ".*>([\\d/]+)</a></strong>.*";
                        String endElement = "<hr id";

                        if (!isInRoutes) {
                            // detect if we are in the routes element in HTML, this is denoted as <h3>City</h3>
                            Pattern p = Pattern.compile(startElement, Pattern.DOTALL);
                            Matcher m = p.matcher(line);

                            isInRoutes = m.matches();
                            if (isInRoutes) {
                                System.out.println("Destination: " + makePretty(m.group(1)));
                            }
                        } else {
                            // route link is denoted as an href link and ending with </a></strong>
                            Pattern p = Pattern.compile(routeNumElement, Pattern.DOTALL);
                            Matcher m = p.matcher(line);

                            if (m.matches()) {
                                System.out.println("Bus number: " + makePretty(m.group(1)));
                            } else if (line.contains(endElement)) {
                                isInRoutes = false;
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("There was an error reading from URL: " + Constants.scheduleURL + " the connection was suddenly ended.");
                }
            } while (line != null);

            int routeId = 0;

            do {

                String routeIdStr = readEntry("Please enter a route ID as string: ");

                try {
                    routeId = Integer.parseInt(routeIdStr);
                }
                catch (Exception e)
                {
                    System.out.println("Input was not a valid integer value.");
                    continue;
                }

                if (routeId <= 0)
                {
                    System.out.println("Input was not a valid integer value.");
                    continue;
                }

            } while (routeId == 0);

            getRouteStops(String.valueOf(routeId));

        } while (true);
    }

    // Helper method to make pretty output
    private static String makePretty(String input)
    {
        return input.replace("&amp;", "&");
    }

    static String readEntry(String prompt) {
        try {
            StringBuffer buffer = new StringBuffer();
            System.out.print(prompt);
            System.out.flush();
            int c = System.in.read();
            while(c != '\n' && c != -1) {
                buffer.append((char)c);
                c = System.in.read();
            }
            return buffer.toString().trim();
        } catch (IOException e) {
            return "";
        }
    }
}